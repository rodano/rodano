import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {NodeTools} from '../node_tools.js';

export default {
	form: 'edit_event_group_form',
	init: function() {
		document.getElementById('edit_event_group_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(event_group) {
		FormHelpers.FillLocalizedInput(document.getElementById('event_group_shortname'), event_group.scopeModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('event_group_longname'), event_group.scopeModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('event_group_description'), event_group.scopeModel.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_event_group_form'), event_group);

		NodeTools.DrawUsage(event_group, document.getElementById('event_group_usage'));
	}
};
