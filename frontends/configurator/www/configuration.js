import './basic-tools/extension.js';

import {ApplicationOutdatedError, Migrator} from './model/config/migrator.js';
import {UI} from './tools/ui.js';
import {LZW} from './basic-tools/lzw.js';
import {ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Settings} from './settings.js';
import {APITools} from './api_tools.js';
import {StudyLoader} from './study_loader.js';
import {TemplateLoader} from './template_loader.js';
import {Changelogs} from './changelogs.js';
import {StudyHandler} from './study_handler.js';
import {Reports} from './reports.js';
import {Welcome} from './welcome.js';

let current_file_handle;

const FILE_TYPE = {
	description: 'KV configuration file',
	accept: {
		'application/json': ['.json']
	}
};

function study_to_object(study) {
	//update study stats
	study.configDate = new Date().getTime();
	study.configUser = APITools.GetUser() ? APITools.GetUser().login : 'NA';
	//serialize and deserialize study to clone it
	const serialized_study = JSON.stringify(study);
	const object_study = JSON.parse(serialized_study);
	//remove nodes that must not appear in serialized configuration
	ConfigHelpers.RemoveStaticNodes(object_study);
	return object_study;
}

function push_to_template_server(study) {
	UI.StartLoading();
	//save study in current template
	const template = TemplateLoader.GetTemplate();
	template.nodes.Study = study_to_object(study);

	//send template
	TemplateLoader.GetRepository().open()
		.then(r => r.add(template))
		.finally(() => UI.StopLoading());
}

function push_to_rodano_server(study) {
	UI.StartLoading();
	//serialize config
	const config_serialized = Configuration.Serialize(study);
	//check if config can be compressed
	let valid = true;
	for(let i = 0; i < config_serialized.length; i++) {
		const code = config_serialized.charCodeAt(i);
		if(code > 256) {
			valid = false;
			console.log(`Unable to compress configuration: illegal character ${config_serialized.charAt(i)} found`);
		}
	}
	//build config blob
	let config;
	if(valid) {
		const config_data = JSON.stringify(LZW.Compress(config_serialized));
		config = new Blob([config_data], {type: 'application/json'});
	}
	else {
		config = new Blob([config_serialized], {type: 'application/json'});
	}
	//send config
	APITools.API.config.push(config, valid).then(
		function() {
			UI.StopLoading();
			UI.Notify('Configuration pushed to server successfully', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
		},
		function(response) {
			UI.StopLoading();
			switch(response.status) {
				case 403:
					response.json().then(result => {
						UI.Notify('Your do not have right to push the configuration', {
							tag: 'error',
							icon: 'images/notifications_icons/warning.svg',
							body: result.message
						});
					});
					break;
				default:
					UI.Notify('Unable to push configuration', {
						tag: 'error',
						icon: 'images/notifications_icons/warning.svg',
						body: 'Check your connectivity'
					});
			}
		}
	);
}

function draw_migrated_node(node) {
	const node_item = document.createElement('li');
	//node is raw json object that has not been revived
	//revive it to be able to use its class methods
	const constructor = ConfigHelpers.GetConfigEntitiesConstructors(node.className);
	const revived_node = new constructor(node);
	const label = revived_node.getLocalizedLabel ? revived_node.getLocalizedLabel() : revived_node.id || revived_node.entity;
	//TODO improve this by displaying a link to attribute
	//node_item.appendChild(document.createFullElement('a', {href : '#' + node.globalId, title : node.id}, label));
	node_item.appendChild(document.createTextNode(label));
	return node_item;
}

function draw_migration_report(report) {
	const report_item = document.createFullElement('li', {style: 'margin-bottom: 1rem;'});
	report_item.appendChild(document.createFullElement('img', {src: 'images/tick.png', style: 'margin-right: 0.5rem; vertical-align: bottom;'}));
	report_item.appendChild(document.createTextNode(report.description || 'No description'));
	if(report.nodes && !report.nodes.isEmpty()) {
		const report_instructions = document.createFullElement('div', {style: 'border-left: 10px solid #5d5d5b; margin-left: 20px; margin-top: 0.5rem; padding-left: 6px;'});
		report_instructions.appendChild(document.createTextNode(report.instructions || 'No instructions'));
		const report_nodes_item = document.createFullElement('ul', {style: 'list-style-type: disc; list-style-position: inside;'});
		report.nodes.map(draw_migrated_node).forEach(Node.prototype.appendChild, report_nodes_item);
		report_instructions.appendChild(report_nodes_item);
		report_item.appendChild(report_instructions);
	}
	return report_item;
}

export const Configuration = {
	Init: function() {
		//do not allow the user to close manually the migration dialog
		/**@type {HTMLDialogElement}*/ (document.getElementById('migration')).addEventListener('cancel', event => event.preventDefault());
	},
	Download: function() {
		const study = StudyHandler.GetStudy();

		const filename = `${study.id.toLowerCase()}.json`;
		const blob = new Blob([Configuration.Serialize(study)], {type: 'application/octet-stream;charset=utf-8'});
		const file = new File([blob], filename, {type: 'application/octet-stream;charset=utf-8', lastModified: Date.now()});
		const url = window.URL.createObjectURL(file);

		//Chrome does not support to set location href
		if(/Chrome/.test(navigator.userAgent)) {
			const link = document.createFullElement('a', {href: url, download: filename});
			const event = new MouseEvent('click', {view: window, bubbles: true, cancelable: true, detail: 1});
			link.dispatchEvent(event);
		}
		else {
			location.href = url;
		}

		//revoke url after event has been dispatched
		setTimeout(function() {
			window.URL.revokeObjectURL(url);
		}, 0);
	},
	Serialize: function(study, inline) {
		const object_study = study_to_object(study);
		return JSON.stringify(
			object_study,
			undefined,
			inline ? undefined : '\t'
		);
	},
	PushToServer: function() {
		const study = StudyHandler.GetStudy();
		UI.StartLoading();

		function prepare_push() {
			if(Settings.Get('document_before_push')) {
				Changelogs.Open(undefined, push);
				UI.StopLoading();
			}
			else {
				push();
			}
		}

		//TODO improve this
		function push() {
			if(TemplateLoader.HasTemplate()) {
				push_to_template_server(study);
			}
			else {
				push_to_rodano_server(study);
			}
		}

		const report = Reports.GenerateReport(study);
		if(report.errors.isEmpty()) {
			prepare_push();
		}
		else {
			UI.StopLoading();
			UI.Validate(
				`There is ${report.errors.length} errors in your configuration. Are you sure you want to push this configuration?`,
				'Push anyway',
				'Cancel'
			).then(confirmed => {
				if(confirmed) {
					prepare_push();
				}
			});
		}
	},
	PullFromServer: async function() {
		const config = await APITools.API.config.pull();
		ConfigHelpers.InsertStaticNodes(config);
		await Configuration.Load(config);
		//update UI
		const absolute_url = APITools.API.url.startsWith('http://') || APITools.API.url.startsWith('https://');
		const hostname = absolute_url ? new URL(APITools.API.url).host : `${window.location.hostname}:${window.location.port}`;
		document.querySelector('h1').textContent = `Configuration of server ${hostname}`;
		document.getElementById('menu_configuration_push').removeAttribute('disabled');
		document.getElementById('menu_configuration_push_icon').removeAttribute('disabled');
	},
	PullFromURL: async function(url) {
		//force no cache by adding timestamp in url
		const response = await fetch(`${url}?t=${new Date().getTime()}`);
		const config = await response.json();
		ConfigHelpers.InsertStaticNodes(config);
		await Configuration.Load(config);
		//update UI
		document.querySelector('h1').textContent = `Remote file at ${url}`;
		document.getElementById('menu_configuration_push').removeAttribute('disabled');
		document.getElementById('menu_configuration_push_icon').removeAttribute('disabled');
	},
	OpenFromFile: async function(file) {
		const text = await file.text();
		return Configuration.OpenFromText(text, file.name);
	},
	OpenFromText: async function(text, name) {
		const data = JSON.parse(text);
		let config, filename;
		//dropped file looks like a template
		if(data.hasOwnProperty('nodes') && data.nodes.hasOwnProperty('Study')) {
			config = data.nodes.Study;
			//keep hook on template information
			TemplateLoader.Load(data);
			filename = `Template ${name}`;
		}
		else {
			config = data;
			filename = `File ${name}`;
		}
		ConfigHelpers.InsertStaticNodes(config);
		await Configuration.Load(config);
		document.querySelector('h1').textContent = filename;
	},
	OpenFromFileSystem: async function() {
		const options = {
			multiple: false,
			types: [FILE_TYPE],

		};
		[current_file_handle] = await window.showOpenFilePicker(options);
		await current_file_handle.requestPermission({mode: 'readwrite'});
		const file = await current_file_handle.getFile();
		Configuration.OpenFromFile(file);
		//update UI
		document.querySelector('h1').textContent = `Local file ${current_file_handle.name}`;
		document.getElementById('menu_configuration_save_filesystem').removeAttribute('disabled');
		document.getElementById('menu_configuration_save_as_filesystem').removeAttribute('disabled');
		document.getElementById('menu_configuration_save_icon').removeAttribute('disabled');
	},
	WriteToFile: async function(handle) {
		const study = StudyHandler.GetStudy();
		try {
			const writable = await handle.createWritable();
			await writable.write(Configuration.Serialize(study));
			await writable.close();
			UI.Notify('Configuration saved successfully', {tag: 'info', icon: 'images/notifications_icons/done.svg', body: `Configuration written to file ${handle.name}`});
		}
		catch(error) {
			UI.Notify('Unable to save configuration', {tag: 'error', icon: 'images/notifications_icons/warning.svg', body: error.message});
		}
	},
	Save: async function() {
		return Configuration.WriteToFile(current_file_handle);
	},
	SaveAs: async function() {
		const options = {
			suggestedName: 'config.json',
			types: [FILE_TYPE]
		};
		const handle = await window.showSaveFilePicker(options);
		return Configuration.WriteToFile(handle);
	},
	Load: function(config) {
		return new Promise(function(resolve, reject) {
			//unload previous study
			StudyLoader.Unload();

			//migrate config
			function after_migration() {
				UI.StartLoading();
				try {
					//revive config
					bus.reset();
					const study = ConfigHelpers.Revive(config);
					//load study ui
					StudyLoader.Load(study);
					//callback
					resolve(study);
				}
				catch(error) {
					UI.Notify('Unable to revive configuration', {tag: 'error', icon: 'images/notifications_icons/warning.svg', body: error.message});
					reject(error);
				}
				finally {
					UI.StopLoading();
					document.getElementById('menu_configuration_close').removeAttribute('disabled');
				}
			}

			if(!Migrator.IsUpToDate(config)) {
				//close any other modal
				UI.CloseDialogs();
				//manage migration window
				document.getElementById('migration_current_version').textContent = Migrator.GetCurrentVersion().toString();
				document.getElementById('migration_version').textContent = config.configVersion;
				const migration_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('migration'));
				migration_dialog.querySelector('menu').empty();
				//open migration window
				migration_dialog.showModal();
				//migration
				try {
					document.getElementById('migration_upgrade').style.display = 'block';
					const migration_reports = Migrator.Migrate(config);
					//show migration history
					const migration_history = document.getElementById('migration_history');
					migration_history.empty();
					migration_reports.map(draw_migration_report).forEach(Node.prototype.appendChild, migration_history);
					migration_history.style.display = 'block';
					document.getElementById('migration_done').style.display = 'block';
					const migration_button_container = document.createElement('li');
					migration_button_container.appendChild(
						document.createFullElement(
							'button',
							{},
							'Ok',
							{click: function() {
								this.setAttribute('disabled', 'disabled');
								migration_dialog.close();
								after_migration();
							}}
						)
					);
					migration_dialog.querySelector('menu').appendChild(migration_button_container);
				}
				catch(exception) {
					//application is outdated
					if(exception instanceof ApplicationOutdatedError) {
						document.getElementById('migration_upgrade').style.display = 'none';
						document.getElementById('migration_outdated').style.display = 'block';
					}
					else {
						//error during migration
						document.getElementById('migration_error').textContent = exception.message;
						document.getElementById('migration_problem').style.display = 'block';
					}
				}
			}
			else {
				after_migration();
			}
		});
	},
	Close: function() {
		document.getElementById('menu_configuration_close').setAttribute('disabled', 'disabled');
		document.getElementById('menu_configuration_push').setAttribute('disabled', 'disabled');
		document.getElementById('menu_configuration_save_filesystem').setAttribute('disabled', 'disabled');
		document.getElementById('menu_configuration_save_as_filesystem').setAttribute('disabled', 'disabled');
		document.getElementById('menu_configuration_push_icon').setAttribute('disabled', 'disabled');
		document.getElementById('menu_configuration_save_icon').setAttribute('disabled', 'disabled');
		document.getElementById('menu_template_push').setAttribute('disabled', 'disabled');
		document.querySelector('h1').textContent = '';
		StudyLoader.Unload();
		const url = new URL(window.location.href);
		Array.from(url.searchParams.keys()).forEach(k => url.searchParams.delete(k));
		window.history.pushState({}, undefined, url.href);
		Welcome.Open();
	}
};
