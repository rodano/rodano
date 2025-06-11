import './basic-tools/extension.js';

import {UI} from './tools/ui.js';
import {Network} from './tools/network.js';
import {ConfigHelpers} from './model_config.js';
import {Configuration} from './configuration.js';
import {TemplateLoader} from './template_loader.js';
import {Wizards} from './wizards.js';
import {APITools} from './api_tools.js';
import {TemplateRepositories} from './template_repositories.js';

const drop_message = 'Drop configuration file from your file system here';

function template_edit_listener() {
	UI.StartLoading();
	const template_li = this.parentElement.parentElement;
	const repository_id = template_li.parentElement.parentElement.dataset.id;
	const template_type = template_li.dataset.type;
	const template_id = template_li.dataset.id;
	TemplateRepositories.GetRepository(repository_id).open().then(repository => {
		repository.get(template_type, template_id).then(function(data) {
			//keep hook on template information
			TemplateLoader.Load(data, repository);
			UI.StopLoading();
			//load study
			const config = data.nodes.Study;
			ConfigHelpers.InsertStaticNodes(config);
			Configuration.Load(config).then(function() {
				//enable push template feature
				document.getElementById('menu_template_push').removeAttribute('disabled');
				/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).close();
			});
		});
	});
}

function template_use_listener() {
	const template_li = this.parentElement.parentElement;
	const repository_id = template_li.parentElement.parentElement.dataset.id;
	const template_id = template_li.dataset.id;
	/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).close();
	Wizards.Open('study', {'template': {repository: repository_id, id: template_id}});
}

function draw_template(template) {
	let template_name = template.name;
	if(template.user) {
		template_name += (` by ${template.user}`);
	}
	const template_li = document.createFullElement('li', {'data-type': 'study', 'data-id': template.id}, template_name);
	const template_actions = document.createFullElement('span');
	template_li.appendChild(template_actions);
	//edit template button
	const template_edit = document.createFullElement('img', {src: 'images/cog.png', title: 'Edit this template', alt: 'Edit'});
	template_edit.addEventListener('click', template_edit_listener);
	template_actions.appendChild(template_edit);
	//use template button
	const template_use = document.createFullElement('img', {src: 'images/application_put.png', title: 'Use this template for a new study', alt: 'Use'});
	template_use.addEventListener('click', template_use_listener);
	template_actions.appendChild(template_use);
	return template_li;
}

function refresh_templates_list() {
	const welcome_template_choice = document.getElementById('welcome_template_choice');
	const welcome_no_template = document.getElementById('welcome_no_template');
	welcome_template_choice.style.display = 'none';
	welcome_no_template.style.display = 'block';
	const welcome_templates = document.getElementById('welcome_templates');
	welcome_templates.empty();
	//browse repositories looking for study templates
	TemplateRepositories.GetRepositories().forEach(repository => {
		const repository_item = document.createFullElement('li', {'data-id': repository.id}, repository.id);
		const repository_templates_item = document.createFullElement('ul');
		repository_item.appendChild(repository_templates_item);
		repository.open()
			.then(r => r.getAll('study'))
			.then(templates => {
				if(!templates.isEmpty()) {
					//fill templates list
					templates.map(draw_template).forEach(Node.prototype.appendChild, repository_templates_item);
					welcome_templates.appendChild(repository_item);
					//show template choice only if there is at least one study template
					welcome_template_choice.style.display = 'block';
					welcome_no_template.style.display = 'none';
				}
			});
	});
}

export const Welcome = {
	Init: function() {
		document.getElementById('welcome_local_configuration_unsupported').style.display = window.hasOwnProperty('showOpenFilePicker') ? 'none' : 'block';

		const drop_error = document.getElementById('welcome_drop_error');
		const drop_local_configuration = document.getElementById('welcome_drop_local_configuration');
		drop_local_configuration.textContent = drop_message;

		function reset_drop() {
			drop_local_configuration.style.backgroundImage = '';
			drop_local_configuration.textContent = drop_message;
		}

		drop_local_configuration.addEventListener(
			'dragover',
			function(event) {
				event.stop();
				this.style.borderStyle = 'dashed';
			}
		);
		drop_local_configuration.addEventListener(
			'dragend',
			function(event) {
				event.stop();
				this.style.borderStyle = 'solid';
			}
		);
		drop_local_configuration.addEventListener(
			'drop',
			function(event) {
				event.preventDefault();
				this.style.borderStyle = 'solid';
				drop_error.style.display = 'none';
				//only one file can be managed at a time
				if(event.dataTransfer.files.length !== 1) {
					drop_error.textContent = `Drop one and only one file (${event.dataTransfer.files.length} files dropped)`;
					drop_error.style.display = 'block';
				}
				else {
					const file = event.dataTransfer.files[0];
					if(file.type !== '' && file.type !== 'application/json') {
						drop_error.textContent = 'Configuration must be a JSON file';
						drop_error.style.display = 'block';
					}
					else {
						const reader = new FileReader();
						reader.addEventListener(
							'loadstart',
							function() {
								UI.StartLoading();
								drop_local_configuration.style.backgroundImage = 'linear-gradient(to right, var(--background-highlight-color) 10%, var(--background-light-color) 10%)';
								drop_local_configuration.textContent = `Please wait while ${file.name} is loaded`;
							}
						);
						reader.addEventListener(
							'error',
							function() {
								drop_error.textContent = `Error while loading ${file.name}`;
								drop_error.style.display = 'block';
							}
						);
						reader.addEventListener(
							'load',
							function(reader_event) {
								try {
									Configuration.OpenFromText(reader_event.target.result, file.name);
								}
								catch(exception) {
									console.error(exception);
									drop_error.textContent = `Unable to parse file ${file.name}: only study configurations or study templates in JSON are supported`;
									drop_error.style.display = 'block';
								}
								finally {
									reset_drop();
								}
							}
						);
						reader.addEventListener(
							'loadend',
							function() {
								UI.StopLoading();
							}
						);
						reader.addEventListener(
							'progress',
							function(reader_event) {
								if(reader_event.lengthComputable) {
									const percent = Math.round((reader_event.loaded / reader_event.total) * 100);
									drop_local_configuration.style.backgroundImage = `linear-gradient(to right, var(--background-highlight-color) ${percent}%, var(--background-light-color) ${percent}%);`;
								}
							}
						);
						reader.readAsText(file);
					}
				}
			}
		);

		document.getElementById('welcome_open_repositories_management').addEventListener(
			'click',
			function(event) {
				event.stop();
				TemplateRepositories.OpenManageRepositoriesDialog(refresh_templates_list);
			}
		);

		document.getElementById('welcome_open_filesystem').addEventListener(
			'click',
			function(event) {
				event.stop();
				Configuration.OpenFromFileSystem();
			}
		);

		document.getElementById('welcome_connect').addEventListener(
			'submit',
			function(event) {
				event.stop();
				document.getElementById('welcome_connect_error').textContent = '';
				const api_url = this['url'].value;
				//check network
				UI.StartLoading();
				Network.Check(`${api_url}/config/public-study`)
					.then(() => {
						//store new API URL in URL
						window.history.pushState({}, undefined, `${window.location.origin + window.location.pathname}?api_url=${api_url}${window.location.hash}`);
					})
					.then(() => {
						//initialize API tools with URL parameters
						APITools.Init(api_url);
					})
					.then(APITools.RetrieveUser)
					.then(Configuration.PullFromServer)
					.catch(() => {
						document.getElementById('welcome_connect_error').textContent = 'Unable to reach server';
					})
					.finally(UI.StopLoading);
			}
		);

		//do not allow the user to close manually the dialog
		document.getElementById('welcome').addEventListener('cancel', event => event.preventDefault());
	},
	Open: function() {
		document.getElementById('welcome_connect')['url'].value = `${window.location.origin}/api`;
		document.getElementById('welcome_connect_error').textContent = '';
		refresh_templates_list();
		/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).showModal();
	}
};
