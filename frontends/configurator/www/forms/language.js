import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_language_form',
	init: function() {
		document.getElementById('edit_language_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(language) {
		FormHelpers.FillLocalizedInput(document.getElementById('language_shortname'), language.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('language_longname'), language.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('language_description'), language.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_language_form'), language);
	}
};
