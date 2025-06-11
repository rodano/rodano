import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {WorkflowStatesSelector} from '../model/config/entities/workflow_states_selector.js';
import {WorkflowWidgetColumn} from '../model/config/entities/workflow_widget_column.js';

function delete_selector(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this selector?').then(confirmed => {
		if(confirmed) {
			const selector_div = this.parentNode.parentNode;
			selector_div.selector.delete();
			selector_div.parentNode.removeChild(selector_div);
		}
	});
}

function draw_selector(selector) {
	function manage_workflow(event) {
		const workflow_id = event ? workflow_select.value : selector.workflowId;
		if(workflow_id) {
			FormHelpers.FillPalette(states_palette, selected_workflow_widget.study.getWorkflow(workflow_id).states);
		}
	}

	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('workflow_widget_states_selector')).content, true);

	instance.querySelector('div').selector = selector;
	instance.querySelector('button').addEventListener('click', delete_selector);

	const workflow_select = /**@type {HTMLSelectElement}*/ (instance.querySelector('select[data-name="workflowId"]'));
	FormHelpers.FillSelect(workflow_select, selected_workflow_widget.study.workflows, true);
	workflow_select.value = selector.workflowId || '';
	workflow_select.addEventListener('change', manage_workflow);

	const states_palette = /**@type {AppPalette}*/ (instance.querySelector('app-palette[data-name="stateIds"]'));
	manage_workflow();
	states_palette.setValues(selector.stateIds || []);

	return instance;
}

function delete_column(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this column?').then(confirmed => {
		if(confirmed) {
			const column_div = this.parentNode.parentNode;
			column_div.column.delete();
			column_div.parentNode.removeChild(column_div);
		}
	});
}

function get_column_types(entity) {
	const column_types = {};
	if(entity) {
		const available_entities = Config.Enums.WorkflowEntities[entity].container_entities.map(e => Config.Enums.WorkflowEntities[e]);
		for(const [type, column_type] of Object.entries(Config.Enums.WorkflowWidgetColumnType)) {
			//add generic columns
			if(!column_type.workflow_entity) {
				column_types[type] = column_type;
			}
			//add entity specific columns
			else if(available_entities.includes(column_type.workflow_entity)) {
				column_types[type] = column_type;
			}
		}
	}
	return column_types;
}

function draw_column(column, index) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('workflow_widget_column')).content, true);

	instance.querySelector('div').column = column;
	instance.querySelector('span').textContent = `Column n°${index + 1}`;
	instance.querySelector('button').addEventListener('click', delete_column);

	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="id"]')).value = column.id || '';
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="shortname"]'), column.widget.study.languages, column.shortname);
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="longname"]'), column.widget.study.languages, column.longname);
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="description"]'), column.widget.study.languages, column.description);
	const column_types = get_column_types(/**@type {HTMLSelectElement}*/ (document.getElementById('workflow_widget_workflow_entity')).value);
	FormHelpers.FillSelectEnum(instance.querySelector('select[data-name="type"]'), column_types, true, column.type);
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="width"]')).value = column.width || '';

	return instance;
}

let selected_workflow_widget;

export default {
	form: 'edit_workflow_widget_form',
	init: function() {
		document.getElementById('edit_workflow_widget_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_workflow_widget, this['id'].value)) {
					FormHelpers.UpdateObject(selected_workflow_widget, this);

					document.querySelectorAll('#workflow_widget_workflow_states_selectors > div').forEach(function(selector_div) {
						const selector = selector_div.selector;
						selector.workflowId = selector_div.querySelector('select[data-name="workflowId"]').value || undefined;
						selector.stateIds = selector_div.querySelector('app-palette[data-name="stateIds"]').getValues();
					});

					document.querySelectorAll('#workflow_widget_columns > div').forEach(function(column_div) {
						const column = column_div.column;
						column.id = column_div.querySelector('input[data-name="id"]').value || undefined;
						column.shortname = JSON.parse(column_div.querySelector('app-localized-input[data-name="shortname"]').value);
						column.longname = JSON.parse(column_div.querySelector('app-localized-input[data-name="longname"]').value);
						column.description = JSON.parse(column_div.querySelector('app-localized-input[data-name="description"]').value);
						column.type = column_div.querySelector('select[data-name="type"]').value;
						const column_width = column_div.querySelector('input[data-name="width"]').value;
						column.width = column_width ? parseInt(column_width) : undefined;
					});

					FormStaticActions.AfterSubmission(selected_workflow_widget);
				}
			}
		);

		document.getElementById('workflow_widget_workflow_entity').addEventListener(
			'change',
			function() {
				const column_types = get_column_types(this.value);
				document.querySelectorAll('#workflow_widget_columns > div').forEach(column_div => {
					const column = column_div.column;
					FormHelpers.FillSelectEnum(column_div.querySelector('select[data-name="type"]'), column_types, true, column.type);
				});
			}
		);

		document.getElementById('workflow_widget_workflow_states_selector_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const selector = new WorkflowStatesSelector();
				selector.parent = selected_workflow_widget;
				selected_workflow_widget.workflowStatesSelectors.push(selector);
				document.getElementById('workflow_widget_workflow_states_selectors').appendChild(draw_selector(selector));
			}
		);

		document.getElementById('workflow_widget_column_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const column = new WorkflowWidgetColumn();
				column.widget = selected_workflow_widget;
				selected_workflow_widget.columns.push(column);
				document.getElementById('workflow_widget_columns').appendChild(draw_column(column, selected_workflow_widget.columns.length - 1));
			}
		);

		Effects.Sortable(
			document.getElementById('workflow_widget_columns'),
			function() {
				selected_workflow_widget.columns = this.querySelectorAll(':scope > div').map(c => c.column);
			},
			'h3 > img:first-child',
			undefined,
			'div'
		);
	},
	open: function(workflow_widget) {
		selected_workflow_widget = workflow_widget;

		FormHelpers.FillLocalizedInput(document.getElementById('workflow_widget_shortname'), workflow_widget.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_widget_longname'), workflow_widget.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_widget_description'), workflow_widget.study.languages);
		FormHelpers.FillSelectEnum(document.getElementById('workflow_widget_workflow_entity'), Config.Enums.WorkflowEntities);
		FormHelpers.UpdateForm(document.getElementById('edit_workflow_widget_form'), workflow_widget);

		workflow_widget.workflowStatesSelectors.map(draw_selector).forEach(Node.prototype.appendChild, document.getElementById('workflow_widget_workflow_states_selectors').empty('div'));
		workflow_widget.columns.map(draw_column).forEach(Node.prototype.appendChild, document.getElementById('workflow_widget_columns').empty('div'));
	}
};
