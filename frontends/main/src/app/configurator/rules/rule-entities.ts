export interface RuleProperty {
	id: string;
	label: string;
	type?: string;
	target?: string;
	configurationEntity?: string;
	options?: string[];
}

export interface RuleEntityAction {
	id: string;
	label: string;
	parameters?: RuleActionParameterDef[];
}

export interface RuleActionParameterDef {
	id: string;
	label: string;
	type?: string;
	configurationEntity?: string;
	dataEntity?: string;
	options?: string[];
	optional?: boolean;
}

export interface RuleEntity {
	name: string;
	label: string;
	configurationEntity?: string;
	properties: RuleProperty[];
	actions: RuleEntityAction[];
}

export const RULE_ENTITIES: Record<string, RuleEntity> = {
	SCOPE: {
		name: 'SCOPE',
		label: 'Scope',
		configurationEntity: 'ScopeModel',
		properties: [
			{id: 'ANCESTOR', label: 'Ancestor scopes', target: 'SCOPE'},
			{id: 'PARENT', label: 'Parent scopes', target: 'SCOPE'},
			{id: 'DEFAULT_PARENT', label: 'Default parent scope', target: 'SCOPE'},
			{id: 'DESCENDANT', label: 'Descendant scopes', target: 'SCOPE'},
			{id: 'LEAF', label: 'Leaf scopes', target: 'SCOPE'},
			{id: 'CODE', label: 'Code', type: 'STRING', target: 'SCOPE'},
			{id: 'ID', label: 'Id', type: 'STRING', target: 'SCOPE', configurationEntity: 'ScopeModel'},
			{id: 'REMOVED', label: 'Removed', type: 'BOOLEAN', target: 'SCOPE', options: ['true', 'false']},
			{id: 'CONTAINS_DATA', label: 'Contains data', type: 'BOOLEAN', target: 'SCOPE', options: ['true', 'false']},
			{id: 'MODEL', label: 'Model', type: 'STRING', target: 'SCOPE', configurationEntity: 'ScopeModel'},
			{id: 'NUMBER_OF_LEAF', label: 'Number of leafs', type: 'STRING', target: 'SCOPE'},
			{id: 'SCOPE_NUMBER_IN_PARENT', label: 'Index in parent', type: 'STRING', target: 'SCOPE'},
			{id: 'WORKFLOW', label: 'Workflows', target: 'WORKFLOW'},
			{id: 'EVENT', label: 'Events', target: 'EVENT'},
			{id: 'DATASET', label: 'Datasets', target: 'DATASET'},
			{id: 'FORM', label: 'Forms', target: 'FORM'},
			{id: 'INCEPTIVE_EVENT', label: 'Inceptive event', target: 'EVENT'},
			{id: 'FIELD_HAVING_VALUE', label: 'Fields having a value', target: 'FIELD'}
		],
		actions: [
			{id: 'CHANGE_CODE', label: 'Change code', parameters: [{id: 'CODE', label: 'Code', type: 'STRING'}]},
			{id: 'CHANGE_SHORTNAME', label: 'Change shortname', parameters: [{id: 'SHORTNAME', label: 'Shortname', type: 'STRING'}]},
			{id: 'CHANGE_LONGNAME', label: 'Change longname', parameters: [{id: 'LONGNAME', label: 'Longname', type: 'STRING'}]},
			{id: 'BLOCK', label: 'Block scope'},
			{id: 'UNBLOCK', label: 'Unblock scope'},
			{id: 'INITIALIZE_WORKFLOW', label: 'Initialize workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'ENABLE_ROLE', label: 'Enable role', parameters: [{id: 'NEW_PROFILE', label: 'New profile', type: 'STRING', configurationEntity: 'Profile'}, {id: 'TARGET_PROFILE', label: 'Target profile', type: 'STRING', configurationEntity: 'Profile'}]},
			{id: 'DISABLE_ROLE', label: 'Disable role', parameters: [{id: 'PROFILE', label: 'Profile', type: 'STRING', configurationEntity: 'Profile'}]},
			{id: 'EXPORT', label: 'Export'},
			{id: 'WRITE_TO_LOG', label: 'Write to log', parameters: [{id: 'TEXT', label: 'Text', type: 'TEXT'}]},
			{id: 'CREATE_EVENT', label: 'Create event', parameters: [{id: 'EVENT_MODEL_ID', label: 'Event model', type: 'STRING', configurationEntity: 'EventModel'}, {id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'VALIDATE', label: 'Validate all fields'},
			{id: 'RESET_EVENT_DATES', label: 'Reset event dates'},
			{id: 'ADD_FORM', label: 'Add or restore form', parameters: [{id: 'FORM_MODEL_ID', label: 'Form model', type: 'STRING', configurationEntity: 'FormModel'}, {id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'SET_START_DATE', label: 'Set start date', parameters: [{id: 'DATE', label: 'Start Date', type: 'DATE'}]},
			{id: 'SET_STOP_DATE', label: 'Set stop date', parameters: [{id: 'DATE', label: 'Stop Date', type: 'DATE'}]},
			{id: 'REMOVE_START_DATE', label: 'Remove start date'},
			{id: 'REMOVE_STOP_DATE', label: 'Remove stop date'},
			{id: 'ADD_PARENT', label: 'Add parent', parameters: [{id: 'PARENT_SCOPE', label: 'Parent Scope', type: 'STRING', dataEntity: 'SCOPE'}]},
			{id: 'REMOVE_PARENT', label: 'Remove parent', parameters: [{id: 'PARENT_SCOPE', label: 'Parent Scope', type: 'STRING', dataEntity: 'SCOPE'}]}
		]
	},
	EVENT: {
		name: 'EVENT',
		label: 'Event',
		configurationEntity: 'EventModel',
		properties: [
			{id: 'SCOPE', label: 'Scope', target: 'SCOPE'},
			{id: 'ID', label: 'Id', type: 'STRING', target: 'EVENT', configurationEntity: 'EventModel'},
			{id: 'EVENT_GROUP_ID', label: 'Event group id', type: 'STRING', target: 'EVENT', configurationEntity: 'EventGroup'},
			{id: 'DATE', label: 'Date', type: 'DATE', target: 'EVENT'},
			{id: 'EXPECTED_DATE', label: 'Expected date', type: 'DATE', target: 'EVENT'},
			{id: 'DATE_OR_EXPECTED_DATE', label: 'Date or expected date', type: 'DATE', target: 'EVENT'},
			{id: 'CREATION_DATE', label: 'Creation date', type: 'DATE', target: 'EVENT'},
			{id: 'EXPECTED', label: 'Expected', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'BLOCKED', label: 'Blocked', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'NOT_DONE', label: 'Not done', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'REMOVED', label: 'Removed', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'FIELD_HAVING_VALUE', label: 'Fields having a value', target: 'FIELD'},
			{id: 'CONTAINS_DATA', label: 'Contains data', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'PREVIOUS', label: 'Previous event', target: 'EVENT'},
			{id: 'ALL_PREVIOUS', label: 'Previous events', target: 'EVENT'},
			{id: 'NEXT', label: 'Next event', target: 'EVENT'},
			{id: 'ALL_NEXT', label: 'Next events', target: 'EVENT'},
			{id: 'ALL_NEXT_INCLUDING_REMOVED', label: 'Next events including removed', target: 'EVENT'},
			{id: 'EVENT_GROUP_NUMBER', label: 'Event group number', type: 'NUMBER', target: 'EVENT'},
			{id: 'EVENT_RESPECT_INTERVAL', label: 'Event respect interval', type: 'BOOLEAN', target: 'EVENT', options: ['true', 'false']},
			{id: 'DATASET', label: 'Datasets', target: 'DATASET'},
			{id: 'FORM', label: 'Forms', target: 'FORM'},
			{id: 'WORKFLOW', label: 'Workflows', target: 'WORKFLOW'}
		],
		actions: [
			{id: 'ADD_FORM', label: 'Add or restore form', parameters: [{id: 'FORM_MODEL_ID', label: 'Form model', type: 'STRING', configurationEntity: 'FormModel'}, {id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'RESET_NEXT_EVENT_DATES', label: 'Reset next event dates'},
			{id: 'REMOVE', label: 'Remove event', parameters: [{id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'RESTORE', label: 'Restore event', parameters: [{id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'VALIDATE', label: 'Validate all fields'},
			{id: 'SET_BLOCKING', label: 'Set blocking', parameters: [{id: 'BLOCKING', label: 'is blocked', type: 'STRING', options: ['true', 'false']}]},
			{id: 'SET_LOCKED', label: 'Set locked', parameters: [{id: 'LOCKED', label: 'is locked', type: 'STRING', options: ['true', 'false']}]},
			{id: 'SET_NOT_DONE', label: 'Set not done'},
			{id: 'SET_DONE', label: 'Set done'},
			{id: 'SET_DATE', label: 'Set date', parameters: [{id: 'DATE', label: 'Date', type: 'DATE'}]},
			{id: 'SET_END_DATE', label: 'Set end date', parameters: [{id: 'DATE', label: 'Date', type: 'DATE'}]},
			{id: 'INITIALIZE_WORKFLOW', label: 'Initialize workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'DELETE_WORKFLOW', label: 'Delete workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]}
		]
	},
	DATASET: {
		name: 'DATASET',
		label: 'Dataset',
		configurationEntity: 'DatasetModel',
		properties: [
			{id: 'SCOPE', label: 'Scope', target: 'SCOPE'},
			{id: 'EVENT', label: 'Event', target: 'EVENT'},
			{id: 'PK', label: 'Pk', type: 'NUMBER', target: 'DATASET', configurationEntity: 'DatasetModel'},
			{id: 'ID', label: 'Id', type: 'STRING', target: 'DATASET', configurationEntity: 'DatasetModel'},
			{id: 'REMOVED', label: 'Removed', type: 'BOOLEAN', target: 'DATASET', options: ['true', 'false']},
			{id: 'IS_ATTACHED_TO_SCOPE', label: 'Is directly attached to scope', type: 'BOOLEAN', target: 'DATASET', options: ['true', 'false']},
			{id: 'FIELD', label: 'Fields', target: 'FIELD'},
			{id: 'CREATION_DATE', label: 'Creation date', type: 'DATE', target: 'DATASET'}
		],
		actions: []
	},
	FIELD: {
		name: 'FIELD',
		label: 'Field',
		configurationEntity: 'FieldModel',
		properties: [
			{id: 'ID', label: 'Id', type: 'STRING', target: 'FIELD', configurationEntity: 'FieldModel'},
			{id: 'VALUE', label: 'Value', type: 'STRING', target: 'FIELD'},
			{id: 'VALUE_DATE', label: 'Date value', type: 'DATE', target: 'FIELD'},
			{id: 'VALUE_NUMBER', label: 'Number value', type: 'NUMBER', target: 'FIELD'},
			{id: 'HAS_WORKFLOW', label: 'Has workflow', type: 'BOOLEAN', target: 'FIELD', options: ['true', 'false']},
			{id: 'IS_DYNAMIC', label: 'Is dynamic', type: 'BOOLEAN', target: 'FIELD', options: ['true', 'false']},
			{id: 'DATASET', label: 'Dataset', target: 'DATASET'},
			{id: 'FORM', label: 'Form', target: 'FORM'},
			{id: 'MODIFICATION_DATE', label: 'Date of modification', type: 'DATE', target: 'FIELD'},
			{id: 'NEWEST_AUDIT_TRAIL', label: 'Date of newest audit trail', type: 'DATE', target: 'FIELD'},
			{id: 'OLDEST_AUDIT_TRAIL', label: 'Date of oldest audit trail', type: 'DATE', target: 'FIELD'},
			{id: 'WORKFLOW', label: 'Workflow', target: 'WORKFLOW'},
			{id: 'LAST_NON_EMPTY_VALUE', label: 'Last non empty value', target: 'FIELD'}
		],
		actions: [
			{id: 'SET_STRING_VALUE', label: 'Set value (using a string)', parameters: [{id: 'VALUE', label: 'Value', type: 'STRING'}]},
			{id: 'SET_OBJECT_VALUE', label: 'Set value (using an object)', parameters: [{id: 'VALUE', label: 'Value', type: 'STRING'}]},
			{id: 'INITIALIZE_WORKFLOW', label: 'Initialize workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'DELETE_WORKFLOW', label: 'Delete workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'RESET', label: 'Reset'},
			{id: 'CALCULATE', label: 'Calculate'}
		]
	},
	FORM: {
		name: 'FORM',
		label: 'Form',
		configurationEntity: 'FormModel',
		properties: [
			{id: 'ID', label: 'Id', type: 'STRING', target: 'FORM', configurationEntity: 'FormModel'},
			{id: 'IS_ATTACHED_TO_SCOPE', label: 'Is directly attached to scope', type: 'BOOLEAN', target: 'FORM', options: ['true', 'false']},
			{id: 'WORKFLOW', label: 'Workflow', target: 'WORKFLOW'},
			{id: 'EVENT', label: 'Event', target: 'EVENT'},
			{id: 'FIELD', label: 'Fields', target: 'FIELD'}
		],
		actions: [
			{id: 'INITIALIZE_WORKFLOW', label: 'Initialize workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'DELETE_WORKFLOW', label: 'Delete workflow', parameters: [{id: 'WORKFLOW', label: 'Workflow', type: 'STRING', configurationEntity: 'Workflow'}]},
			{id: 'REMOVE', label: 'Remove form and fields', parameters: [{id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]},
			{id: 'RESTORE', label: 'Restore form', parameters: [{id: 'RATIONALE', label: 'Rationale', type: 'STRING'}]}
		]
	},
	WORKFLOW: {
		name: 'WORKFLOW',
		label: 'Workflow',
		configurationEntity: 'Workflow',
		properties: [
			{id: 'ID', label: 'Id', type: 'STRING', target: 'WORKFLOW', configurationEntity: 'Workflow'},
			{id: 'STATUS', label: 'Status', type: 'STRING', target: 'WORKFLOW', configurationEntity: 'WorkflowState'},
			{id: 'DATE_OF_FIRST_STATUS_AFTER_INITIALIZATION', label: 'Date of first trail after workflow init', type: 'DATE', target: 'WORKFLOW'},
			{id: 'CREATION_ACTION', label: 'Creation action', type: 'STRING', target: 'WORKFLOW'},
			{id: 'VALIDATOR_ID', label: 'Validator ID', type: 'STRING', target: 'WORKFLOW'},
			{id: 'COMMENT_ON_LAST_AUDIT_TRAIL', label: 'Has comment on last audit trail', type: 'BOOLEAN', target: 'WORKFLOW', options: ['true', 'false']},
			{id: 'SCOPE', label: 'Scope', target: 'SCOPE'},
			{id: 'EVENT', label: 'Event', target: 'EVENT'},
			{id: 'FORM', label: 'Form', target: 'FORM'},
			{id: 'FIELD', label: 'Field', target: 'FIELD'}
		],
		actions: [
			{id: 'CHANGE_STATUS', label: 'Change status', parameters: [{id: 'STATUS', label: 'Status', type: 'STRING'}, {id: 'MESSAGE', label: 'Message', type: 'STRING', optional: true}]}
		]
	}
};
