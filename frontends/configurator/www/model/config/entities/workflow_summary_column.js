import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class WorkflowSummaryColumn extends Node {
	static getProperties() {
		return {
			summary: {type: Entities.WorkflowSummary.name, back_reference: true},
			label: {type: 'object'},
			description: {type: 'object'},
			total: {type: 'boolean'},
			stateIds: {type: 'array'},
			percent: {type: 'boolean'},
			nonNullColor: {type: 'string'},
			nonNullBackgroundColor: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.summary = undefined;
		this.label = {};
		this.description = {};
		this.total = false;
		this.stateIds = [];
		this.percent = true;
		this.nonNullColor = undefined;
		this.nonNullBackgroundColor = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeWorkflowStateId(event) {
		if(this.summary.workflowIds.includes(event.node.workflow.id)) {
			this.stateIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteWorkflowState(event) {
		if(this.summary.workflowIds.includes(event.node.workflow.id)) {
			this.stateIds.removeElement(event.node.id);
		}
	}
	onMoveWorkflowState(event) {
		if(this.summary.workflowIds.includes(event.oldParent.id)) {
			this.stateIds.removeElement(event.node.id);
		}
	}
}
