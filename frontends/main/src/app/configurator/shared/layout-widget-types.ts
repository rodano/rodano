export interface WidgetTypeDef {
	type: string;
	label: string;
	parameters: WidgetParamDef[];
}

export interface WidgetParamDef {
	id: string;
	label: string;
	kind: 'chart' | 'workflowWidget' | 'workflowSummary' | 'resourceCategory' | 'scopeModel' | 'eventModel' | 'formModel' | 'workflow' | 'boolean' | 'number' | 'text';
}

export const WIDGET_TYPES: WidgetTypeDef[] = [
	{type: 'ACTIVITY_LOG', label: 'Activity Log', parameters: [
		{id: 'displayAddResponse', label: 'Display Add Response', kind: 'boolean'},
		{id: 'removeProfileSelector', label: 'Remove Profile Selector', kind: 'boolean'}
	]},
	{type: 'CHART', label: 'Chart', parameters: [
		{id: 'chart', label: 'Graph', kind: 'chart'}
	]},
	{type: 'CONTACTS', label: 'Contacts', parameters: []},
	{type: 'GENERAL_INFORMATIONS', label: 'General Information', parameters: []},
	{type: 'LOCK_SUMMARY', label: 'Lock Summary', parameters: [
		{id: 'scopeModelId', label: 'Scope Model', kind: 'scopeModel'}
	]},
	{type: 'RESOURCE', label: 'Resources', parameters: [
		{id: 'category', label: 'Category', kind: 'resourceCategory'}
	]},
	{type: 'SCOPE_OVERDUE', label: 'Scope Overdue', parameters: [
		{id: 'OVERDUE_TYPE', label: 'Overdue Type', kind: 'text'},
		{id: 'SPECIFIC_COLUMN_NAME', label: 'Specific Column Name', kind: 'text'}
	]},
	{type: 'WELCOME_TEXT', label: 'Welcome Text', parameters: []},
	{type: 'WORKFLOW', label: 'Workflow', parameters: [
		{id: 'workflow', label: 'Workflow Widget', kind: 'workflowWidget'},
		{id: 'width', label: 'Display Width (px)', kind: 'number'}
	]},
	{type: 'WORKFLOWS_SUMMARY', label: 'Workflow Summary', parameters: [
		{id: 'summary', label: 'Workflow Summary', kind: 'workflowSummary'}
	]}
];
