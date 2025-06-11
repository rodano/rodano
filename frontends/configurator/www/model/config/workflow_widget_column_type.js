import {DataType} from './data_type.js';
import {WorkflowEntities} from './workflow_entities.js';

export const WorkflowWidgetColumnType = Object.freeze({
	WORKFLOW_LABEL: {
		label: 'Workflow label',
		type: DataType.STRING,
		default_width: 80
	},
	WORKFLOW_TRIGGER_MESSAGE: {
		label: 'Workflow trigger message',
		type: DataType.DATE,
		default_width: 80
	},
	STATUS_LABEL: {
		label: 'Status label',
		type: DataType.STRING,
		default_width: 80
	},
	STATUS_DATE: {
		label: 'Status date',
		type: DataType.DATE,
		default_width: 80
	},
	PARENT_SCOPE_CODE: {
		label: 'Parent scope code',
		type: DataType.STRING,
		default_width: 100,
		workflow_entity: WorkflowEntities.SCOPE
	},
	SCOPE_CODE: {
		label: 'Scope code',
		type: DataType.STRING,
		default_width: 100,
		workflow_entity: WorkflowEntities.SCOPE
	},
	EVENT_LABEL: {
		label: 'Event label',
		type: DataType.STRING,
		default_width: 100,
		workflow_entity: WorkflowEntities.EVENT
	},
	EVENT_DATE: {
		label: 'Event date',
		type: DataType.DATE,
		default_width: 100,
		workflow_entity: WorkflowEntities.EVENT
	},
	FORM_LABEL: {
		label: 'Form label',
		type: DataType.STRING,
		default_width: 100,
		workflow_entity: WorkflowEntities.FORM
	},
	FORM_DATE: {
		label: 'Form date',
		type: DataType.DATE,
		default_width: 100,
		workflow_entity: WorkflowEntities.FORM
	},
	FIELD_LABEL: {
		label: 'Field label',
		type: DataType.STRING,
		default_width: 100,
		workflow_entity: WorkflowEntities.FIELD
	},
	FIELD_DATE: {
		label: 'Field date',
		type: DataType.DATE,
		default_width: 100,
		workflow_entity: WorkflowEntities.FIELD
	}
});
