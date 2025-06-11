import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

export default {
	form: 'edit_workflow_state_form',
	init: function() {
		document.getElementById('edit_workflow_state_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(workflow_state) {
		let aggregate_states = [];
		if(workflow_state.workflow.aggregateWorkflowId) {
			const aggregate_workflow = workflow_state.workflow.study.getWorkflow(workflow_state.workflow.aggregateWorkflowId);
			aggregate_states = aggregate_workflow.states.slice();
			document.getElementById('workflow_state_aggregate').style.display = 'block';
		}
		else {
			document.getElementById('workflow_state_aggregate').style.display = 'none';
		}
		FormHelpers.FillSelect(document.getElementById('workflow_state_aggregate_state_id'), aggregate_states, true);
		FormHelpers.FillSelectEnum(document.getElementById('workflow_state_aggregate_state_matcher'), Config.Enums.WorkflowStateMatcher, false);
		FormHelpers.FillPalette(document.getElementById('workflow_state_actions_ids'), workflow_state.workflow.actions);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_state_shortname'), workflow_state.workflow.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_state_longname'), workflow_state.workflow.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('workflow_state_description'), workflow_state.workflow.study.languages);

		FormHelpers.UpdateForm(document.getElementById('edit_workflow_state_form'), workflow_state);
	}
};
