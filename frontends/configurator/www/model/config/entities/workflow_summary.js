import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class WorkflowSummary extends Node {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			workflowEntity: {type: 'string'},
			workflowIds: {type: 'array'},
			columns: {type: 'array', subtype: Entities.WorkflowSummaryColumn.name},
			leafScopeModelId: {type: 'string'},
			filterEventModelIds: {type: 'array'},
			filterExpectedEvents: {type: 'boolean'},
			title: {type: 'object'},
			displayLegend: {type: 'boolean'},
			displayColumnExport: {type: 'boolean'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.workflowEntity = undefined;
		this.workflowIds = [];
		this.columns = [];
		this.leafScopeModelId = undefined;
		this.filterEventModelIds = [];
		this.filterExpectedEvents = true;
		this.title = {};
		this.displayLegend = undefined;
		this.displayColumnExport = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedTitle(languages) {
		return Utils.getLocalizedField.call(this, 'title', languages);
	}
	getLocalizedLabel(languages) {
		return this.getLocalizedTitle(languages) || this.id;
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

	onChangeScopeModelId(event) {
		if(this.leafScopeModelId && this.leafScopeModelId === event.oldValue) {
			this.leafScopeModelId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.leafScopeModelId === event.node.id) {
			this['delete']();
		}
	}

	onChangeEventModelId(event) {
		this.filterEventModelIds.replace(event.oldValue, event.newValue);
	}
	onDeleteEventModel(event) {
		this.filterEventModelIds.removeElement(event.node.id);
	}

	onDeleteWorkflowSummaryColumn(event) {
		this.columns.removeElement(event.node);
	}
	onMoveWorkflowSummaryColumn(event) {
		if(event.newParent === this) {
			event.node.widget.columns.removeElement(event.node);
			event.node.widget = this;
			this.columns.push(event.node);
		}
	}
}
