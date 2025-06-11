import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class ChartRequest extends Node {
	static getProperties() {
		return {
			eventModelId: {type: 'string'},
			datasetModelId: {type: 'string'},
			fieldModelId: {type: 'string'},
		};
	}

	constructor(values) {
		super();
		this.eventModelId = undefined;
		this.datasetModelId = undefined;
		this.fieldModelId = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeEventModelId(event) {
		if(this.eventModelId === event.oldValue) {
			this.eventModelId = event.newValue;
		}
	}
	onDeleteEventModel(event) {
		if(this.eventModelId === event.node.id) {
			this.eventModelId = undefined;
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
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
