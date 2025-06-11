import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {DatasetModel} from '../model/config/entities/dataset_model.js';
import {FieldModel} from '../model/config/entities/field_model.js';

const sort_margin = 5;

function manage_multiple() {
	if(/**@type {HTMLInputElement}*/ (document.getElementById('dataset_model_multiple')).checked) {
		document.getElementById('dataset_model_labels').style.display = 'block';
	}
	else {
		//reset and hide fields
		/**@type {HTMLInputElement}*/ (document.getElementById('dataset_model_collapsed_label_pattern')).value = '';
		/**@type {HTMLInputElement}*/ (document.getElementById('dataset_model_expanded_label_pattern')).value = '';
		document.getElementById('dataset_model_labels').style.display = 'none';
	}
}

function draw_field_model(field_model) {
	const field_model_li = document.createFullElement('li', {'data-field-model-id': field_model.id});
	field_model_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort field model', title: 'Sort field model'}));
	field_model_li.appendChild(document.createFullElement('span', {}, field_model.exportOrder === undefined ? 'x' : field_model.exportOrder));
	field_model_li.appendChild(document.createTextNode(field_model.getLocalizedShortname(Languages.GetLanguage())));
	return field_model_li;
}

function draw_field_models(dataset_model) {
	//draw interface used to sort field models
	const field_models = dataset_model.fieldModels.slice();
	field_models.sort(FieldModel.getExportComparator());
	field_models.map(draw_field_model).forEach(Node.prototype.appendChild, document.getElementById('dataset_model_field_models').empty());
}

let selected_dataset_model;

export default {
	form: 'edit_dataset_model_form',
	init: function() {
		document.getElementById('edit_dataset_model_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		function dragstart(event) {
			this.style.opacity = 0.6;
			event.dataTransfer.effectAllowed = 'copy';
			event.dataTransfer.setData('text/plain', this.textContent);
		}
		function dragend() {
			this.style.opacity = 1;
		}
		document.getElementById('dataset_model_labels_patterns').querySelectorAll('span').forEach(function(pattern) {
			pattern.addEventListener('dragstart', dragstart);
			pattern.addEventListener('dragend', dragend);
		});

		document.getElementById('dataset_model_multiple').addEventListener('change', manage_multiple);

		document.getElementById('dataset_model_all_field_models_validator').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const validator_id = /**@type {HTMLSelectElement}*/ (document.getElementById('dataset_model_field_model_validators')).value;
				selected_dataset_model.fieldModels.forEach(field_model => {
					if(!field_model.validatorIds.includes(validator_id)) {
						field_model.validatorIds.push(validator_id);
					}
				});
				UI.Notify(
					'Validator added',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `Validator ${validator_id} has been added to all field models of dataset model ${selected_dataset_model.id}`
					}
				);
			}
		);

		document.getElementById('dataset_model_all_field_models_not_validator').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const validator_id = /**@type {HTMLSelectElement}*/ (document.getElementById('dataset_model_field_model_validators')).value;
				selected_dataset_model.fieldModels.forEach(field_model => {
					field_model.validatorIds.removeElement(validator_id);
				});
				UI.Notify(
					'Validator removed',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `Validator ${validator_id} has been removed from all field models of dataset model ${selected_dataset_model.id}`
					}
				);
			}
		);

		document.getElementById('dataset_model_all_field_models_workflow').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const workflow_id = /**@type {HTMLSelectElement}*/ (document.getElementById('dataset_model_field_model_workflows')).value;
				selected_dataset_model.fieldModels.forEach(field_model => {
					if(!field_model.workflowIds.includes(workflow_id)) {
						field_model.workflowIds.push(workflow_id);
					}
				});
				UI.Notify(
					'Workflow added',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `Workflow ${workflow_id} has been added to all field models of dataset model ${selected_dataset_model.id}`
					}
				);
			}
		);

		document.getElementById('dataset_model_all_field_models_not_workflow').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const workflow_id = /**@type {HTMLSelectElement}*/ (document.getElementById('dataset_model_field_model_workflows')).value;
				selected_dataset_model.fieldModels.forEach(field_model => {
					field_model.workflowIds.removeElement(workflow_id);
				});
				UI.Notify(
					'Workflow added',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `Workflow ${workflow_id} has been removed from all field models of dataset model ${selected_dataset_model.id}`
					}
				);
			}
		);

		//export
		document.getElementById('dataset_model_all_field_models_not_exportable').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				selected_dataset_model.fieldModels.forEach(f => f.exportable = false);
				UI.Notify('Field models are no longer exportable', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		);

		document.getElementById('dataset_model_all_field_models_exportable').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				selected_dataset_model.fieldModels.forEach(f => f.exportable = true);
				UI.Notify('Field models are now exportable', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		);

		document.getElementById('dataset_model_all_field_models_sort_according_to_form_model').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				let order = 0;

				//set order function
				function set_order(field_model) {
					field_model.exportOrder = order;
					order += sort_margin;
				}

				//retrieve all field model ids in dataset model
				const field_model_ids = selected_dataset_model.fieldModels.map(f => f.id);

				//retrieve all form models that may contain field models of the selected dataset model
				const dataset_model_form_models = [
					...selected_dataset_model.getEventModels()
						.filter(e => e.datasetModelIds.includes(selected_dataset_model.id))
						.flatMap(e => e.getFormModels()),
					...selected_dataset_model.getScopeModels()
						.filter(s => s.datasetModelIds.includes(selected_dataset_model.id))
						.flatMap(s => s.getFormModels())
				];

				//set export order according to what is found on form models
				const dataset_model_cells = dataset_model_form_models
					.flatMap(p => p.getCells())
					.filter(c => c.datasetModelId === selected_dataset_model.id && c.fieldModelId);

				dataset_model_cells.forEach(function(cell) {
					//a field model may appear more than once on form models
					//in this case, keep the order from the first time it has been found
					if(field_model_ids.includes(cell.fieldModelId)) {
						set_order(selected_dataset_model.getFieldModel(cell.fieldModelId));
						field_model_ids.removeElement(cell.fieldModelId);
					}
				});

				//retrieve unsorted field models
				const field_models = field_model_ids.map(DatasetModel.prototype.getFieldModel, selected_dataset_model);

				//set export order for dynamic unsorted field models
				field_models.filter(a => a.dynamic).forEach(set_order);

				//set export order for other unsorted field models
				field_models.filter(a => !a.dynamic).forEach(set_order);

				draw_field_models(selected_dataset_model);

				UI.Notify(
					'Field models sorted',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `All field models of dataset model ${selected_dataset_model.id} are now sorted according to the form models they appear on`
					}
				);
			}
		);

		document.getElementById('dataset_model_all_field_models_sort_alphabetically').addEventListener(
			'click',
			function(event) {
				event.preventDefault();

				//retrieve all field model ids in dataset model
				const field_model_ids = selected_dataset_model.fieldModels.map(a => a.id).sort();
				selected_dataset_model.fieldModels.forEach(f => f.exportOrder = field_model_ids.indexOf(f.id) * sort_margin);
				draw_field_models(selected_dataset_model);

				UI.Notify(
					'Field models sorted',
					{
						tag: 'info',
						icon: 'images/notifications_icons/done.svg',
						body: `All field models of dataset model ${selected_dataset_model.id} are now sorted alphabetically`
					}
				);
			}
		);

		Effects.Sortable(
			document.getElementById('dataset_model_field_models'),
			function() {
				let offset = 0;
				this.children.forEach(function(child) {
					selected_dataset_model.getFieldModel(child.dataset.fieldModelId).exportOrder = offset;
					child.querySelector('span').textContent = offset;
					offset += sort_margin;
				});
			}
		);
	},
	open: function(dataset_model) {
		selected_dataset_model = dataset_model;

		FormHelpers.FillSelect(document.getElementById('dataset_model_field_model_validators'), dataset_model.study.validators);
		FormHelpers.FillSelect(document.getElementById('dataset_model_field_model_workflows'), dataset_model.study.workflows);
		FormHelpers.FillLocalizedInput(document.getElementById('dataset_model_shortname'), dataset_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('dataset_model_longname'), dataset_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('dataset_model_description'), dataset_model.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_dataset_model_form'), dataset_model);

		FormStaticActions.DrawRules(dataset_model, dataset_model.deleteRules, dataset_model.constructor.RuleEntities, document.getElementById('dataset_model_delete_rules'), 'Removal rules');
		FormStaticActions.DrawRules(dataset_model, dataset_model.restoreRules, dataset_model.constructor.RuleEntities, document.getElementById('dataset_model_restore_rules'), 'Restoration rules');

		if(dataset_model.id) {
			document.getElementById('dataset_model_sql_create').textContent = dataset_model.createSQLCreate('\n');
			document.getElementById('dataset_model_sql_drop').textContent = dataset_model.createSQLDrop();
		}
		else {
			document.getElementById('dataset_model_sql_create').textContent = 'Not available yet';
			document.getElementById('dataset_model_sql_drop').textContent = 'Not available yet';
		}

		draw_field_models(dataset_model);
		manage_multiple();
		NodeTools.DrawUsage(dataset_model, document.getElementById('dataset_model_usage'));
	}
};
