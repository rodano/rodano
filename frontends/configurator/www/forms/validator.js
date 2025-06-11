import {ConfigHelpers} from '../model_config.js';
import {Languages} from '../languages.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

function manage_workflow_states(event) {
	const workflow_id = event ? this.value : selected_validator.workflowId;
	const workflow_states_container = document.getElementById('validator_workflow_states_container');

	const validator_workflow_invalid_state_id = /**@type {HTMLSelectElement}*/ (document.getElementById('validator_workflow_invalid_state_id'));
	const validator_workflow_valid_state_id = /**@type {HTMLSelectElement}*/ (document.getElementById('validator_workflow_valid_state_id'));

	if(workflow_id) {
		const workflow_states = selected_validator.study.getWorkflow(workflow_id).states;
		FormHelpers.FillSelect(validator_workflow_invalid_state_id, workflow_states, true);
		FormHelpers.FillSelect(validator_workflow_valid_state_id, workflow_states, true);
		validator_workflow_invalid_state_id.setAttribute('required', 'required');
		validator_workflow_valid_state_id.setAttribute('required', 'required');
		workflow_states_container.style.display = 'block';
	}
	else {
		//reset and hide fields
		validator_workflow_invalid_state_id.value = '';
		validator_workflow_valid_state_id.value = '';
		validator_workflow_invalid_state_id.removeAttribute('required');
		validator_workflow_valid_state_id.removeAttribute('required');
		workflow_states_container.style.display = 'none';
	}
}

let selected_validator;

export default {
	form: 'edit_validator_form',
	init: function() {
		document.getElementById('edit_validator_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('validator_workflow_id').addEventListener('change', manage_workflow_states);

		document.getElementById('validator_copy_rules').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const validator_id = /**@type {HTMLSelectElement}*/ (document.getElementById('validator_copy_rules_validators')).value;
				const validator = selected_validator.study.getValidator(validator_id);
				const constraint = ConfigHelpers.CloneNode(validator.constraint);
				constraint.constrainable = selected_validator;
				selected_validator.constraint = constraint;
				FormStaticActions.UpdateConstraintEdition(selected_validator, 'constraint', document.getElementById('validator_constraint_add'), document.getElementById('validator_constraint_edit'), document.getElementById('validator_constraint_delete'));
			}
		);

		FormStaticActions.ManageConstraintEdition('constraint', document.getElementById('validator_constraint_add'), document.getElementById('validator_constraint_edit'), document.getElementById('validator_constraint_delete'));
	},
	open: function(validator) {
		selected_validator = validator;

		const rules_validators = validator.study.validators.filter(v => v !== validator && v.constraint);
		rules_validators.sort(validator.constructor.getComparator(Languages.GetLanguage()));
		FormHelpers.FillSelect(document.getElementById('validator_copy_rules_validators'), rules_validators);
		FormHelpers.FillSelect(document.getElementById('validator_workflow_id'), validator.study.workflows, true);
		FormHelpers.FillLocalizedInput(document.getElementById('validator_shortname'), validator.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('validator_longname'), validator.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('validator_description'), validator.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('validator_message'), validator.study.languages);
		manage_workflow_states();
		FormHelpers.UpdateForm(document.getElementById('edit_validator_form'), validator);

		FormStaticActions.UpdateConstraintEdition(validator, 'constraint', document.getElementById('validator_constraint_add'), document.getElementById('validator_constraint_edit'), document.getElementById('validator_constraint_delete'));

		NodeTools.DrawUsage(validator, document.getElementById('validator_usage'));
	}
};
