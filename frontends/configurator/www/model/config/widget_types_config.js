import {Entities} from './entities.js';

export const WidgetTypes = Object.freeze({
	CHART: {
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
	GENERAL_INFORMATIONS: {
		label: 'General information'
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
	WELCOME_TEXT: {
		label: 'Welcome text'
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
