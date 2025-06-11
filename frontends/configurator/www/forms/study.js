import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_study_form',
	init: function() {
		document.getElementById('edit_study_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(study) {
		FormHelpers.FillSelect(document.getElementById('study_default_language_id'), study.languages);
		FormHelpers.FillPalette(document.getElementById('study_language_ids'), study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('study_shortname'), study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('study_longname'), study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('study_description'), study.languages);
		FormHelpers.FillSelect(document.getElementById('study_epro_profile_id'), study.profiles);
		FormHelpers.UpdateForm(document.getElementById('edit_study_form'), study);
	}
};
