import {UUID} from './basic-tools/uuid.js';
import {bus} from './event_bus.js';
import {UI} from './ui.js';
import {api_base_url, Fetch} from './fetch.js';
import {application} from './application.js';

let selected_task_id;

let backup_dialog;
let backup_form;
let backup_validate;
let backup_close;
let backup_progress;
let backup_progress_message;
let backup_error;

export const Backup = {
	Init: function() {
		//retrieve ui elements
		backup_dialog = document.getElementById('backup');
		backup_form = document.getElementById('backup_form');
		backup_validate = document.getElementById('backup_validate');
		backup_close = document.getElementById('backup_close');
		backup_progress = document.getElementById('backup_progress');
		backup_progress_message = document.getElementById('backup_progress_message');
		backup_error = document.getElementById('backup_error');

		//register bus listener
		bus.register({
			onTaskMessageBackup: function(event) {
				if(event.message.id === selected_task_id) {
					if(event.message.error) {
						backup_error.textContent = event.message.error;
						//restore ui
						backup_close.removeAttribute('disabled');
						backup_form.enable();
						backup_progress.style.display = 'none';
						backup_progress_message.style.display = 'none';
					}
					else if(event.message.begin) {
						backup_progress.value = 0;
						backup_progress.style.display = 'block';
						backup_progress_message.textContent = '';
						backup_progress_message.style.display = 'block';
					}
					//update progression
					else if(event.message.step) {
						backup_progress.value = event.message.step;
						backup_progress_message.textContent = event.message.message;
					}
					//successful backup
					else if(event.message.end) {
						selected_task_id = undefined;
						//restore form ui
						backup_close.removeAttribute('disabled');
						backup_form.enable();
						backup_progress.style.display = 'none';
						backup_progress_message.style.display = 'none';
						//refresh ui
						window.history.back();
						UI.Notify('Application backed up successfully', {tag: 'backup', icon: 'images/notifications/cog.png', body: 'Application backed up successfully'});
					}
				}
			}
		});

		backup_form.addEventListener(
			'submit',
			function(event) {
				event.stop();
				//update ui
				backup_error.textContent = '';
				backup_validate.setAttribute('disabled', 'disabled');
				backup_close.setAttribute('disabled', 'disabled');


				//generate task uuid
				selected_task_id = UUID.Generate();
				//create form data
				const body = new FormData();
				body.append('rationale', this['rationale'].value);
				body.append('id', selected_task_id);
				//send data
				Fetch(`${api_base_url}/backup`, {'method': 'POST', body, 'managed-error': [500]})
					.then(response => {
						backup_progress.max = response.steps;
					})
					.catch(response => {
						backup_error.textContent = response.body.error;
						//restore form ui
						backup_validate.removeAttribute('disabled');
						backup_close.removeAttribute('disabled');
					});
			}
		);
	},
	Launch: function() {
		selected_task_id = undefined;
		document.getElementById('backup_not_prod_warning').style.display = application.environment !== 'PROD' ? 'block' : 'none';
		//update application ui
		backup_error.textContent = '';
		backup_dialog.showModal();
	}
};
