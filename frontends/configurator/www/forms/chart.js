import {UI} from '../tools/ui.js';
import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {ChartRange} from '../model/config/entities/chart_range.js';

//specific cases
function toggle_section_type(section, type) {
	const types = Array.prototype.slice.call(arguments, 2);
	section.style.display = types.includes(type) ? 'block' : 'none';
}

/*function manage_event_model() {
	const event_model_id = document.getElementById('chart_event_model_id').value;
	if(event_model_id) {
		const event_model = selected_chart.study.getEventModel(event_model_id);
		FormHelpers.FillSelect(document.getElementById('chart_dataset_model_id'), event_model.getDatasetModels(), true);
		document.getElementById('chart_document').style.display = 'block';
	}
	else {
		document.getElementById('chart_dataset_model_id').value = '';
		document.getElementById('chart_dataset_model').style.display = 'none';
	}
}

function manage_dataset_model() {
	const dataset_model_id = document.getElementById('chart_dataset_model_id').value;
	if(dataset_model_id) {
		const dataset_model = selected_chart.study.getDatasetModel(dataset_model_id);
		FormHelpers.FillSelect(document.getElementById('chart_field_model_id'), dataset_model.fieldModels, true);
		document.getElementById('chart_field_model').style.display = 'block';
	}
	else {
		document.getElementById('chart_field_model_id').value = '';
		document.getElementById('chart_field_model').style.display = 'none';
	}
}*/

function manage_workflow(event) {
	const workflow_id = event ? this.value : selected_chart.workflowId;
	if(workflow_id) {
		const workflow = selected_chart.study.getWorkflow(workflow_id);
		FormHelpers.FillPalette(document.getElementById('chart_workflow_include_state_ids'), workflow.states);
		FormHelpers.FillPalette(document.getElementById('chart_workflow_exclude_state_ids'), workflow.states);
		document.getElementById('chart_workflow_states').style.display = 'block';
	}
	else {
		/**@type {AppPalette}*/ (document.getElementById('chart_workflow_include_state_ids')).value = '[]';
		/**@type {AppPalette}*/ (document.getElementById('chart_workflow_exclude_state_ids')).value = '[]';
		document.getElementById('chart_workflow_states').style.display = 'none';
	}
}

function manage_enrollment_workflow(event) {
	const workflow_id = event ? this.value : selected_chart.enrollmentWorkflowId;
	if(workflow_id) {
		const workflow = selected_chart.study.getWorkflow(workflow_id);
		FormHelpers.FillPalette(document.getElementById('chart_enrollment_state_ids'), workflow.states);
		document.getElementById('chart_enrollment_states').style.display = 'block';
	}
	else {
		/**@type {AppPalette}*/ (document.getElementById('chart_enrollment_state_ids')).value = '[]';
		document.getElementById('chart_enrollment_states').style.display = 'none';
	}
}

function manage_type(event) {
	const chart_type = Config.Enums.ChartType[event ? this.value : selected_chart.type];

	//do special initialization when type is modified
	if(event) {
		//not statistics
		if(chart_type !== Config.Enums.ChartType.STATISTICS) {
			//document.getElementById('chart_event_group_id').value = '';
			//document.getElementById('chart_event_model_id').value = '';
			//document.getElementById('chart_dataset_model_id').value = '';
			//document.getElementById('chart_attribute_id').value = '';
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_with_statistics')).checked = false;
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_use_percentile')).checked = false;
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_values_min')).value = '';
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_values_max')).value = '';
		}
		//not workflow status
		if(chart_type !== Config.Enums.ChartType.WORKFLOW_STATUS) {
			/**@type {HTMLSelectElement}*/ (document.getElementById('chart_workflow_id')).value = '';
			manage_workflow();
		}
		//not enrollment
		if(chart_type !== Config.Enums.ChartType.ENROLLMENT_BY_SCOPE && chart_type !== Config.Enums.ChartType.ENROLLMENT) {
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_leaf_scope_model_id')).value = '';
		}
		//not enrollment by scope
		if(chart_type !== Config.Enums.ChartType.ENROLLMENT_BY_SCOPE) {
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_scope_model_id')).value = '';
		}
		//not enrollment by date
		if(chart_type !== Config.Enums.ChartType.ENROLLMENT) {
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_display_expected')).checked = false;
			/**@type {HTMLInputElement}*/ (document.getElementById('chart_enrollment_workflow_id')).value = '';
			manage_enrollment_workflow();
		}
	}

	//retrieve some fields
	toggle_section_type(document.getElementById('chart_statistics'), chart_type, Config.Enums.ChartType.STATISTICS);
	toggle_section_type(document.getElementById('chart_workflow'), chart_type, Config.Enums.ChartType.WORKFLOW_STATUS);
	toggle_section_type(document.getElementById('chart_enrollment'), chart_type, Config.Enums.ChartType.ENROLLMENT, Config.Enums.ChartType.ENROLLMENT_BY_SCOPE);
	toggle_section_type(document.getElementById('chart_enrollment_by_scope'), chart_type, Config.Enums.ChartType.ENROLLMENT_BY_SCOPE);
	toggle_section_type(document.getElementById('chart_enrollment_by_date'), chart_type, Config.Enums.ChartType.ENROLLMENT);
}

function delete_range(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this entry?').then(confirmed => {
		if(confirmed) {
			const range_tr = this.parentNode.parentNode;
			range_tr.parentNode.removeChild(range_tr);
		}
	});
}

function draw_range(range) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('chart_range')).content, true);

	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="id"]')).value = range.id || '';
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="labels"]'), range.chart.study.languages, range.labels);
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="value"]')).value = range.value !== undefined ? range.value : '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="min"]')).value = range.min !== undefined ? range.min : '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="max"]')).value = range.max !== undefined ? range.max : '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="other"]')).checked = range.other;
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="show"]')).checked = range.show;
	instance.querySelector('button').addEventListener('click', delete_range);

	return instance;
}

let selected_chart;

export default {
	form: 'edit_chart_form',
	init: function() {
		document.getElementById('edit_chart_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_chart, this['id'].value)) {
					FormHelpers.UpdateObject(selected_chart, this);

					selected_chart.ranges = document.querySelectorAll('#chart_ranges > tbody > tr').map(function(chart_range) {
						const range = new ChartRange();
						range.id = chart_range.querySelector('input[data-name="id"]').value;
						range.labels = JSON.parse(chart_range.querySelector('app-localized-input[data-name="labels"]').value);
						range.value = chart_range.querySelector('input[data-name="value"]').value;
						const range_min = chart_range.querySelector('input[data-name="min"]').value;
						range.min = range_min !== '' ? parseFloat(range_min) : undefined;
						const range_max = chart_range.querySelector('input[data-name="max"]').value;
						range.max = range_max !== '' ? parseFloat(range_max) : undefined;
						range.other = chart_range.querySelector('input[data-name="other"]').checked;
						range.show = chart_range.querySelector('input[data-name="show"]').checked;
						range.chart = selected_chart;
						return range;
					});
					const chart_request = /**@type {HTMLTextAreaElement}*/ (document.getElementById('chart_request')).value;
					selected_chart.request = chart_request ? JSON.parse(chart_request) : {};
					FormStaticActions.AfterSubmission(selected_chart);
				}
			}
		);

		document.getElementById('chart_ranges_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const range = new ChartRange();
				range.chart = selected_chart;
				document.querySelector('#chart_ranges > tbody').appendChild(draw_range(range));
			}
		);

		document.getElementById('chart_type').addEventListener('change', manage_type);
		//document.getElementById('chart_event_group_id').addEventListener('change', manage_event_group);
		//document.getElementById('chart_event_model_id').addEventListener('change', manage_event_model);
		//document.getElementById('chart_dataset_model_id').addEventListener('change', manage_dataset_model);
		document.getElementById('chart_workflow_id').addEventListener('change', manage_workflow);
		document.getElementById('chart_enrollment_workflow_id').addEventListener('change', manage_enrollment_workflow);
	},
	open: function(chart) {
		selected_chart = chart;

		FormHelpers.FillSelectEnum(document.getElementById('chart_type'), Config.Enums.ChartType);

		//different type of charts
		//FormHelpers.FillSelect(document.getElementById('chart_event_group_id'), chart.study.eventGroups, true);
		FormHelpers.FillSelect(document.getElementById('chart_workflow_id'), chart.study.workflows, true);
		FormHelpers.FillSelect(document.getElementById('chart_enrollment_workflow_id'), chart.study.workflows, true);
		FormHelpers.FillSelect(document.getElementById('chart_scope_model_id'), chart.study.scopeModels, true);
		FormHelpers.FillSelect(document.getElementById('chart_leaf_scope_model_id'), chart.study.scopeModels, true);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_shortname'), chart.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_longname'), chart.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_description'), chart.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_title'), chart.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_legend_x_label'), chart.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('chart_legend_y_label'), chart.study.languages);

		manage_type();
		//manage_event_group();
		//manage_event();
		//manage_document();
		manage_workflow();
		manage_enrollment_workflow();

		FormHelpers.UpdateForm(document.getElementById('edit_chart_form'), chart);

		FormHelpers.EnhanceInputSimpleListString(document.getElementById('chart_colors'));

		/**@type {HTMLTextAreaElement}*/ (document.getElementById('chart_request')).value = JSON.stringify(chart.request || {});

		chart.ranges.map(draw_range).forEach(Node.prototype.appendChild, document.querySelector('#chart_ranges > tbody').empty('tr'));
	}
};
