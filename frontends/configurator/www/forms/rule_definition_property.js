import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {NodeTools} from '../node_tools.js';
import {Entities} from '../model/config/entities.js';

export default {
	form: 'edit_rule_definition_property_form',
	init: function() {
		document.getElementById('edit_rule_definition_property_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);
	},
	open: function(rule_definition_property) {
		FormHelpers.FillSelectEnum(document.getElementById('rule_definition_property_entity'), Config.Enums.RuleEntities);
		FormHelpers.FillSelectEnum(document.getElementById('rule_definition_property_type'), Config.Enums.DataType);
		FormHelpers.FillSelectEnum(document.getElementById('rule_definition_property_target'), Config.Enums.RuleEntities);
		/**@type {HTMLSelectElement}*/ (document.getElementById('rule_definition_property_configuration_entity')).fill(Object.keys(Entities), false, rule_definition_property.configurationEntity);
		FormHelpers.UpdateForm(document.getElementById('edit_rule_definition_property_form'), rule_definition_property);

		NodeTools.DrawUsage(rule_definition_property, document.getElementById('rule_definition_property_usage'));
	}
};
