import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class WorkflowStatesSelector extends Node {
	static getProperties() {
		return {
			parent: {back_reference: true},
			workflowId: {type: 'string'},
			stateIds: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.parent = undefined;
		this.workflowId = undefined;
		this.stateIds = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeWorkflowId(event) {
		if(this.workflowId && this.workflowId === event.oldValue) {
			this.workflowId = event.newValue;
		}
	}
	onDeleteWorkflow(event) {
		if(this.workflowId === event.node.id) {
			this['delete']();
		}
	}

	onChangeWorkflowStateId(event) {
		if(this.workflowId && this.workflowId === event.node.workflow.id) {
			this.stateIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteWorkflowState(event) {
		if(this.workflowId === event.node.workflow.id) {
			this.stateIds.removeElement(event.node.id);
		}
	}
	onMoveWorkflowState(event) {
		if(this.workflowId === event.oldParent.id) {
			this.stateIds.removeElement(event.node.id);
		}
	}
}
