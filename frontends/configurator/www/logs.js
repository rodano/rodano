import {Config} from './model_config.js';
import {logger} from './app.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';

export const Logs = {
	Init: function() {

		//add listeners to bus to log and change ui
		bus_ui.register({
			onChange: function(event) {
				let old_value, new_value;
				//when a back reference is updated, display back reference label
				if(Config.Entities[event.entity].getProperties()[event.property].back_reference) {
					old_value = Object.isObject(event.oldValue) ? event.oldValue.getLocalizedLabel(Languages.GetLanguage()) : 'undefined';
					new_value = Object.isObject(event.newValue) ? event.newValue.getLocalizedLabel(Languages.GetLanguage()) : 'undefined';
				}
				else {
					old_value = Object.isObject(event.oldValue) ? JSON.stringify(event.oldValue) : event.oldValue;
					new_value = Object.isObject(event.newValue) ? JSON.stringify(event.newValue) : event.newValue;
				}
				logger.log(4, `Change ${event.property} of ${event.entity} from ${old_value} to ${new_value}`);
			},
			onDelete: function(event) {
				logger.log(4, `Delete ${event.entity} ${event.node.id}`);
			}
		});

		const logs_content = document.getElementById('logs_content');
		const log_info = document.getElementById('logs_level_info');
		const log_warning = document.getElementById('logs_level_warning');
		const log_error = document.getElementById('logs_level_error');

		let selected_level = 1;

		function change_level(level) {
			selected_level = level;
			log_info.classList.remove('selected');
			log_warning.classList.remove('selected');
			log_error.classList.remove('selected');
			if(level <= 8) {
				log_error.classList.add('selected');
				if(level <= 5) {
					log_warning.classList.add('selected');
					if(level <= 2) {
						log_info.classList.add('selected');
					}
				}
			}
			logs_content.children.forEach(display_log);
		}

		function display_log(log_representation) {
			if(log_representation.dataset.logLevel >= selected_level) {
				log_representation.style.display = 'block';
			}
			else {
				log_representation.style.display = 'none';
			}
		}

		const observer = new MutationObserver(function(mutations) {
			mutations.forEach(function(mutation) {
				mutation.addedNodes.forEach(display_log);
			});
		});
		observer.observe(logs_content, {childList: true});

		log_info.addEventListener('click', change_level.bind(undefined, 2));
		log_warning.addEventListener('click', change_level.bind(undefined, 5));
		log_error.addEventListener('click', change_level.bind(undefined, 8));

		document.getElementById('logs_close').addEventListener('click', this.CloseDialog);

		logger.onlog = function(log) {
			const log_representation = document.createFullElement('li', {'class': `level-${log.level}`});
			log_representation.dataset.logLevel = log.level;
			log_representation.appendChild(document.createFullElement('time', {}, log.date.format('${hour}:${minute}:${second}')));
			log_representation.appendChild(document.createTextNode(log.message));
			logs_content.appendChild(log_representation);
		};
	},
	CloseDialog: function(event) {
		event.stop();
		document.getElementById('logs').style.display = 'none';
	},
	OpenDialog: function() {
		document.getElementById('logs').style.display = 'flex';
	}
};
