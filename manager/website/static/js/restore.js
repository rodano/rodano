import {api_base_url, Fetch} from './fetch.js';
import {UUID} from './basic-tools/uuid.js';
import {bus} from './event_bus.js';
import {UI} from './ui.js';
import {application} from './application.js';

let selected_task_id;

let restore_dialog;
let restore_close;
let restore_progress;
let restore_progress_message;
let restore_error;

let restore_storage;
let restore_storage_loading;
let restore_file;

export const Restore = {
	Init: function() {
		//retrieve ui elements
		restore_dialog = document.getElementById('restore');
		restore_close = document.getElementById('restore_close');
		restore_progress = document.getElementById('restore_progress');
		restore_progress_message = document.getElementById('restore_progress_message');
		restore_error = document.getElementById('restore_error');
		restore_storage = document.getElementById('restore_storage');
		restore_storage_loading = document.getElementById('restore_storage_loading');
		restore_file = document.getElementById('restore_file');

		function restore_database(type, file) {
			//hide error
			restore_error.textContent = '';
			//update ui
			restore_close.setAttribute('disabled', 'disabled');
			restore_storage.disable();
			restore_file.disable();

			//generate task uuid
			selected_task_id = UUID.Generate();
			//create form data
			const body = new FormData();
			body.append('id', selected_task_id);
			body.append('type', type);
			body.append('file', file);
			//send data
			Fetch(`${api_base_url}/restore`, {'method': 'POST', body, 'managed-error': [500]})
				.then(response => {
					restore_progress.max = response.steps;
				})
				.catch(response => {
					if(response.status === 413) {
						restore_error.textContent = 'File is too large';
					}
					else {
						restore_error.textContent = response.body.error;
					}
					//restore form ui
					restore_storage.enable();
					restore_file.enable();
					restore_close.removeAttribute('disabled');
				});
		}

		restore_storage.addEventListener(
			'submit',
			function(event) {
				event.stop();
				restore_database('backup', this['file'].value);
			}
		);

		restore_file.addEventListener(
			'submit',
			function(event) {
				event.stop();
				restore_database('file', this['file'].files[0]);
			}
		);

		//register bus listener
		bus.register({
			onTaskMessageRestore: function(event) {
				if(event.message.id === selected_task_id) {
					//handle error
					if(event.message.error) {
						restore_error.textContent = event.message.error;
						//restore ui
						restore_storage.enable();
						restore_file.enable();
						restore_close.removeAttribute('disabled');
						restore_progress.style.display = 'none';
						restore_progress_message.style.display = 'none';
					}
					else if(event.message.begin) {
						restore_progress.value = 0;
						restore_progress.style.display = 'block';
						restore_progress_message.textContent = '';
						restore_progress_message.style.display = 'block';
					}
					//update progression
					else if(event.message.step) {
						restore_progress.value = event.message.step;
						restore_progress_message.textContent = event.message.message;
					}
					//successful restart
					else if(event.message.end) {
						selected_task_id = undefined;
						//restore form ui
						restore_storage.enable();
						restore_file.enable();
						restore_close.removeAttribute('disabled');
						restore_progress.style.display = 'none';
						restore_progress_message.style.display = 'none';
						//refresh ui
						window.history.back();
						UI.Notify('Application restored successfully', {tag: 'control', icon: 'images/notifications/cog.png', body: 'Application restored successfully'});
					}
				}
			}
		});
	},
	Launch: function() {
		selected_task_id = undefined;
		//update application ui
		document.getElementById('restore_prod_warning').style.display = application.environment === 'PROD' ? 'block' : 'none';
		Fetch(`${api_base_url}/backups`, {'managed-error': [500]})
			.then(response => {
				document.getElementById('restore_storage')['file'].fill(response, true);
			})
			.catch(response => {
				restore_error.textContent = response.body.error;
			})
			.finally(() => restore_storage_loading.style.visibility = 'hidden');
		restore_storage_loading.style.visibility = 'visible';
		restore_error.textContent = '';
		restore_dialog.showModal();
	}
};
