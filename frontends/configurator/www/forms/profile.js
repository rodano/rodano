import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_profile_form',
	init: function() {
		document.getElementById('edit_profile_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(profile) {
		FormHelpers.FillLocalizedInput(document.getElementById('profile_shortname'), profile.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('profile_longname'), profile.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('profile_description'), profile.study.languages);
		FormHelpers.FillSelect(document.getElementById('profile_workflow_id_of_interest'), profile.study.workflows, true);
		FormHelpers.UpdateForm(document.getElementById('edit_profile_form'), profile);
	}
};
