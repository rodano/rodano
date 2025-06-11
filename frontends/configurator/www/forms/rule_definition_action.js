import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {NodeTools} from '../node_tools.js';
import {Entities} from '../model/config/entities.js';
import {RuleDefinitionActionParameter} from '../model/config/entities/rule_definition_action_parameter.js';

function delete_parameter(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this parameter?').then(confirmed => {
		if(confirmed) {
			const parameter_div = this.parentNode.parentNode;
			parameter_div.parameter.delete();
			parameter_div.parentNode.removeChild(parameter_div);
		}
	});
}

function draw_parameter(parameter) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('rule_definition_action_parameter')).content, true);

	instance.querySelector('div').parameter = parameter;
	instance.querySelector('button').addEventListener('click', delete_parameter);
	instance.querySelector('span').textContent = parameter.id || '';

	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="id"]')).value = parameter.id || '';
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="label"]')).value = parameter.label || '';
	FormHelpers.FillSelectEnum(instance.querySelector('select[data-name="dataEntity"]'), Config.Enums.RuleEntities, true, parameter.dataEntity);
	/**@type {HTMLSelectElement}*/ (instance.querySelector('select[data-name="configurationEntity"]')).fill(Object.keys(Entities), true, parameter.configurationEntity);
	/**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="options"]')).value = parameter.options ? JSON.stringify(parameter.options) : '';

	return instance;
}

let selected_rule_definition_action;

export default {
	form: 'edit_rule_definition_action_form',
	init: function() {
		document.getElementById('edit_rule_definition_action_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_rule_definition_action, this['id'].value)) {
					FormHelpers.UpdateObject(selected_rule_definition_action, this);

					document.querySelectorAll('#rule_definition_action_parameters > div').forEach(function(parameter_div) {
						const parameter = parameter_div.parameter;
						parameter.id = parameter_div.querySelector('input[data-name="id"]').value;
						parameter.label = parameter_div.querySelector('input[data-name="label"]').value;
						parameter.dataEntity = parameter_div.querySelector('select[data-name="dataEntity"]').value;
						parameter.configurationEntity = parameter_div.querySelector('select[data-name="configurationEntity"]').value;
						const parameter_options = parameter_div.querySelector('input[data-name="options"]').value;
						parameter.options = parameter_options.value ? JSON.parse(parameter_options) : undefined;
					});

					FormStaticActions.AfterSubmission(selected_rule_definition_action);
				}
			}
		);

		document.getElementById('rule_definition_action_parameters_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const parameter = new RuleDefinitionActionParameter();
				selected_rule_definition_action.parameters.push(parameter);
				document.getElementById('rule_definition_action_parameters').appendChild(draw_parameter(parameter));
			}
		);

		Effects.Sortable(
			document.getElementById('rule_definition_action_parameters'),
			function() {
				selected_rule_definition_action.parameters = this.querySelectorAll(':scope > div').map(c => c.parameter);
			},
			'h3 > img:first-child',
			undefined,
			'div'
		);
	},
	open: function(rule_definition_action) {
		selected_rule_definition_action = rule_definition_action;

		FormHelpers.FillSelectEnum(document.getElementById('rule_definition_action_entity'), Config.Enums.RuleEntities);
		FormHelpers.UpdateForm(document.getElementById('edit_rule_definition_action_form'), rule_definition_action);

		rule_definition_action.parameters.map(draw_parameter).forEach(Node.prototype.appendChild, document.getElementById('rule_definition_action_parameters').empty('div'));

		NodeTools.DrawUsage(rule_definition_action, document.getElementById('rule_definition_action_usage'));
	}
};
