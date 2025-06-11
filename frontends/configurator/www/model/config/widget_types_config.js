import {Entities} from './entities.js';

export const WidgetTypes = Object.freeze({
	ACTIVITY_LOG: {
		label: 'Activity log',
		parameters: [
			{
				id: 'displayAddResponse',
				label: 'Display add response',
				type: 'BOOLEAN'
			},
			{
				id: 'removeProfileSelector',
				label: 'Remove profile selector',
				type: 'BOOLEAN'
			}
		]
	},
	WORKFLOW: {
		label: 'Workflow',
		parameters: [
			{
				id: 'workflow',
				label: 'Workflow',
				type: 'STRING',
				entity: Entities.WorkflowWidget.name
			}
		]
	},
	WORKFLOWS_SUMMARY: {
		label: 'Workflow summary',
		parameters: [
			{
				id: 'summary',
				label: 'Summary',
				type: 'STRING',
				entity: Entities.WorkflowSummary.name
			}
		]
	},
	SCOPE_EDITION: {
		label: 'Scope edition'
	},
	DOCUMENT: {
		label: 'Document edition',
		parameters: [
			{
				id: 'event',
				label: 'Event model',
				type: 'STRING',
				entity: Entities.EventModel.name
			},
			{
				id: 'page',
				label: 'Form model',
				type: 'STRING',
				entity: Entities.FormModel.name
			}
		]
	},
	CONTACTS: {
		label: 'Contacts'
	},
	WELCOME_TEXT: {
		label: 'Welcome text'
	},
	GENERAL_INFORMATIONS: {
		label: 'General information'
	},
	SCOPE_OVERDUE: {
		label: 'Scope overdue',
		parameters: [
			{
				id: 'OVERDUE_TYPE',
				label: 'Overdue type',
				type: 'STRING',
			},
			{
				id: 'SPECIFIC_COLUMN_NAME',
				label: 'Specific column name',
				type: 'STRING'
			}
		]
	},
	EVENT_SUMMARY: {
		label: 'Event summary',
		parameters: [
			{
				id: 'SCOPE',
				label: 'Scope model',
				type: 'STRING',
				entity: Entities.ScopeModel.name
			},
			{
				id: 'DEPTH',
				label: 'Depth',
				type: 'NUMBER'
			},
			{
				id: 'EVENT',
				label: 'Events',
				type: 'LIST',
				entity: Entities.EventModel.name
			},
			{
				id: 'workflow',
				label: 'Workflow',
				type: 'STRING',
				entity: Entities.Workflow.name
			},
			{
				id: 'SCOPE_WIDTH',
				label: 'Scope column width',
				type: 'NUMBER'
			},
			{
				id: 'VIEW_STATUS',
				label: 'View status',
				type: 'BOOLEAN'
			}
		]
	},
	PAYMENT_BATCH_MANAGEMENT: {
		label: 'Payment batch management',
		parameters: [
			{
				id: 'plan',
				label: 'Payment plan',
				type: 'STRING',
				entity: Entities.PaymentPlan.name
			}
		]
	},
	PAYMENT_MANAGEMENT: {
		label: 'Payment management',
		parameters: [
			{
				id: 'plan',
				label: 'Payment plan',
				type: 'STRING',
				entity: Entities.PaymentPlan.name
			}
		]
	},
	HIGHCHART: {
		label: 'Chart',
		parameters: [
			{
				id: 'chart',
				label: 'Graph',
				type: 'STRING',
				entity: Entities.Chart.name
			}
		]
	},
	RESOURCE: {
		label: 'Resources',
		parameters: [
			{
				id: 'category',
				label: 'Category',
				type: 'STRING',
				entity: Entities.ResourceCategory.name
			}
		]
	},
	VISITS_DUE_FOR_ACTIVE_CENTERS: {
		label: 'Events due',
		parameters: [
			{
				id: 'events',
				label: 'Events',
				type: 'LIST',
				entity: Entities.EventModel.name
			},
			{
				id: 'delayInterval',
				label: 'Delay interval',
				type: 'NUMBER'
			},
			{
				id: 'timeUnit',
				label: 'Time unit',
				type: 'STRING'
			},
			{
				id: 'timeInterval',
				label: 'Time interval',
				type: 'NUMBER'
			}
		]
	},
	LOCK_SUMMARY: {
		label: 'Lock summary',
		parameters: [
			{
				id: 'scopeModelId',
				label: 'Scope model',
				type: 'STRING',
				entity: Entities.ScopeModel.name
			},
		]
	}
});

function getConfigurationEntity() {
	return Entities[this.entity];
}
function getParameter(parameter_id) {
	const parameter = this.parameters.find(p => p.id === parameter_id);
	if(parameter) {
		return parameter;
	}
	throw new Error(`No parameter with id ${parameter_id}`);
}

for(const [name, type] of Object.entries(WidgetTypes)) {
	type.name = name;
	type.getParameter = getParameter;
	type.parameters?.forEach(parameter => {
		parameter.getConfigurationEntity = getConfigurationEntity;
	});
}
