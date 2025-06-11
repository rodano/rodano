import {Settings} from './settings.js';
import {DBRepository, HTTPRepository} from './model/templates/repository.js';

const repositories = [];
let selected_repository;
let repositories_management_callback;

const BROWSER_REPOSITORY_ID = 'browser';
const SETTING_REPOSITORIES = 'repositories';
const SETTING_SELECTED_REPOSITORY = 'selected_repository';

function save_repositories() {
	Settings.Set(SETTING_REPOSITORIES, repositories.filter(r => r.id !== BROWSER_REPOSITORY_ID).map(r => r.getConfig()));
}

function draw_repository(repository) {
	const repository_li = document.createFullElement('li', {}, repository.id);
	repository_li.addEventListener(
		'click',
		function() {
			document.getElementById('template_repository_information').style.display = 'none';
			document.getElementById('template_repository')['repository_id'].value = repository.id;
			document.getElementById('template_repository')['repository_id'].setAttribute('disabled', 'disabled');
			document.getElementById('template_repository')['url'].value = repository.url;
			document.getElementById('template_repository')['login'].value = repository.login;
			document.getElementById('template_repository')['password'].value = repository.password;
			document.getElementById('template_repository').style.display = 'block';
			//select repository
			document.getElementById('template_repositories_list').children.forEach(function(child) {
				if(child === repository_li) {
					child.classList.add('selected');
				}
				else {
					child.classList.remove('selected');
				}
			});
		}
	);
	const repository_delete = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete repository'});
	repository_delete.addEventListener(
		'click',
		function(event) {
			event.stop();
			repositories.removeElement(repository);
			//select default browser repository if the repository that was selected has been deleted
			if(!repositories.includes(selected_repository)) {
				TemplateRepositories.SetSelectedRepository(BROWSER_REPOSITORY_ID);
			}
			save_repositories();
			//manage ui
			document.getElementById('template_repositories_list').removeChild(repository_li);
			document.getElementById('template_repository').style.display = 'none';
		}
	);
	repository_li.appendChild(repository_delete);
	return repository_li;
}

export const TemplateRepositories = {
	Init: function() {
		//add default browser repository
		const browser_repository = new DBRepository(BROWSER_REPOSITORY_ID, 'templates');
		repositories.push(browser_repository);
		//add repositories from settings
		repositories.pushAll(Settings.Get(SETTING_REPOSITORIES, []).map(r => new HTTPRepository(r.id, r.url, r.login, r.password)));
		TemplateRepositories.SetSelectedRepository(Settings.Get(SETTING_SELECTED_REPOSITORY, BROWSER_REPOSITORY_ID));

		const template_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('template_repositories'));

		//do not allow the user to close manually the dialog
		template_dialog.addEventListener('cancel', event => event.preventDefault());

		document.getElementById('template_repository_add').addEventListener(
			'click',
			function() {
				document.getElementById('template_repository_information').style.display = 'none';
				document.getElementById('template_repository').reset();
				document.getElementById('template_repository')['repository_id'].removeAttribute('disabled');
				document.getElementById('template_repository').style.display = 'block';
			}
		);

		document.getElementById('template_repository').addEventListener(
			'submit',
			function(event) {
				event.stop();
				const repository_id = this['repository_id'].value;
				let repository = TemplateRepositories.GetRepository(repository_id);
				if(repository === undefined) {
					//create a new repository
					repository = new HTTPRepository(repository_id, this['url'].value, this['login'].value, this['password'].value);
					repository.open();
					repositories.push(repository);
					//manage ui
					document.getElementById('template_repositories_list').appendChild(draw_repository(repository));
				}
				else {
					repository.url = this['url'].value;
					repository.login = this['login'].value;
					repository.password = this['password'].value;
				}
				save_repositories();
				document.getElementById('template_repository').style.display = 'none';
			}
		);

		document.getElementById('template_repository_test').addEventListener(
			'click',
			function(event) {
				event.stop();
				const repository_form = document.getElementById('template_repository');
				const repository_information = document.getElementById('template_repository_information');
				const repository_url = repository_form['url'].value;
				const repository_login = repository_form['login'].value;
				const repository_password = repository_form['password'].value;
				if(repository_url && repository_login && repository_password) {
					const repository = new HTTPRepository(repository_form['id'].value, repository_url, repository_login, repository_password);
					repository_information.classList.remove('error');
					repository_information.style.display = 'none';
					repository.authenticate()
						.then(() => {
							repository_information.textContent = 'Authentication successful';
							repository_information.style.display = 'block';
						}).catch(rejection => {
							if(rejection.status === 401) {
								repository_information.textContent = rejection.error;
							}
							else {
								repository_information.textContent = 'Unable to connect to repository';
							}
							repository_information.classList.add('error');
							repository_information.style.display = 'block';
						});
				}
				else {
					repository_information.textContent = 'All fields are required';
					repository_information.style.display = 'block';
				}
			}
		);

		document.getElementById('template_repositories_close').addEventListener(
			'click',
			function(event) {
				event.stop();
				if(repositories_management_callback) {
					repositories_management_callback();
					repositories_management_callback = undefined;
				}
				template_dialog.close();
			}
		);
	},
	GetRepositories: function() {
		return repositories;
	},
	GetRepository: function(id) {
		return repositories.find(r => r.id === id);
	},
	SetSelectedRepository: function(id) {
		selected_repository = repositories.find(r => r.id === id);
		Settings.Set(SETTING_SELECTED_REPOSITORY, selected_repository.id);
	},
	GetSelectedRepository: function() {
		if(!selected_repository) {
			//set browser as default repository
			TemplateRepositories.SetSelectedRepository(BROWSER_REPOSITORY_ID);
		}
		return selected_repository;
	},
	GetOtherRepositoriesIds() {
		return repositories.map(r => r.id).filter(i => i !== selected_repository.id);
	},
	GetTemplate: function(repository_id, template_type, template_id) {
		const repository = TemplateRepositories.GetRepository(repository_id);
		return repository.open().then(r => r.get(template_type, template_id));
	},
	OpenManageRepositoriesDialog: function(callback) {
		repositories_management_callback = callback;
		//refresh repositories list
		repositories
			.filter(r => r.id !== BROWSER_REPOSITORY_ID)
			.map(draw_repository)
			.forEach(Node.prototype.appendChild, document.getElementById('template_repositories_list').empty());
		/**@type {HTMLDialogElement}*/ (document.getElementById('template_repositories')).showModal();
	}
};
