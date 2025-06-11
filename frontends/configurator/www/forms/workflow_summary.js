import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {WorkflowSummaryColumn} from '../model/config/entities/workflow_summary_column.js';

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

function draw_column(column, index) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('workflow_summary_column')).content, true);

	instance.querySelector('div').column = column;
	instance.querySelector('span').textContent = `Column n°${index + 1}`;
	instance.querySelector('button').addEventListener('click', delete_column);

	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="label"]'), column.summary.study.languages, column.label);
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="description"]'), column.summary.study.languages, column.description);

	const column_total = /**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="total"]'));
	const column_state_ids = instance.querySelector('app-palette[data-name="stateIds"]');

	column_total.checked = column.total;
	function update_on_total_change() {
		column_state_ids.parentElement.parentElement.style.display = column_total.checked ? 'none' : 'block';
	}
	column_total.addEventListener('change', update_on_total_change);
	update_on_total_change();

	const workflow_ids = /**@type {AppPalette}*/ (document.getElementById('workflow_summary_workflow_ids')).getValues();
	const reference_workflow = column.summary.study.getWorkflow(workflow_ids[0]);
	FormHelpers.FillPalette(column_state_ids, reference_workflow.states, undefined, undefined, column.stateIds);

	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="percent"]')).checked = column.percent;
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="nonNullColor"]')).value = column.nonNullColor || '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="nonNullBackgroundColor"]')).value = column.nonNullBackgroundColor || '';

	return instance;
}

let selected_workflow_summary;

export default {
	form: 'edit_workflow_summary_form',
	init: function() {
		document.getElementById('edit_workflow_summary_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_workflow_summary, this['id'].value)) {
					FormHelpers.UpdateObject(selected_workflow_summary, this);

					document.querySelectorAll('#workflow_summary_columns > div').forEach(function(column_div) {
						const column = column_div.column;
						column.label = JSON.parse(column_div.querySelector('app-localized-input[data-name="label"]').value);
						column.description = JSON.parse(column_div.querySelector('app-localized-input[data-name="description"]').value);
						column.total = column_div.querySelector('input[data-name="total"]').checked;
						const column_state_ids = column_div.querySelector('app-palette[data-name="stateIds"]').value;
						column.stateIds = column_state_ids ? JSON.parse(column_state_ids) : [];
						column.percent = column_div.querySelector('input[data-name="percent"]').checked;
						column.nonNullColor = column_div.querySelector('input[data-name="nonNullColor"]').value || undefined;
						column.nonNullBackgroundColor = column_div.querySelector('input[data-name="nonNullBackgroundColor"]').value || undefined;
					});

					FormStaticActions.AfterSubmission(selected_workflow_summary);
				}
			}
		);

		document.getElementById('workflow_summary_column_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				if(/**@type {HTMLSelectElement}*/ (document.getElementById('workflow_summary_workflow_entity')).value) {
					const column = new WorkflowSummaryColumn();
					column.summary = selected_workflow_summary;
					selected_workflow_summary.columns.push(column);
					document.getElementById('workflow_summary_columns').appendChild(draw_column(column, selected_workflow_summary.columns.length - 1));
				}
				else {
					UI.Notify('Select a workflow', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			}
		);

		Effects.Sortable(
			document.getElementById('workflow_summary_columns'),
			function() {
				selected_workflow_summary.columns = this.querySelectorAll(':scope > div').map(c => c.column);
			},
			'h3 > img:first-child',
			undefined,
			'div'
		);
	},
	open: function(workflow_summary) {
		selected_workflow_summary = workflow_summary;

		FormHelpers.FillSelectEnum(document.getElementById('workflow_summary_workflow_entity'), Config.Enums.WorkflowEntities);
		FormHelpers.FillPalette(document.getElementById('workflow_summary_workflow_ids'), workflow_summary.study.workflows);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_summary_title'), workflow_summary.study.languages);
		FormHelpers.FillSelect(document.getElementById('workflow_summary_leaf_scope_model_id'), workflow_summary.study.scopeModels, true);
		FormHelpers.FillPalette(document.getElementById('workflow_summary_filter_event_model_ids'), workflow_summary.study.getEventModels());
		FormHelpers.UpdateForm(document.getElementById('edit_workflow_summary_form'), workflow_summary);

		workflow_summary.columns.map(draw_column).forEach(Node.prototype.appendChild, document.getElementById('workflow_summary_columns').empty('div'));
	}
};
