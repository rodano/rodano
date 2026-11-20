import {bus} from './event_bus.js';
import {api_base_url, Fetch, FetchFile} from './fetch.js';
import {UI} from './ui.js';
import {Hash} from './basic-tools/hash.js';
import {Table, Datasource} from './basic-table/index.js';

const UNKNOWN_APPLICATION = Object.freeze({
	name: 'Unknown',
	environment: '???',
	running: false,
	env: {},
	images: {}
});

const application = Object.assign({}, UNKNOWN_APPLICATION);

let backups_table;

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
	//restore buttons of the backups table depend on the application status
	if(backups_table?.datasource) {
		backups_table.draw();
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

function format_size(size) {
	const units = ['B', 'KB', 'MB', 'GB', 'TB'];
	let value = size;
	let unit = 0;
	while(value >= 1024 && unit < units.length - 1) {
		value /= 1024;
		unit++;
	}
	return `${unit === 0 ? value : value.toFixed(1)} ${units[unit]}`;
}

//the table does not support null values, and its filter only works on string values
function backup_to_row(backup) {
	return {
		id: backup.id,
		date: backup.date,
		type: backup.type ?? '',
		rationale: backup.rationale ?? '',
		size: backup.size,
		expiration_date: backup.expiration_date ?? ''
	};
}

function update_backups(rows) {
	if(!backups_table) {
		backups_table = new Table({
			container: document.getElementById('application_backups'),
			columns: [
				{
					label: 'Backup',
					data: 'id',
					type: Table.DataType.STRING,
					render: value => {
						const link = document.createFullElement('a', {href: `${api_base_url}/backups/${encodeURIComponent(value)}`}, value);
						link.addEventListener('click', download_backup);
						return link;
					}
				},
				{label: 'Date', data: 'date', type: Table.DataType.DATE, render: value => value.toFullDisplay(), width: 170},
				{label: 'Type', data: 'type', type: Table.DataType.STRING, width: 110},
				{label: 'Rationale', data: 'rationale', type: Table.DataType.STRING},
				{label: 'Size', data: 'size', type: Table.DataType.NUMBER, render: format_size, width: 90},
				{label: 'Expiration', data: 'expiration_date', type: Table.DataType.DATE, render: value => value ? value.toFullDisplay() : 'Never', width: 170},
				{
					label: 'Actions',
					unsortable: true,
					render: (_, record) => {
						const container = document.createElement('span');
						const restore_button = document.createFullElement('button', {title: 'The application must be stopped to restore a backup'}, 'Restore');
						if(application.running) {
							restore_button.setAttribute('disabled', 'disabled');
						}
						else {
							restore_button.removeAttribute('title');
						}
						restore_button.addEventListener('click', event => {
							event.stop();
							location.hash = Hash.Encode({action: 'restore', id: encodeURIComponent(record.id)});
						});
						container.appendChild(restore_button);
						const button = document.createFullElement('button', {style: 'margin-left: 0.3rem;'}, 'Delete');
						button.addEventListener('click', event => {
							event.stop();
							UI.Confirm(`Delete backup "${record.id}"? This action cannot be undone.`, 'Delete', 'Cancel')
								.then(confirmed => {
									if(confirmed) {
										Fetch(`${api_base_url}/backups/${encodeURIComponent(record.id)}`, {method: 'DELETE'})
											.then(() => update_backups(backups_table.datasource.data.filter(r => r.id !== record.id)));
									}
								});
						});
						container.appendChild(button);
						return container;
					},
					width: 150
				}
			]
		});
	}
	const datasource = new Datasource({data: rows});
	//newest backups first by default (overridden by the ordering saved by the table, if any)
	datasource.sortingOrders = [{field: 'date', descendant: true}];
	backups_table.render(datasource);
}

const ApplicationManager = {
	UpdateBackups: function() {
		Fetch(`${api_base_url}/backups`)
			.then(backups => update_backups(backups.map(backup_to_row)))
			.catch(() => update_backups([]));
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
