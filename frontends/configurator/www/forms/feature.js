import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_feature_form',
	init: function() {
		document.getElementById('edit_feature_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(feature) {
		FormHelpers.FillLocalizedInput(document.getElementById('feature_shortname'), feature.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('feature_longname'), feature.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('feature_description'), feature.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_feature_form'), feature);
	}
};
