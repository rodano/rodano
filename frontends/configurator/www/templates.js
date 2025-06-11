import './basic-tools/dom_extension.js';

import {UI} from './tools/ui.js';
import {logger} from './app.js';
import {ConfigHelpers} from './model_config.js';
import {Router} from './router.js';
import {StudyHandler} from './study_handler.js';
import {StudyTree} from './study_tree.js';
import {APITools} from './api_tools.js';
import {FormHelpers} from './form_helpers.js';
import {TemplateRepositories} from './template_repositories.js';
import {Entities} from './model/config/entities.js';

const TemplateTypes = Object.freeze({
	study: {
		label: 'Study',
		icon: 'book_open.png',
	},
	sheet: {
		label: 'Sheet',
		icon: 'page.png',
		parent_entity: Entities.Study
	},
	dataset_model: {
		label: 'Dataset model',
		icon: 'folder.png',
		parent_entity: Entities.Study
	},
	field_model: {
		label: 'Field model',
		icon: 'page_white_wrench.png',
		parent_entity: Entities.DatasetModel
	},
	workflow: {
		label: 'Workflow',
		icon: 'cog.png',
		parent_entity: Entities.Study
	}
});

function draw_template(template) {
	//draw template
	const template_li = document.createFullElement('li');
	template_li.dataset.id = template.id;
	template_li.dataset.type = template.type;
	const template_name = document.createFullElement('p');
	template_name.appendChild(document.createTextNode(template.name));
	if(template.user) {
		template_name.appendChild(document.createFullElement('span', {style: 'font-size: 0.8rem; margin-left: 0.5rem;'}, `by ${template.user}`));
	}
	template_li.appendChild(template_name);
	template_li.appendChild(document.createFullElement('p', {}, template.description));
	const template_actions = document.createElement('span');
	//add template button for non study templates
	if(template.type !== 'study') {
		const template_add = document.createFullElement('button', {title: 'Add this template', alt: 'Add'}, 'Add');
		template_add.addEventListener('click', template_add_listener);
		template_actions.appendChild(template_add);
	}
	//download template button
	const template_download = document.createFullElement('button', {style: 'margin-left: 0.5rem;', title: 'Download this template'}, 'Download');
	template_download.addEventListener('click', template_download_listener);
	template_actions.appendChild(template_download);
	//send template button
	const template_transfer = document.createFullElement('button', {style: 'margin-left: 0.5rem;', title: 'Transfer this template'}, 'Transfer');
	template_transfer.addEventListener('click', template_transfer_listener);
	template_actions.appendChild(template_transfer);
	//delete template button
	const template_delete = document.createFullElement('button', {style: 'margin-left: 0.5rem;', title: 'Delete this template'}, 'Delete');
	template_delete.addEventListener('click', template_delete_listener);
	template_actions.appendChild(template_delete);
	template_li.appendChild(template_actions);
	return template_li;
}

function template_delete_listener(event) {
	event.stop();
	const template_li = this.parentElement.parentElement;
	const template_type = template_li.parentElement.dataset.typeId;
	const template_id = template_li.dataset.id;
	UI.Validate('Are you sure you want to delete this template?').then(confirmed => {
		if(confirmed) {
			TemplateRepositories.GetSelectedRepository().open()
				.then(r => r.remove(template_type, template_id))
				.then(() => {
					template_li.parentElement.removeChild(template_li);
					const template_number = document.querySelector(`#templates_entities > li[data-type-id="${template_type}"] > span`);
					template_number.textContent = (parseInt(template_number.textContent) - 1).toString();
					const message = 'Template has been deleted successfully';
					logger.info(message);
					UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
				});
		}
	});
}

function add_template(template_type, template_id, parent) {
	UI.StartLoading();
	TemplateRepositories.GetSelectedRepository().open()
		.then(r => r.get(template_type, template_id))
		.then(data => {
			UI.StopLoading();
			//revive template
			const template = ConfigHelpers.Revive(data);
			//check that node id do not conflict with current configuration
			//do not try to change node ids as they may be depend on each other, for example the cell of a form model depending on a dataset model
			for(const [entity_id, node] of Object.entries(template.nodes)) {
				const entity = Entities[entity_id];
				if(parent.getHasChild(entity, undefined, node.id)) {
					const message = `A node with id ${node.id} already exists in ${parent.id}`;
					UI.Notify(message, {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					return;
				}
			}
			for(const node of Object.values(template.nodes)) {
				//add node in model
				parent.addChild(node);
				StudyTree.GetTree().find(parent).refresh();
				const message = `Template ${template.name} has been added to your configuration successfully`;
				logger.info(message);
				UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		});
}

function template_add_listener() {
	const template_type = this.parentElement.parentElement.dataset.type;
	const template_id = this.parentElement.parentElement.dataset.id;
	const templates_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('templates'));
	//for nodes that cannot be added directly in the study, a parent dataset model must be chosen
	const parent_entity = TemplateTypes[template_type].parent_entity;
	if(parent_entity !== Entities.Study) {
		const template_load_choice = /**@type {HTMLFormElement}*/ (document.getElementById('template_load_choice'));
		template_load_choice['template_type'].value = template_type;
		template_load_choice['template_id'].value = template_id;
		const path = Entities.Study.getPath(parent_entity);
		const parents = StudyHandler.GetStudy().getChildren(path[0]);
		FormHelpers.FillSelect(template_load_choice['parent'], parents, false);
		template_load_choice.style.display = 'block';
	}
	else {
		templates_dialog.close();
		add_template(template_type, template_id, StudyHandler.GetStudy());
	}
}

function template_download_listener() {
	const template_type = this.parentElement.parentElement.dataset.type;
	const template_id = this.parentElement.parentElement.dataset.id;
	TemplateRepositories.GetSelectedRepository().open()
		.then(r => r.get(template_type, template_id))
		.then(data => {
			const filename = `${data.id}.json`;
			const blob = new Blob([JSON.stringify(data, undefined, '\t')], {type: 'application/octet-stream;charset=utf-8'});
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
			setTimeout(() =>window.URL.revokeObjectURL(url), 0);
		});
}

function template_transfer_listener() {
	const template_type = this.parentElement.parentElement.dataset.type;
	const template_id = this.parentElement.parentElement.dataset.id;
	TemplateRepositories.GetSelectedRepository().open()
		.then(r => r.get(template_type, template_id))
		.then(template => {
			const template_send_choice = document.getElementById('template_send_choice');
			template_send_choice.template = template;
			document.getElementById('template_send_choice')['repository'].fill(TemplateRepositories.GetOtherRepositoriesIds(), false);
			template_send_choice.style.display = 'block';
		});
}

function update_repositories() {
	//update repositories list
	document.getElementById('templates_selected_repository').textContent = TemplateRepositories.GetSelectedRepository().id;
	const other_repositories_ids = TemplateRepositories.GetOtherRepositoriesIds();
	if(other_repositories_ids.isEmpty()) {
		document.getElementById('templates_choose_repository').style.display = 'none';
	}
	else {
		document.getElementById('templates_choose_repository').style.display = 'inline';
		document.getElementById('templates_choose_repository')['repository'].fill(other_repositories_ids, false);
	}
	Templates.UpdateTemplateList();
}

export const Templates = {
	Init: function() {
		//dynamically build templates section
		const templates_entities = document.getElementById('templates_entities');
		const templates_nodes = document.getElementById('templates_nodes');
		Object.entries(TemplateTypes).forEach(([template_type_id, template_type]) => {
			const type_section_id = `templates_${template_type_id}`;
			//create tab
			const type_tab = document.createFullElement('li', {'data-tab': type_section_id, 'data-type-id': template_type_id});
			type_tab.appendChild(document.createFullElement('img', {src: `images/entities_icons/${template_type.icon}`}));
			type_tab.appendChild(document.createTextNode(template_type.label));
			type_tab.appendChild(document.createFullElement('span'));
			templates_entities.appendChild(type_tab);
			//create associated section
			const type_section = document.createFullElement('ul', {id: type_section_id, 'data-type-id': template_type_id, style: 'display: none;'});
			templates_nodes.appendChild(type_section);
		});
		UI.Tabify(templates_entities);

		const templates_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('templates'));

		//template dialog is managed by the application URL
		templates_dialog.addEventListener('close', () => Router.CloseTool());

		const save_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('template_save'));

		document.getElementById('templates_repositories_manage').addEventListener(
			'click',
			function(event) {
				event.stop();
				TemplateRepositories.OpenManageRepositoriesDialog(update_repositories);
			}
		);

		save_dialog.querySelector('form').addEventListener(
			'submit',
			function(event) {
				function clean_dataset_model(dataset_model) {
					dataset_model.fieldModels.forEach(clean_field_model);
				}
				function clean_field_model(field_model) {
					field_model.rules = [];
					delete field_model.valueConstraint;
					delete field_model.valueFormula;
					field_model.validatorIds = [];
				}
				function clean_form_model(form_model) {
					delete form_model.conditions;
					form_model.layouts.forEach(clean_layout);
				}
				function clean_layout(layout) {
					layout.conditions = {};
				}
				function clean_workflow(workflow) {
					delete workflow.rules;
				}
				function clean_study(study) {
					ConfigHelpers.RemoveStaticNodes(study);
				}
				event.stop();
				const template = {
					id: Router.selectedNode.id,
					date: new Date(),
					user: APITools.GetUser() ? APITools.GetUser().login : 'NA',
					name: this['name'].value,
					description: this['description'].value
				};
				//do some check and prepare node for templating
				if(Router.selectedNode.getEntity() === Entities.Study) {
					const study = Object.clone(Router.selectedNode);
					clean_study(study);
					template.nodes = {
						Study: study
					};
					template.type = 'study';
				}
				else if(Router.selectedNode.getEntity() === Entities.FormModel) {
					const dataset_models = Router.selectedNode.getDatasetModels();
					if(dataset_models.length > 1) {
						save_dialog.close();
						UI.Notify('Unable to build a template from a form model linked to more than one dataset model', {
							tag: 'error',
							icon: 'images/notifications_icons/warning.svg',
							body: `Form model is linked to dataset models ${dataset_models.map(d => d.id).join(', ')}.`
						});
						return;
					}
					//retrieve and clean nodes
					const dataset_model = Object.clone(dataset_models[0]);
					const form_model = Object.clone(Router.selectedNode);
					clean_dataset_model(dataset_model);
					clean_form_model(form_model);
					//update template
					template.nodes = {
						DatasetModel: dataset_model,
						FormModel: form_model
					};
					template.type = 'sheet';
				}
				else if(Router.selectedNode.getEntity() === Entities.Workflow) {
					//retrieve and clean nodes
					const workflow = Object.clone(Router.selectedNode);
					clean_workflow(workflow);
					//update template
					template.nodes = {
						Workflow: workflow
					};
					template.type = 'workflow';
				}
				else if(Router.selectedNode.getEntity() === Entities.FieldModel) {
					//retrieve and clean nodes
					const field_model = Object.clone(Router.selectedNode);
					clean_field_model(field_model);
					//update template
					template.nodes = {
						FieldModel: field_model
					};
					template.type = 'field_model';
				}
				else if(Router.selectedNode.getEntity() === Entities.DatasetModel) {
					//retrieve and clean nodes
					const dataset_model = Object.clone(Router.selectedNode);
					clean_dataset_model(dataset_model);
					//update template
					template.nodes = {
						DatasetModel: dataset_model
					};
					template.type = 'dataset_model';
				}
				else {
					UI.Notify('Unsupported entity', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					return;
				}
				TemplateRepositories.GetSelectedRepository().open()
					.then(r => r.add(template))
					.then(() => {
						save_dialog.close();
						const message = 'Template saved successfully';
						logger.info(message);
						UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
					}).catch(error => {
						const message = `Unable to save template: ${error}`;
						logger.info(message);
						UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
					});
			}
		);

		document.getElementById('template_save_cancel').addEventListener(
			'click',
			function() {
				save_dialog.close();
			}
		);

		document.getElementById('template_load_choice').addEventListener(
			'submit',
			function(event) {
				event.stop();
				templates_dialog.close();
				const parent = StudyHandler.GetStudy().getDatasetModel(this['parent'].value);
				add_template(this['template_type'].value, this['template_id'].value, parent);
			}
		);

		document.getElementById('template_load_choice_cancel').addEventListener(
			'click',
			function() {
				document.getElementById('template_load_choice').style.display = 'none';
			}
		);

		document.getElementById('template_send_choice').addEventListener(
			'submit',
			function(event) {
				event.stop();
				const repository = TemplateRepositories.GetRepository(this['repository'].value);
				repository.open()
					.then(r => r.add(this.template))
					.then(() => {
						templates_dialog.close();
						const message = `Template ${this.template.name} has been sent to repository`;
						logger.info(message);
						UI.Notify(message, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
					});
			}
		);

		document.getElementById('template_send_choice_cancel').addEventListener(
			'click',
			function() {
				document.getElementById('template_send_choice').style.display = 'none';
			}
		);

		document.getElementById('templates_choose_repository').addEventListener(
			'submit',
			function(event) {
				event.stop();
				TemplateRepositories.SetSelectedRepository(this['repository'].value);
				update_repositories();
			}
		);
	},
	UpdateTemplateList: function() {
		//manage ui
		const templates_loading = document.getElementById('templates_loading');
		templates_loading.style.display = 'block';

		const templates_error = document.getElementById('templates_error');
		templates_error.style.display = 'block';

		TemplateRepositories.GetSelectedRepository().open().then(repository => {
			Object.keys(TemplateTypes).forEach(template_type_id => {
				//clean template entities
				const template_container = document.getElementById(`templates_${template_type_id}`);
				template_container.style.display = 'none';
				template_container.empty();
				//reset entities counters
				const template_number = document.querySelector(`#templates_entities > li[data-type-id="${template_type_id}"] > span`);
				template_number.textContent = '0';

				repository.getAll(template_type_id)
					.then(templates => {
						templates
							.map(draw_template)
							.forEach(Node.prototype.appendChild, document.getElementById(`templates_${template_type_id}`));
						template_number.textContent = templates.length.toString();
						document.getElementById('templates_entities').firstElementChild.click();
					})
					.catch(error => templates_error.textContent = error)
					.finally(() =>	templates_loading.style.display = 'none');
			});
		});
	},
	OpenSaveDialog: function() {
		//update information
		document.getElementById('template_save_repository').textContent = TemplateRepositories.GetSelectedRepository().id;
		//reset and show window
		const dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('template_save'));
		dialog.querySelector('form').reset();
		dialog.showModal();
	},
	OpenManageDialog: function() {
		document.getElementById('template_load_choice').style.display = 'none';
		document.getElementById('template_send_choice').style.display = 'none';
		/**@type {HTMLDialogElement}*/ (document.getElementById('templates')).showModal();
		update_repositories();
	}
};
