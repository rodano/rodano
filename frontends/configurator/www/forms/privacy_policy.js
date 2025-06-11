import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_privacy_policy_form',
	init: function() {
		document.getElementById('edit_privacy_policy_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(privacy_policy) {
		FormHelpers.FillPalette(document.getElementById('privacy_policy_profile_ids'), privacy_policy.study.profiles);
		FormHelpers.FillLocalizedInput(document.getElementById('privacy_policy_shortname'), privacy_policy.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('privacy_policy_longname'), privacy_policy.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('privacy_policy_description'), privacy_policy.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('privacy_policy_content'), privacy_policy.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_privacy_policy_form'), privacy_policy);
	}
};
