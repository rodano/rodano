import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_resource_category_form',
	init: function() {
		document.getElementById('edit_resource_category_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(resource_category) {
		FormHelpers.FillLocalizedInput(document.getElementById('resource_category_shortname'), resource_category.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('resource_category_longname'), resource_category.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('resource_category_description'), resource_category.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_resource_category_form'), resource_category);
	}
};
