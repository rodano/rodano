import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Report} from '../report.js';

export class ValueSource extends Node {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			scopeModelId: {type: 'string'},
			eventModelId: {type: 'string'},
			datasetModelId: {type: 'string'},
			fieldModelId: {type: 'string'},
			eventSource: {type: 'string'},
			forStatistics: {type: 'string'},
			ignoreNull: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.scopeModelId = undefined;
		this.eventModelId = undefined;
		this.datasetModelId = undefined;
		this.fieldModelId = undefined;
		this.eventSource = undefined;
		this.forStatistics = undefined;
		this.ignoreNull = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeScopeModelId(event) {
		if(this.scopeModelId && this.scopeModelId === event.oldValue) {
			this.scopeModelId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.scopeModelId === event.node.id) {
			this.scopeModelId = undefined;
		}
	}

	onChangeEventModelId(event) {
		if(this.eventModelId && this.eventModelId === event.oldValue) {
			this.eventModelId = event.newValue;
		}
	}
	onDeleteEventModel(event) {
		if(this.eventModelId === event.node.id) {
			this.eventModelId = undefined;
		}
	}

	onChangeDatasetModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.oldValue) {
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
		if(this.datasetModelId && this.datasetModelId === event.node.datasetModel.id && this.fieldModelId && this.fieldModelId === event.oldValue) {
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

	//report
	report(settings) {
		const report = new Report(this);
		Report.checkId(report, this, settings.id_check !== false);
		if(this.datasetModelId && this.fieldModelId) {
			try {
				this.study.getDatasetModel(this.datasetModelId).getFieldModel(this.fieldModelId);
			}
			catch {
				report.addError(`Value source references invalid dataset model ${this.datasetModelId} and field model ${this.fieldModelId}`);
			}
		}
		if(this.eventModelId) {
			try {
				const scope_model = this.study.getScopeModel(this.scopeModelId);
				const event_model = scope_model.getEventModel(this.eventModelId);
				if(this.datasetModelId) {
					if(!event_model.datasetModelIds.includes(this.datasetModelId)) {
						report.addError(`Value source is invalid because event model ${this.eventModelId} does not contain dataset model ${this.datasetModelId}`);
					}
					else {
						const dataset_model = this.study.getDatasetModel(this.datasetModelId);
						if(this.fieldModelId) {
							if(!dataset_model.fieldModels.some(a => a.id === this.fieldModelId)) {
								report.addError(`Value source is invalid because dataset model ${this.datasetModelId} does not contain field model ${this.fieldModelId}`);
							}
						}
					}
				}
			}
			catch {
				report.addError(`Value source references unknown event model ${this.eventModelId} or dataset model ${this.datasetModelId}`);
			}
		}
		return report;
	}
}
