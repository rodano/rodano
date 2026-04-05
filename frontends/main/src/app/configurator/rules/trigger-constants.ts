export interface TriggerType {
	id: string;
	label: string;
	domains: string[];
}

export const TRIGGER_TYPES: TriggerType[] = [
	{id: 'CREATE_SCOPE', label: 'Scope Created', domains: ['SCOPE']},
	{id: 'REMOVE_SCOPE', label: 'Scope Removed', domains: ['SCOPE']},
	{id: 'RESTORE_SCOPE', label: 'Scope Restored', domains: ['SCOPE']},
	{id: 'CREATE_EVENT', label: 'Event Created', domains: ['SCOPE', 'EVENT']},
	{id: 'REMOVE_EVENT', label: 'Event Removed', domains: ['SCOPE', 'EVENT']},
	{id: 'RESTORE_EVENT', label: 'Event Restored', domains: ['SCOPE', 'EVENT']},
	{id: 'CREATE_DATASET', label: 'Dataset Created', domains: ['SCOPE', 'EVENT', 'DATASET']},
	{id: 'REMOVE_DATASET', label: 'Dataset Removed', domains: ['SCOPE', 'EVENT', 'DATASET']},
	{id: 'RESTORE_DATASET', label: 'Dataset Restored', domains: ['SCOPE', 'EVENT', 'DATASET']},
	{id: 'CREATE_WORKFLOW_STATUS', label: 'Workflow Status Created', domains: ['SCOPE', 'EVENT', 'DATASET', 'FIELD', 'FORM', 'WORKFLOW']},
	{id: 'UPDATE_VALUE', label: 'Field Updated', domains: ['SCOPE', 'EVENT', 'DATASET', 'FIELD']},
	{id: 'SAVE_FORM', label: 'Form Saved', domains: ['SCOPE', 'EVENT', 'FORM']},
	{id: 'ROLE_CREATE', label: 'Role Created', domains: ['SCOPE']},
	{id: 'ROLE_ENABLE', label: 'Role Enabled', domains: ['SCOPE']},
	{id: 'ROLE_DISABLE', label: 'Role Disabled', domains: ['SCOPE']},
	{id: 'USER_LOGIN', label: 'User Logged In', domains: ['SCOPE']}
];
