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
			backgroundColor: {type: 'string'},
			overrideUserRights: {type: 'boolean'},
			valuesMin: {type: 'number'},
			valuesMax: {type: 'number'},
			withStatistics: {type: 'boolean'},
			usePercentile: {type: 'boolean'},
			request: {type: 'object'},
			ranges: {type: 'array', subtype: Entities.ChartRange.name},
			workflowId: {type: 'string'},
			includedStateIds: {type: 'array'},
			excludedStateIds: {type: 'array'},
			scopeModelId: {type: 'string'},
			leafScopeModelId: {type: 'string'},
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
		this.legendX = {x: undefined, y: undefined, color: undefined, labels: {}};
		this.legendY = {x: undefined, y: undefined, color: undefined, labels: {}};
		this.colors = [];
		this.backgroundColor = undefined;
		this.overrideUserRights = false;
		this.valuesMin = undefined;
		this.valuesMax = undefined;
		this.withStatistics = false;
		this.usePercentile = false;
		this.request = {};
		this.ranges = [];
		this.workflowId = undefined;
		this.includedStateIds = [];
		this.excludedStateIds = [];
		this.scopeModelId = undefined;
		this.leafScopeModelId = undefined;
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
}
