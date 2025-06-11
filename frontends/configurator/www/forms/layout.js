import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {Router} from '../router.js';

function manage_multiple(event) {
	const layout_type_id = event ? this.value : selected_layout.type;
	const layout_type = Config.Enums.LayoutType[layout_type_id];

	const layout_dataset_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('layout_dataset_model_id'));
	const layout_default_sort_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('layout_default_sort_field_model_id'));

	if(layout_type.has_dataset_model) {
		layout_dataset_model_id.parentElement.style.display = 'block';
		layout_default_sort_field_model_id.parentElement.style.display = 'block';
	}
	else {
		//reset and hide fields
		layout_dataset_model_id.value = '';
		layout_dataset_model_id.parentElement.style.display = 'none';
		layout_default_sort_field_model_id.value = '';
		layout_default_sort_field_model_id.parentElement.style.display = 'none';
	}
}

function manage_dataset_model(event) {
	const dataset_model_id = event ? this.value : selected_layout.datasetModelId;
	const layout_default_sort_field_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('layout_default_sort_field_model_id'));
	if(dataset_model_id) {
		const field_models = selected_layout.formModel.study.getDatasetModel(dataset_model_id).fieldModels;
		FormHelpers.FillSelect(layout_default_sort_field_model_id, field_models);
		layout_default_sort_field_model_id.parentElement.style.display = 'block';
	}
	else {
		layout_default_sort_field_model_id.value = '';
		layout_default_sort_field_model_id.parentElement.style.display = 'none';
	}
}

function close_form() {
	Router.SelectNode(selected_layout.formModel, 'edit_form_model_content');
}

let selected_layout;

export default {
	form: 'edit_layout_form',
	init: function() {
		document.getElementById('edit_layout_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_layout, this['id'].value)) {
					FormHelpers.UpdateObject(selected_layout, this);
					FormStaticActions.AfterSubmission(selected_layout);
					close_form();
				}
			}
		);

		document.getElementById('edit_layout_close').addEventListener('click', close_form);

		document.getElementById('layout_type').addEventListener('change', manage_multiple);

		document.getElementById('layout_dataset_model_id').addEventListener('change', manage_dataset_model);

		FormStaticActions.ManageConstraintEdition('constraint', document.getElementById('layout_constraint_add'), document.getElementById('layout_constraint_edit'), document.getElementById('layout_constraint_delete'));
	},
	open: function(layout) {
		selected_layout = layout;

		FormHelpers.FillSelectEnum(document.getElementById('layout_type'), Config.Enums.LayoutType);
		FormHelpers.FillSelect(document.getElementById('layout_dataset_model_id'), layout.formModel.study.datasetModels, true);
		FormHelpers.FillLocalizedInput(document.getElementById('layout_description'), layout.formModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('layout_text_before'), layout.formModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('layout_text_after'), layout.formModel.study.languages);
		manage_multiple();
		manage_dataset_model();
		FormHelpers.UpdateForm(document.getElementById('edit_layout_form'), layout);

		FormStaticActions.UpdateConstraintEdition(layout, 'constraint', document.getElementById('layout_constraint_add'), document.getElementById('layout_constraint_edit'), document.getElementById('layout_constraint_delete'));
	}
};
