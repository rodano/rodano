export const ENTITY_OPTIONS = [
	{value: 'SCOPE', label: 'Scope'},
	{value: 'EVENT', label: 'Event'},
	{value: 'DATASET', label: 'Dataset'},
	{value: 'FIELD', label: 'Field'},
	{value: 'FORM', label: 'Form'},
	{value: 'WORKFLOW', label: 'Workflow'}
] as const;

export type Entity = typeof ENTITY_OPTIONS[number]['value'];
