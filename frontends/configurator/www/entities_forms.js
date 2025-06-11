import {Loader} from './basic-tools/loader.js';
import {Settings} from './settings.js';
import {NodeTools} from './node_tools.js';
import {FormStaticActions} from './form_static_actions.js';
import {Entities} from './model/config/entities.js';

const available_entities = {
	[Entities.Action.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.FieldModel.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.Cell.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.Chart.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.CMSSection.name]: {html: true, js: true, css: false, overlay: true},
	[Entities.CMSWidget.name]: {html: true, js: true, css: false, overlay: true},
	[Entities.Column.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Cron.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.DatasetModel.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.EventModel.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.EventGroup.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Feature.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Language.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Layout.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Menu.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.FormModel.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.PaymentPlan.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.PaymentStep.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.PrivacyPolicy.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Profile.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Report.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.ResourceCategory.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.RuleDefinitionAction.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.RuleDefinitionProperty.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.ScopeModel.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.Study.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.TimelineGraph.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.TimelineGraphSection.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.TimelineGraphSectionReference.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.Validator.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.Workflow.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.WorkflowState.name]: {html: true, js: true, css: false, overlay: false},
	[Entities.WorkflowSummary.name]: {html: true, js: true, css: true, overlay: false},
	[Entities.WorkflowWidget.name]: {html: true, js: true, css: true, overlay: false}
};

const FORMS_PATH = 'forms/';

const forms_loading = {};
const forms = {};
const loader = new Loader(document, {url: FORMS_PATH, nocache: false});

export const EntitiesForms = {
	Init: function() {
		//do not allow to close the dialog that contains the overlay forms
		/**@type {HTMLDialogElement}*/ (document.getElementById('edition_overlay')).addEventListener('cancel', event => event.preventDefault());
	},
	HasForm: function(entity) {
		return available_entities.hasOwnProperty(entity.name);
	},
	IsFormLoading: function(entity) {
		return forms_loading.hasOwnProperty(entity.name);
	},
	IsFormLoaded: function(entity) {
		return forms.hasOwnProperty(entity.name);
	},
	Load: async function(entity, container) {
		const definition = available_entities[entity.name];
		forms_loading[entity.name] = true;
		//load HTML template
		if(definition.html) {
			await loader.loadHTML(`${entity.id}.html`, container);
		}
		//load CSS
		if(definition.css) {
			await loader.loadCSS(`${entity.id}.css`);
		}
		//load script
		if(definition.js) {
			const form_module = (await import(`./${FORMS_PATH}${entity.id}.js`)).default;
			//keep a hook on form
			forms[entity.name] = form_module;
			//initialize only forms loaded asynchronously
			if(form_module.init) {
				form_module.init();
			}
		}
		//generic enhancement on form
		if(definition.html) {
			const editor = document.getElementById(`edit_${entity.id}_form`);
			FormStaticActions.EnhanceForm(editor);
		}
		//form is no longer loading
		delete forms_loading[entity.name];
	},
	Open: async function(entity, node, tab, reload) {
		const edition = document.getElementById('edition');
		//if form has not been loaded yet, load it and re-open it
		//next time, it will be loaded
		if(!EntitiesForms.IsFormLoaded(entity)) {
			if(!EntitiesForms.IsFormLoading(entity)) {
				EntitiesForms.Load(entity, edition).then(() => EntitiesForms.Open(entity, node, tab, reload));
			}
		}
		else {
			//retrieve form declaration
			const form_definition = forms[entity.name];

			//retrieve root HTML element base on the name of the node constructor
			//TODO this should be improved
			const editor = document.getElementById(`edit_${node.getEntity().id}`);

			//update title
			NodeTools.UpdateTitle(editor.querySelector('h2'), node);

			//update tabs links
			const tabs = editor.querySelector('ul.tabs');
			if(tabs) {
				const node_global_id = node.getGlobalId();
				//update tabs links
				tabs.children.forEach(function(item) {
					const link = item.firstElementChild;
					link.setAttribute('href', `#node=${node_global_id}&tab=${link.dataset.tab}`);
				});
			}

			//manage static nodes
			const form = document.getElementById(form_definition.form);
			if(node.staticNode) {
				form.disable();
			}
			else {
				form.enable();
			}

			const id_input = editor.querySelector('input[name="id"]');
			if(id_input) {
				if(Settings.Get('id_check')) {
					if(id_input.dataset.pattern) {
						id_input.setAttribute('pattern', id_input.dataset.pattern);
					}
				}
				else {
					if(id_input.hasAttribute('pattern')) {
						id_input.dataset.pattern = id_input.getAttribute('pattern');
						id_input.removeAttribute('pattern');
					}
				}
			}

			//try to put focus on id field if it exists
			/*var id_field = document.getElementById(node.getEntity().id + '_id');
			if(id_field) {
				id_field.focus();
			}*/

			edition.style.display = 'block';
			editor.style.display = 'block';

			//manage tabs
			//select good tab or first one
			if(tabs) {
				const selected_tab = tab || tabs.children[0].firstChild.dataset.tab;
				tabs.children.forEach(function(item) {
					const link = item.firstChild;
					if(link.dataset.tab === selected_tab) {
						item.classList.add('selected');
						document.getElementById(link.dataset.tab).style.display = 'block';
					}
					else {
						item.classList.remove('selected');
						document.getElementById(link.dataset.tab).style.display = 'none';
					}
				});
			}

			if(reload) {
				form_definition.open(node, tab);
			}
		}
	},
	//TODO remove this, used to manage overlays node
	OpenOverlay: function(node, entity, overlay_node, tab) {
		const edition_overlay = /**@type {HTMLDialogElement}*/ (document.getElementById('edition_overlay'));
		if(!EntitiesForms.IsFormLoaded(entity)) {
			EntitiesForms.Load(entity, edition_overlay).then(function() {
				EntitiesForms.OpenOverlay(node, entity, overlay_node, tab);
			});
		}
		else {
			//retrieve root HTML element base on the name of the node constructor
			//TODO this should be improved
			const editor = document.getElementById(`edit_${overlay_node.getEntity().id}`);

			//update title
			NodeTools.UpdateTitle(editor.querySelector('h2'), node, overlay_node);
			editor.style.display = 'flex';

			edition_overlay.showModal();

			forms[entity.name].open(overlay_node, tab);
		}
	},
	Close: function(entity, node, tab) {
		if(forms[entity.name].hasOwnProperty('close')) {
			return forms[entity.name].close(node, tab);
		}
		return document.getElementById(forms[entity.name].form).getUnsavedData(node);
	},
	//for testing purpose, this method loads all forms
	LoadAll: function() {
		const edition = document.getElementById('edition');
		const edition_overlay = document.getElementById('edition_overlay');
		//retrieve available entities
		const entities = Object.values(Entities).filter(e => available_entities.hasOwnProperty(e.name));
		return Promise.all(entities.map(e => EntitiesForms.Load(e, available_entities[e.name].overlay ? edition_overlay : edition)));
	}
};
