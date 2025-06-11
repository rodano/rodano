import './basic-tools/extension.js';

import {CSV} from './basic-tools/csv.js';
import {Config} from './model_config.js';
import {Languages} from './languages.js';
import {StudyHandler} from './study_handler.js';
import {NodeTools} from './node_tools.js';
import {Router} from './router.js';
import {Entities} from './model/config/entities.js';
import {Constrainables, Rulables} from './model/config/entities_categories.js';

const SEARCH_DELAY = 400;

//rules dashboard
function draw_rule(rule) {
	const rule_li = document.createElement('li');
	rule_li.appendChild(NodeTools.Draw(rule));
	rule.tags.map(t => document.createFullElement('span', {'class': 'tag'}, t)).forEach(Node.prototype.appendChild, rule_li);
	return rule_li;
}

function draw_rules(rules) {
	const rule_list = document.createFullElement('ul');
	rules.map(draw_rule).forEach(Node.prototype.appendChild, rule_list);
	return rule_list;
}

function draw_rules_entity(study, entity) {
	const rules_dashboard_entity = document.createElement('div');
	rules_dashboard_entity.appendChild(document.createFullElement('h3', {'class': 'dashboard_entity'}, entity.plural_label));
	const rules_dashboard_entity_list = document.createElement('div');
	rules_dashboard_entity.appendChild(rules_dashboard_entity_list);
	study.getDescendants(entity).forEach(function(node) {
		//container
		const node_report = document.createFullElement('div', {'class': 'dashboard_node'});
		//title
		const node_report_title = document.createFullElement('h3');
		node_report_title.appendChild(NodeTools.Draw(node));
		node_report.appendChild(node_report_title);

		//retrieve rules
		const relation = node.getEntity().children[Entities.Rule.name];
		if(relation.size > 1) {
			for(let i = 0; i < relation.size; i++) {
				const rules = node.getChildren(Entities.Rule, i);
				if(!rules.isEmpty()) {
					//subtitle
					const subtitle = relation.size > 1 ? relation.names[i] : Entities.Rule.label;
					node_report.appendChild(document.createFullElement('h4', {}, subtitle));

					//rules
					node_report.appendChild(draw_rules(rules));
					rules_dashboard_entity_list.appendChild(node_report);
				}
			}
		}
		else {
			const rules = node.getChildren(Entities.Rule);
			if(!rules.isEmpty()) {
				//rules
				node_report.appendChild(draw_rules(rules));
				rules_dashboard_entity_list.appendChild(node_report);
			}
		}
	});
	return rules_dashboard_entity;
}

//constraints dashboard
function draw_constraints_entity(study, entity) {
	const constraints_dashboard_entity = document.createElement('div');
	constraints_dashboard_entity.appendChild(document.createFullElement('h3', {'class': 'dashboard_entity'}, entity.plural_label));
	const constraints_dashboard_entity_list = document.createElement('div');
	constraints_dashboard_entity.appendChild(constraints_dashboard_entity_list);
	study.getDescendants(entity).forEach(function(node) {
		const constraints = node.getChildren(Entities.RuleConstraint);
		if(!constraints.isEmpty()) {
			//container
			const node_report = document.createFullElement('div');
			//title
			const node_report_title = document.createFullElement('h3');
			node_report_title.appendChild(NodeTools.Draw(node));
			node_report.appendChild(node_report_title);
			constraints_dashboard_entity_list.appendChild(node_report);
		}
	});
	return constraints_dashboard_entity;
}

//filter
function filter_rules(search, tag) {
	//update selected tag
	document.querySelectorAll('#rules_dashboard_tags > li').forEach(tag_li => {
		tag_li.classList.remove('selected');
		if(tag_li.textContent === tag) {
			tag_li.classList.add('selected');
		}
	});
	//update search text
	document.getElementById('rules_dashboard_search').value = search || '';
	//apply filer
	document.querySelectorAll('#rules_dashboard_entities .dashboard_node').forEach(function(container) {
		let match = false;
		container.querySelectorAll('li').forEach(function(rule) {
			const tag_match = !tag || rule.querySelectorAll('span').some(t => t.textContent === tag);
			const search_match = !search || rule.firstElementChild.textContent.nocaseIncludes(search);
			if(tag_match && search_match) {
				match = true;
				rule.style.display = 'block';
			}
			else {
				rule.style.display = 'none';
			}
		});
		container.style.display = match ? 'block' : 'none';
	});
}

function draw_tag(tag) {
	const tag_li = document.createFullElement('li', {}, tag);
	tag_li.addEventListener(
		'click',
		function() {
			const parameters = {};
			if(!this.classList.contains('selected')) {
				parameters.tag = this.textContent;
			}
			Router.SelectReport('rules_dashboard', parameters);
		}
	);
	return tag_li;
}

function build_rules_report(node, property, property_index) {
	const children = node.getChildren(Entities.Rule, property_index);
	return children.map(function(rule) {
		return [
			node.getEntity().label,
			node.getLocalizedShortname(Languages.GetLanguage()),
			property || '',
			rule.getLocalizedLabel(Languages.GetLanguage()),
			rule.tags.join(', ')
		];
	});
}

export const Dashboards = {
	Init: function() {

		//dashboard dialogs are managed by the application URL
		/**@type {HTMLDialogElement}*/ (document.getElementById('rules_dashboard')).addEventListener('close', () => Router.CloseReport());
		/**@type {HTMLDialogElement}*/ (document.getElementById('constraints_dashboard')).addEventListener('close', () => Router.CloseReport());

		let search_throttle;
		document.getElementById('rules_dashboard_search').addEventListener(
			'input',
			function(event) {
				event.stop();
				//add throttle to real search to avoid filling browser history with useless searches
				//timeout is used to detect end of the search
				if(search_throttle) {
					clearTimeout(search_throttle);
				}
				search_throttle = setTimeout(() => {
					Router.SelectReport('rules_dashboard', {search: this.value});
				}, SEARCH_DELAY);
			}
		);

		document.getElementById('rules_dashboard_report').addEventListener(
			'click',
			function() {
				const study = StudyHandler.GetStudy();
				const data = [];
				data.push(['Element type', 'Element', 'Rule type', 'Rule description', 'Rule tags']);
				Rulables.forEach(entity => {
					study.getDescendants(entity).forEach(node => {
						//retrieve rules
						const relation = node.getEntity().children[Entities.Rule.name];
						if(relation.size > 1) {
							for(let i = 0; i < relation.size; i++) {
								const property_name = relation.size > 1 ? relation.names[i] : Entities.Rule.label;
								data.pushAll(build_rules_report(node, property_name, i));
							}
						}
						else {
							data.pushAll(build_rules_report(node));
						}
					});
					data.push([]);
				});
				//add triggers
				for(const [trigger_id, trigger] of Object.entries(Config.Enums.Trigger)) {
					if(study.eventActions[trigger_id] && !study.eventActions[trigger_id].isEmpty()) {
						data.pushAll(study.eventActions[trigger_id].map(function(rule) {
							return [
								'Trigger',
								trigger.shortname[Languages.GetLanguage()],
								'',
								rule.getLocalizedLabel(Languages.GetLanguage()),
								rule.tags.join(', ')
							];
						}));
					}
				}
				new CSV(data).download('rules.csv');
			}
		);

		document.getElementById('constraints_dashboard_report').addEventListener(
			'click',
			function() {
				const study = StudyHandler.GetStudy();
				const data = [];
				data.push(['Element type', 'Element']);
				Constrainables.forEach(function(entity) {
					study.getDescendants(entity).forEach(function(node) {
						const constraints = node.getChildren(Entities.RuleConstraint);
						if(!constraints.isEmpty()) {
							const line = [
								node.getEntity().label,
								node.getLocalizedLabel(Languages.GetLanguage()),
							];
							data.push(line);
						}
					});
					data.push([]);
				});
				new CSV(data).download('constraints.csv');
			}
		);
	},
	//rules dashboard
	ShowRules: function(study, parameters) {
		//clear search field
		document.getElementById('rules_dashboard_search').value = '';

		//manage tags
		study.ruleTags.map(draw_tag).forEach(Node.prototype.appendChild, document.getElementById('rules_dashboard_tags').empty());

		const rules_dashboard_entities = document.getElementById('rules_dashboard_entities');
		rules_dashboard_entities.empty();

		//entities
		Rulables.map(e => draw_rules_entity(study, e)).forEach(Node.prototype.appendChild, rules_dashboard_entities);

		//trigger
		const rules_dashboard_triggers = document.createElement('div');
		rules_dashboard_triggers.appendChild(document.createFullElement('h3', {'class': 'dashboard_entity'}, 'Triggers'));
		let has_trigger_rules = false;
		for(const [trigger_id, trigger] of Object.entries(Config.Enums.Trigger)) {
			if(study.eventActions[trigger_id] && !study.eventActions[trigger_id].isEmpty()) {
				has_trigger_rules = true;
				//container
				const trigger_report = document.createFullElement('div', {'class': 'dashboard_node'});
				//title
				trigger_report.appendChild(document.createFullElement('h3', {}, trigger.shortname[Languages.GetLanguage()]));
				//rules
				trigger_report.appendChild(draw_rules(study.eventActions[trigger_id]));
				rules_dashboard_triggers.appendChild(trigger_report);
			}
		}
		if(has_trigger_rules) {
			rules_dashboard_entities.appendChild(rules_dashboard_triggers);
		}

		if(parameters) {
			filter_rules(parameters.search, parameters.tag);
		}

		/**@type {HTMLDialogElement}*/ (document.getElementById('rules_dashboard')).showModal();
	},
	//constraints dashboard
	ShowConstraints: function(study) {
		Constrainables.map(e => draw_constraints_entity(study, e)).forEach(Node.prototype.appendChild, document.getElementById('constraints_dashboard_entities').empty());

		/**@type {HTMLDialogElement}*/ (document.getElementById('constraints_dashboard')).showModal();
	}
};
