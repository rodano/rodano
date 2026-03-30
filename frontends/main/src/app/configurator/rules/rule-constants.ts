export const OPERATORS = [
	{value: 'EQUALS', label: 'Equals to', hasValue: true},
	{value: 'NOT_EQUALS', label: 'Not equals to', hasValue: true},
	{value: 'CONTAINS', label: 'Contains', hasValue: true},
	{value: 'NOT_CONTAINS', label: 'Contains not', hasValue: true},
	{value: 'GREATER', label: 'Greater than', hasValue: true},
	{value: 'GREATER_EQUALS', label: 'Greater or equals to', hasValue: true},
	{value: 'LOWER', label: 'Lower than', hasValue: true},
	{value: 'LOWER_EQUALS', label: 'Lower or equals to', hasValue: true},
	{value: 'NULL', label: 'Is null', hasValue: false},
	{value: 'NOT_NULL', label: 'Is not null', hasValue: false},
	{value: 'BLANK', label: 'Is blank', hasValue: false},
	{value: 'NOT_BLANK', label: 'Is not blank', hasValue: false}
];

export const OPERATORS_BY_TYPE: Record<string, string[]> = {
	BOOLEAN: ['EQUALS', 'NOT_EQUALS', 'NULL', 'NOT_NULL'],
	STRING: ['EQUALS', 'NOT_EQUALS', 'CONTAINS', 'NOT_CONTAINS', 'NULL', 'NOT_NULL', 'BLANK', 'NOT_BLANK'],
	NUMBER: ['EQUALS', 'NOT_EQUALS', 'GREATER', 'GREATER_EQUALS', 'LOWER', 'LOWER_EQUALS', 'NULL', 'NOT_NULL'],
	DATE: ['EQUALS', 'NOT_EQUALS', 'GREATER', 'GREATER_EQUALS', 'LOWER', 'LOWER_EQUALS', 'NULL', 'NOT_NULL']
};

export const ACTION_TYPES = [
	{value: 'ENTITY_ACTION', label: 'On your selection'},
	{value: 'STATIC_ACTION', label: 'Using a pre-configured action'},
	{value: 'CONFIGURATION_ACTION', label: 'Trigger another action'}
];

export const DOMAINS = ['SCOPE', 'EVENT', 'DATASET', 'FIELD', 'FORM', 'WORKFLOW'] as const;
export type Domain = typeof DOMAINS[number];
