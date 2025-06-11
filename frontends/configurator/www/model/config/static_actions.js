import {Entities} from './entities.js';
import {RuleEntities} from './rule_entities.js';

export const StaticActions = Object.freeze({
	LOG: {
		label: 'Log',
		parameters: [
			{
				id: 'TEXT',
				label: 'Text',
				type: 'TEXT'
			}
		]
	},
	EMAIL: {
		label: 'Send e-mail',
		parameters: [
			{
				id: 'SCOPES',
				label: 'Scopes in email',
				type: 'STRING',
				dataEntity: 'SCOPE',
				optional: true
			},
			{
				id: 'EVENTS',
				label: 'Events in email',
				type: 'STRING',
				dataEntity: 'EVENT',
				optional: true
			},
			{
				id: 'FORMS',
				label: 'Forms in email',
				type: 'STRING',
				dataEntity: 'FORM',
				optional: true
			},
			{
				id: 'WORKFLOWS',
				label: 'Workflows in email',
				type: 'STRING',
				dataEntity: 'WORKFLOW',
				optional: true
			},
			{
				id: 'FIELDS',
				label: 'Fields in email',
				type: 'STRING',
				dataEntity: 'FIELD',
				optional: true
			},
			{
				id: 'FEATURE_ID',
				label: 'Recipients feature',
				type: 'STRING',
				configurationEntity: 'Feature'
			},
			{
				id: 'RECIPIENTS_SCOPE',
				label: 'Recipients scopes',
				type: 'STRING',
				dataEntity: 'SCOPE',
				optional: true
			},
			{
				id: 'BRANCH_TYPE',
				label: 'Branch',
				type: 'STRING',
				options: ['NONE', 'DESCENDANTS', 'ANCESTORS', 'BRANCH', 'ALL']
			},
			{
				id: 'SUBJECT',
				label: 'Subject',
				type: 'STRING'
			},
			{
				id: 'CONTENT_TEXT',
				label: 'Text Content',
				type: 'TEXT'
			},
			{
				id: 'CONTENT_HTML',
				label: 'Html Content',
				type: 'TEXT',
				optional: true
			},
			{
				id: 'INTENT',
				label: 'Intent',
				type: 'STRING'
			}
		]
	}
});

function getConfigurationEntity() {
	return Entities[this.configurationEntity];
}
function getDataEntity() {
	return RuleEntities[this.dataEntity];
}
function getParameter(parameter_id) {
	if(!this.parameters) {
		throw new Error('No parameter for action');
	}
	const parameter = this.parameters.find(p => p.id === parameter_id);
	if(!this.parameters) {
		throw new Error(`No parameter with id ${parameter_id}`);
	}
	return parameter;
}

Object.values(StaticActions).forEach(function(action) {
	action.getParameter = getParameter;
	if(action.parameters) {
		action.parameters.forEach(function(parameter) {
			parameter.getConfigurationEntity = getConfigurationEntity;
			parameter.getDataEntity = getDataEntity;
		});
	}
});
