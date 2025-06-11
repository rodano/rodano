import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class Report extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			rawSql: {type: 'string'},
			datasetModelId: {type: 'string'},
			workflowId: {type: 'string'},
			fieldModelIds: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.rawSql = undefined;
		this.datasetModelId = undefined;
		this.workflowId = undefined;
		this.fieldModelIds = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeDatasetModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.oldValue) {
			this.datasetModelId = event.newValue;
		}
	}
	onDeleteDatasetModel(event) {
		if(this.datasetModelId === event.node.id) {
			this.datasetModelId = undefined;
		}
	}

	onChangeFieldModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.node.datasetModel.id && this.fieldModelIds.includes(event.oldValue)) {
			this.fieldModelIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteFieldModel(event) {
		if(this.datasetModelId === event.node.datasetModel.id && this.fieldModelIds.includes(event.node.id)) {
			this.fieldModelIds.removeElement(event.node.id);
		}
	}
	onMoveFieldModel(event) {
		if(this.datasetModelId === event.oldParent.id && this.fieldModelIds.includes(event.node.id)) {
			this.datasetModelId = event.newParent.id;
		}
	}

	onChangeWorkflowId(event) {
		if(this.workflowId && this.workflowId === event.oldValue) {
			this.workflowId = event.newValue;
		}
	}
	onDeleteWorkflow(event) {
		if(this.workflowId === event.node.id) {
			this.workflowId = undefined;
		}
	}
}
