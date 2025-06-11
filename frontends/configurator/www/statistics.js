import {logger} from './app.js';
import {bus_ui} from './bus_ui.js';
import {Entities} from './model/config/entities.js';
import {StudyHandler} from './study_handler.js';

function count_all_nodes(root_node) {
	const counters = {
		total: 0,
		entities: {}
	};
	function count_nodes(node) {
		counters.total++;
		if(!counters.entities[node.getEntity().name]) {
			counters.entities[node.getEntity().name] = 0;
		}
		counters.entities[node.getEntity().name]++;
		for(const [entity_name, relation] of Object.entries(node.getEntity().children)) {
			const entity = Entities[entity_name];
			for(let n = 0; n < relation.size; n++) {
				const children = node.getChildren(entity, n);
				for(let i = 0; i < children.length; i++) {
					count_nodes(children[i]);
				}
			}
		}
	}
	count_nodes(root_node);
	return counters;
}

function delete_stats_time_config(event) {
	event.stop();
	const stats_config = this.parentNode.parentNode;
	//update model
	const stats = localStorage.getObject('rodano.configurator.stats');
	delete stats.time.studies[stats_config.dataset.id];
	localStorage.setObject('rodano.configurator.stats', stats);
	//update ui
	stats_config.parentNode.removeChild(stats_config);
}

const Timer = {
	START_TIME: undefined,
	Start: function() {
		if(!Timer.START_TIME) {
			Timer.START_TIME = new Date().getTime();
			logger.info('Timer started');
		}
		else {
			logger.info('Timer has already been started');
		}
	},
	Stop: function() {
		if(Timer.START_TIME) {
			//store time in local storage
			const session_time = new Date().getTime() - Timer.START_TIME;
			const stats = localStorage.getItem('rodano.configurator.stats') ? localStorage.getObject('rodano.configurator.stats') : {time: {total: 0, studies: {}}};
			//total time
			stats.time.total += session_time;
			//study time
			const study = StudyHandler.GetStudy();
			if(study?.id) {
				if(!stats.time.studies.hasOwnProperty(study.id)) {
					stats.time.studies[study.id] = 0;
				}
				stats.time.studies[study.id] += session_time;
			}
			localStorage.setObject('rodano.configurator.stats', stats);
			//delete start time
			Timer.START_TIME = undefined;
			logger.info('Timer stopped');
		}
		else {
			logger.info('Timer has not been started');
		}
	},
	Reset: function() {
		Timer.Stop();
		localStorage.removeItem('rodano.configurator.stats');
		Timer.Start();
	}
};

export const Statistics = {
	Init: function() {
		bus_ui.register({
			onLoadStudy: function() {
				Timer.Start();
			},
			onUnloadStudy: function() {
				Timer.Stop();
			}
		});

		window.addEventListener('focus', Timer.Start);
		window.addEventListener('blur', Timer.Stop);

		document.getElementById('stats_close').addEventListener(
			'click',
			function() {
				/**@type {HTMLDialogElement}*/ (document.getElementById('stats')).close();
			}
		);

		document.getElementById('stats_reset').addEventListener(
			'click',
			function() {
				Timer.Reset();
				/**@type {HTMLDialogElement}*/ (document.getElementById('stats')).close();
			}
		);
	},
	Open: function() {
		const study = StudyHandler.GetStudy();
		//current config stats
		if(study) {
			//calculate node numbers
			const counters = count_all_nodes(study);
			const stats_nodes_global = document.getElementById('stats_nodes_global');
			stats_nodes_global.textContent = `Current configuration contains ${counters.total} nodes.`;
			//top notch entities
			const stats_nodes_entities = document.getElementById('stats_nodes_entities');
			stats_nodes_entities.empty();
			[Entities.FormModel, Entities.FieldModel, Entities.Validator, Entities.Workflow, Entities.RuleCondition].map(function(entity) {
				const stats_nodes_entity = document.createElement('tr');
				stats_nodes_entity.appendChild(document.createFullElement('td', {}, entity.name));
				stats_nodes_entity.appendChild(document.createFullElement('td', {style: 'text-align: right;'}, counters.entities[entity.name]));
				return stats_nodes_entity;
			}).forEach(Node.prototype.appendChild, stats_nodes_entities);
			//entities needing a script
			const field_models_scripts = Array.prototype.concat.apply([], study.datasetModels.map(d => d.fieldModels)).filter(d => d.dynamic);
			const validators_scripts = study.validators.filter(v => v.script);
			document.getElementById('stats_nodes_script').textContent = `There is ${field_models_scripts.length} plugins and ${validators_scripts.length} scripted validators.`;
			//give a complexity score to config
			let complexity = counters.total;
			complexity += (3 * counters.entities[Entities.FieldModel.name] + 5 * field_models_scripts.length);
			complexity += (5 * counters.entities[Entities.Validator.name] + 8 * validators_scripts.length);
			complexity += (2 * counters.entities[Entities.RuleCondition.name]);
			document.getElementById('stats_complexity').textContent = `Complexity score: ${complexity}`;
		}
		//application stats
		const stats_time_global = document.getElementById('stats_time_global');
		stats_time_global.empty();
		const stats_time_configs = document.getElementById('stats_time_configs');
		stats_time_configs.empty();
		const stats = localStorage.getObject('rodano.configurator.stats');
		if(stats) {
			stats_time_global.textContent = `You spent a total time of ${Date.getDurationLiteral(Math.round(stats.time.total / 1000))} on this application.`;
			for(const study_id in stats.time.studies) {
				if(stats.time.studies.hasOwnProperty(study_id)) {
					const stats_time_configs_config = document.createFullElement('tr', {'data-id': study_id});
					stats_time_configs_config.appendChild(document.createFullElement('td', {}, study_id));
					stats_time_configs_config.appendChild(document.createFullElement('td', {}, Date.getDurationLiteral(Math.round(stats.time.studies[study_id] / 1000))));
					//delete
					const stats_time_configs_config_delete = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', style: 'cursor: pointer;'});
					stats_time_configs_config_delete.addEventListener('click', delete_stats_time_config);
					const stats_time_configs_config_delete_cell = document.createFullElement('td');
					stats_time_configs_config_delete_cell.appendChild(stats_time_configs_config_delete);
					stats_time_configs_config.appendChild(stats_time_configs_config_delete_cell);
					stats_time_configs.appendChild(stats_time_configs_config);
				}
			}
		}
		else {
			stats_time_global.appendChild(document.createFullElement('p', {}, 'No stats for now.'));
		}
		/**@type {HTMLDialogElement}*/ (document.getElementById('stats')).showModal();
	}
};
