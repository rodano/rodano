import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';
import {Workflow} from './workflow.js';

export class WorkflowState extends DisplayableNode {
	static getProperties() {
		return {
			workflow: {type: Entities.Workflow.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			possibleActionIds: {type: 'array'},
			aggregateStateId: {type: 'string'},
			aggregateStateMatcher: {type: 'string'},
			important: {type: 'boolean'},
			icon: {type: 'string'},
			color: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.workflow = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.possibleActionIds = [];
		this.aggregateStateId = undefined;
		this.aggregateStateMatcher = undefined;
		this.important = false;
		this.icon = undefined;
		this.color = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getPossibleActions() {
		return this.possibleActionIds.map(Workflow.prototype.getAction, this.workflow);
	}

	//bus
	onChangeActionId(event) {
		if(event.node.workflow === this.workflow) {
			this.possibleActionIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteAction(event) {
		if(event.node.workflow === this.workflow) {
			this.possibleActionIds.removeElement(event.node.id);
		}
	}

	onDeleteWorkflowState(event) {
		if(event.node.workflow.id === this.workflow.aggregateWorkflowId && this.trackedStateId === event.node.id) {
			this.trackedStateId = undefined;
			this.trackedStateMatcher = undefined;
		}
	}
	onChangeWorkflowStateId(event) {
		if(event.node.workflow.id === this.workflow.aggregateWorkflowId && this.trackedStateId === event.oldValue) {
			this.trackedStateId = event.newValue;
		}
	}
	onMoveWorkflowState(event) {
		if(event.node.workflow.id === this.workflow.aggregateWorkflowId && this.trackedStateId === event.node.id) {
			this.trackedStateId = undefined;
			this.trackedStateMatcher = undefined;
		}
	}
}
