import {api_base_url, Fetch} from './fetch.js';
import {UUID} from './basic-tools/uuid.js';
import {bus} from './event_bus.js';
import {UI} from './ui.js';
import {Users} from './users.js';
import {ApplicationManager} from './application.js';

let selected_action;
let selected_task_id;

let control_dialog;
let control_close;
let control_validate;
let control_confirmation;
let control_progress;
let control_progress_message;
let control_error;
let control_connected_users_warning;

const ACTIONS = {
	start: {
		id: 'start',
		label: 'Start',
		performed: 'started'
	},
	stop: {
		id: 'stop',
		label: 'Stop',
		performed: 'stopped'
	},
	restart: {
		id: 'restart',
		label: 'Restart',
		performed: 'restarted'
	}
};

export const Control = {
	Init: function() {
		//retrieve ui elements
		control_dialog = document.getElementById('control');
		control_close = document.getElementById('control_close');
		control_validate = document.getElementById('control_validate');

		control_confirmation = document.getElementById('control_confirmation');
		control_progress = document.getElementById('control_progress');
		control_progress_message = document.getElementById('control_progress_message');
		control_error = document.getElementById('control_error');
		control_connected_users_warning = document.getElementById('control_connected_users_warning');

		//register bus listener
		bus.register({
			onTaskMessageControl: function(event) {
				if(event.message.id === selected_task_id) {
					//handle error
					if(event.message.error) {
						control_error.textContent = `${event.message.error}: ${event.message.details}`;
						//restore ui
						control_validate.removeAttribute('disabled');
						control_close.removeAttribute('disabled');
						control_progress.style.display = 'none';
						control_progress_message.style.display = 'none';
					}
					//begin control task
					else if(event.message.begin) {
						control_progress.value = 0;
						control_progress.style.display = 'block';
						control_progress_message.textContent = '';
						control_progress_message.style.display = 'block';
					}
					//update progression
					else if(event.message.step) {
						control_progress.value = event.message.step;
						control_progress_message.textContent = event.message.message;
					}
					//end control task
					else if(event.message.end) {
						selected_task_id = undefined;
						//restore form ui
						control_validate.removeAttribute('disabled');
						control_close.removeAttribute('disabled');
						control_progress.style.display = 'none';
						control_progress_message.style.display = 'none';
						//refresh application state and ui
						ApplicationManager.Update();
						UI.Notify(`Application ${selected_action.performed.toLowerCase()} successfully`, {tag: 'control', icon: 'images/notifications/cog.png'});
						selected_action = undefined;
						window.history.back();
					}
				}
			}
		});

		control_validate.addEventListener(
			'click',
			function(event) {
				event.stop();
				if(!selected_task_id) {
					//hide error
					control_error.textContent = '';
					//update ui
					control_validate.setAttribute('disabled', 'disabled');
					control_close.setAttribute('disabled', 'disabled');

					//generate task uuid
					selected_task_id = UUID.Generate();
					//create form data
					const body = new FormData();
					body.append('action', selected_action.id);
					body.append('id', selected_task_id);
					//send data
					Fetch(`${api_base_url}/control`, {'method': 'POST', body, 'managed-error': [500]})
						.then(response => {
							control_progress.max = response.steps;
						})
						.catch(response => {
							control_error.textContent = response.body.error;
							//restore form ui
							control_validate.removeAttribute('disabled');
							control_close.removeAttribute('disabled');
						});
				}
				else {
					control_error.textContent = 'There is already one control task running';
				}
			}
		);
	},
	Launch: function(action_id) {
		selected_action = ACTIONS[action_id];
		selected_task_id = undefined;
		//check if there is users connected
		Users.GetUsers().then(users => {
			if(!users.isEmpty()) {
				control_connected_users_warning.textContent = `Warning! There is currently ${users.length} user(s) potentially connected on the application`;
				control_connected_users_warning.style.display = 'block';
			}
			else {
				control_connected_users_warning.style.display = 'none';
			}
		});
		//update application ui
		control_confirmation.textContent = `Are you sure you want to ${selected_action.label.toLowerCase()} the application?`;
		control_validate.textContent = selected_action.label;
		control_error.textContent = '';
		control_dialog.showModal();
	}
};
