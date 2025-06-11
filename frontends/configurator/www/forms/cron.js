import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_cron_form',
	init: function() {
		document.getElementById('edit_cron_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(cron) {
		FormHelpers.FillSelectEnum(document.getElementById('cron_interval_unit'), Config.Enums.EventTimeUnit, true);
		FormHelpers.FillLocalizedInput(document.getElementById('cron_description'), cron.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_cron_form'), cron);

		FormStaticActions.DrawRules(cron, cron.rules, cron.constructor.RuleEntities, document.getElementById('cron_rules'));
	}
};
