import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

function manage_field_models(event) {
	const dataset_model_id = event ? this.value : selected_report.datasetModelId;
	const dataset_model = selected_report.study.getDatasetModel(dataset_model_id);
	FormHelpers.FillPalette(document.getElementById('report_field_model_ids'), dataset_model.fieldModels);
}

let selected_report;

export default {
	form: 'edit_report_form',
	init: function() {
		document.getElementById('edit_report_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('report_dataset_model_id').addEventListener('change', manage_field_models);
	},
	open: function(report) {
		selected_report = report;

		FormHelpers.FillSelect(document.getElementById('report_workflow_id'), report.study.workflows);
		FormHelpers.FillSelect(document.getElementById('report_dataset_model_id'), report.study.datasetModels);
		FormHelpers.FillLocalizedInput(document.getElementById('report_shortname'), report.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('report_longname'), report.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('report_description'), report.study.languages);
		manage_field_models();
		FormHelpers.UpdateForm(document.getElementById('edit_report_form'), report);
	}
};
