import {UUID} from './basic-tools/uuid.js';
import {bus} from './event_bus.js';
import {UI} from './ui.js';
import {api_base_url, Fetch} from './fetch.js';
import {application} from './application.js';

let selected_task_id;

let reset_dialog;
let reset_close;
let reset_progress;
let reset_error;

export const Reset = {
	Init: function() {
		//retrieve ui elements
		reset_dialog = document.getElementById('reset');
		const reset_form = document.getElementById('reset_form');
		reset_close = document.getElementById('reset_close');
		reset_progress = document.getElementById('reset_progress');
		reset_error = document.getElementById('reset_error');

		function manage_demo_users_password() {
			reset_form['demo_users_password'].parentNode.parentNode.style.display = reset_form['demo_users'].checked ? 'block' : 'none';
		}
		reset_form['demo_users'].addEventListener('change', manage_demo_users_password);
		manage_demo_users_password();

		document.getElementById('reset_generate_password').addEventListener(
			'click',
			function() {
				const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
				let password = 'P1!';
				while(password.length < 10) {
					password += characters[Math.floor(Math.random() * characters.length)];
				}
				reset_form['demo_users_password'].value = password;
			}
		);

		reset_form.addEventListener(
			'submit',
			function(event) {
				event.stop();
				//hide error
				reset_error.textContent = '';
				//update ui
				reset_close.setAttribute('disabled', 'disabled');
				reset_form.disable();

				//generate task uuid
				selected_task_id = UUID.Generate();
				//create form data
				const body = new FormData();
				body.append('id', selected_task_id);
				body.append('reference', this['reference'].value);
				body.append('demo_users', this['demo_users'].checked ? 'yes' : 'no');
				body.append('demo_users_password', this['demo_users_password'].value);
				body.append('demo_data', this['demo_data'].checked ? 'yes' : 'no');
				//send data
				Fetch(`${api_base_url}/reset`, {'method': 'POST', body, 'managed-error': [500]})
					.then(response =>
						reset_progress.max = response.steps
					)
					.catch(response => {
						reset_error.textContent = response.body.error;
						//restore form ui
						reset_form.enable();
						reset_close.removeAttribute('disabled');
					});
			}
		);

		//register bus listener
		bus.register({
			onTaskMessageReset: function(event) {
				if(event.message.id === selected_task_id) {
					//handle error
					if(event.message.error) {
						reset_error.textContent = event.message.error;
						//restore ui
						reset_form.enable();
						reset_close.removeAttribute('disabled');
						reset_progress.style.display = 'none';
					}
					else if(event.message.begin) {
						reset_progress.value = 0;
						reset_progress.style.display = 'block';
					}
					//update progression
					else if(event.message.step) {
						reset_progress.value = event.message.step;
					}
					//successful reset
					else if(event.message.end) {
						selected_task_id = undefined;
						//restore form ui
						reset_form.enable();
						reset_close.removeAttribute('disabled');
						reset_progress.style.display = 'none';
						//refresh ui
						window.history.back();
						UI.Notify('Application reset successfully', {tag: 'control', icon: 'images/notifications/cog.png', body: 'Application reset successfully'});
					}
				}
			}
		});
	},
	Launch: function() {
		selected_task_id = undefined;
		//update modal window title
		document.getElementById('reset_prod_warning').style.display = application.environment === 'PROD' ? 'block' : 'none';
		reset_error.textContent = '';
		reset_dialog.showModal();
	}
};
