import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class Chart extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			type: {type: 'string'},
			title: {type: 'object'},
			legendX: {type: 'object'},
			legendY: {type: 'object'},
			colors: {type: 'array'},
			overrideUserRights: {type: 'boolean'},
			withStatistics: {type: 'boolean'},
			ranges: {type: 'array', subtype: Entities.ChartRange.name},
			workflowId: {type: 'string'},
			includedStateIds: {type: 'array'},
			excludedStateIds: {type: 'array'},
			scopeModelId: {type: 'string'},
			leafScopeModelId: {type: 'string'},
			eventModelId: {type: 'string'},
			datasetModelId: {type: 'string'},
			fieldModelId: {type: 'string'},
			enrollmentWorkflowId: {type: 'string'},
			enrollmentStateIds: {type: 'array'},
			displayExpected: {type: 'boolean'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.type = 'STATISTICS';
		this.title = {};
		this.legendX = {};
		this.legendY = {};
		this.colors = [];
		this.overrideUserRights = false;
		this.withStatistics = false;
		this.ranges = [];
		this.workflowId = undefined;
		this.includedStateIds = [];
		this.excludedStateIds = [];
		this.scopeModelId = undefined;
		this.leafScopeModelId = undefined;
		this.eventModelId = undefined;
		this.datasetModelId = undefined;
		this.fieldModelId = undefined;
		this.enrollmentWorkflowId = undefined;
		this.enrollmentStateIds = [];
		this.displayExpected = false;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeScopeModelId(event) {
		if(this.scopeModelId && this.scopeModelId === event.oldValue) {
			this.scopeModelId = event.newValue;
		}
		if(this.leafScopeModelId && this.leafScopeModelId === event.oldValue) {
			this.leafScopeModelId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.leafScopeModelId === event.node.id || this.scopeModelId === event.node.id) {
			this['delete']();
		}
	}

	onChangeWorkflowId(event) {
		if(this.workflowId && this.workflowId === event.oldValue) {
			this.workflowId = event.newValue;
		}
		if(this.enrollmentWorkflowId && this.enrollmentWorkflowId === event.oldValue) {
			this.enrollmentWorkflowId = event.newValue;
		}
	}
	onDeleteWorkflow(event) {
		if(this.workflowId === event.node.id || this.enrollmentWorkflowId === event.node.id) {
			this['delete']();
		}
	}

	onChangeWorkflowStateId(event) {
		if(this.workflowId && this.workflowId === event.node.workflow.id) {
			this.includedStateIds.replace(event.oldValue, event.newValue);
			this.excludedStateIds.replace(event.oldValue, event.newValue);
		}
		if(this.enrollmentWorkflowId && this.enrollmentWorkflowId === event.node.workflow.id) {
			this.enrollmentStateIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteWorkflowState(event) {
		if(this.workflowId === event.node.workflow.id) {
			this.includedStateIds.removeElement(event.node.id);
			this.excludedStateIds.removeElement(event.node.id);
		}
		if(this.enrollmentWorkflowId === event.node.workflow.id) {
			this.enrollmentStateIds.removeElement(event.node.id);
		}
	}
	onMoveWorkflowState(event) {
		if(this.workflowId === event.oldParent.id) {
			this.includedStateIds.removeElement(event.node.id);
			this.excludedStateIds.removeElement(event.node.id);
		}
		if(this.enrollmentWorkflowId === event.oldParent.id) {
			this.enrollmentStateIds.removeElement(event.node.id);
		}
	}

	onChangeDatasetModelId(event) {
		if(this.datasetModelId === event.oldValue) {
			this.datasetModelId = event.newValue;
		}
	}
	onDeleteDatasetModel(event) {
		if(this.datasetModelId === event.node.id) {
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
		}
	}

	onChangeFieldModelId(event) {
		if(this.fieldModelId && this.fieldModelId === event.oldValue) {
			this.fieldModelId = event.newValue;
		}
	}
	onDeleteFieldModel(event) {
		if(this.datasetModelId === event.node.datasetModel.id && this.fieldModelId === event.node.id) {
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
		}
	}
	onMoveFieldModel(event) {
		if(this.datasetModelId === event.oldParent.id && this.fieldModelId === event.node.id) {
			this.datasetModelId = event.newParent.id;
		}
	}
}
