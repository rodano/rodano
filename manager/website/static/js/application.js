import {bus} from './event_bus.js';
import {api_base_url, Fetch, FetchFile} from './fetch.js';
import {UI} from './ui.js';

const UNKNOWN_APPLICATION = Object.freeze({
	name: 'Unknown',
	environment: '???',
	running: false,
	env: {},
	images: {}
});

const application = Object.assign({}, UNKNOWN_APPLICATION);

function update_application() {
	document.querySelector('main').style.display = 'block';
	document.getElementById('application_name').textContent = application.name;
	const application_environment = document.getElementById('application_environment');
	application_environment.className = `environment ${application.environment.toLowerCase()}`;
	application_environment.textContent = application.environment;

	const variables = document.getElementById('application_environment_variables');
	variables.empty();
	Object.keys(application.env).sort().forEach(key => {
		const li = document.createElement('li');
		li.textContent = `${key}=${application.env[key]}`;
		variables.appendChild(li);
	});

	const digests = document.getElementById('application_image_digests');
	digests.empty();
	Object.keys(application.images).sort().forEach(service => {
		const image = application.images[service];
		const li = document.createElement('li');
		let text = `${service}: ${image.digest}`;
		if(image.tag) {
			text += ` (${image.tag})`;
		}
		li.textContent = text;
		digests.appendChild(li);
	});

	//stop button
	const application_stop = document.getElementById('application_stop');
	application_stop.removeAttribute('disabled');
	application_stop.style.display = application.running ? 'inline-block' : 'none';
	//start button
	const application_start = document.getElementById('application_start');
	application_start.removeAttribute('disabled');
	application_start.style.display = application.running ? 'none' : 'inline-block';
	//restart button
	const application_restart = document.getElementById('application_restart');
	application_restart.style.display = application.running ? 'inline-block' : 'none';
	//reset and restore button
	const application_running_warning = document.getElementById('application_running_warning');
	const application_reset = document.getElementById('application_reset');
	const application_restore = document.getElementById('application_restore');
	if(application.running) {
		application_running_warning.style.display = 'block';
		application_reset.setAttribute('disabled', 'disabled');
		application_restore.setAttribute('disabled', 'disabled');
	}
	else {
		application_running_warning.style.display = 'none';
		application_reset.removeAttribute('disabled');
		application_restore.removeAttribute('disabled');
	}
}

function download_backup(event) {
	event.stop();
	FetchFile(this.href)
		.then(file => {
			const url = URL.createObjectURL(file);
			//Chrome does not support to set location href
			if(/Chrome/.test(navigator.userAgent)) {
				const link = document.createFullElement('a', {href: url, download: file.name});
				const event = new MouseEvent('click', {bubbles: true, cancelable: true});
				link.dispatchEvent(event);
			}
			else {
				location.href = url;
			}
		});
}

function delete_backup(event) {
	event.stop();
	const backup = this.dataset.backup;
	UI.Confirm(`Delete backup "${backup}"? This action cannot be undone.`, 'Delete', 'Cancel')
		.then(confirmed => {
			if(confirmed) {
				Fetch(`${api_base_url}/backups/${encodeURIComponent(backup)}`, {method: 'DELETE'})
					.then(() => {
						document.getElementById('application_backups').removeChild(this.parentElement);
					});
			}
		});
}

function update_backups(backups) {
	const application_backups = document.getElementById('application_backups');
	application_backups.empty();
	backups.forEach(backup => {
		const li = document.createElement('li');
		const download_link = document.createFullElement('a', {href: `${api_base_url}/backups/${encodeURIComponent(backup)}`}, backup);
		download_link.addEventListener('click', download_backup);
		li.appendChild(download_link);
		const delete_button = document.createFullElement('button', {'data-backup': backup}, 'Delete');
		delete_button.addEventListener('click', delete_backup);
		li.appendChild(delete_button);
		application_backups.appendChild(li);
	});
}

const ApplicationManager = {
	UpdateBackups: function() {
		Fetch(`${api_base_url}/backups`)
			.then(response => {
				update_backups(response);
			})
			.catch(() => {
				update_backups([]);
			});
	},
	Update: function() {
		Fetch(`${api_base_url}/info`, {'managed-error': [500, 503]})
			.then(app => {
				document.getElementById('error_not_started').style.display = 'none';
				Object.assign(application, app);
				application.running = true;
			})
			.catch(() => {
				document.getElementById('error_not_started').style.display = 'block';
				Object.assign(application, UNKNOWN_APPLICATION);
			})
			.finally(() => {
				update_application();
			});
		ApplicationManager.UpdateBackups();
	},
	Init: function() {
		//register listener to catch log messages
		const application_logs = document.getElementById('application_logs');
		bus.register({
			onLogMessage: function(event) {
				//eslint-disable-next-line no-control-regex
				const line = event.message.data.replace(/\u001b\[[0-9;]*[a-zA-Z]/g, '');
				application_logs.appendChild(document.createTextNode(line));
			}
		});

		ApplicationManager.Update();
	}
};

export {application, ApplicationManager};
