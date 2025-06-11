import {ComparatorUtils} from '../comparator_utils.js';
import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class TimelineGraphSection extends Node {
	static getProperties() {
		return {
			timelineGraph: {type: Entities.TimelineGraph.name, back_reference: true},
			id: {type: 'string'},
			label: {type: 'object'},
			useScopePaths: {type: 'boolean'},
			eventModelIds: {type: 'array'},
			hideExpectedEvent: {type: 'boolean'},
			hideDoneEvent: {type: 'boolean'},
			datasetModelId: {type: 'string'},
			dateFieldModelId: {type: 'string'},
			endDateFieldModelId: {type: 'string'},
			valueFieldModelId: {type: 'string'},
			labelFieldModelId: {type: 'string'},
			metaFieldModelIds: {type: 'array'},
			type: {type: 'string'},
			tooltip: {type: 'object'},
			unit: {type: 'string'},
			color: {type: 'string'},
			strokeColor: {type: 'string'},
			opacity: {type: 'number'},
			dashed: {type: 'boolean'},
			mark: {type: 'string'},
			scale: {type: 'string'},
			position: {type: 'number'},
			hiddenLegend: {type: 'boolean'},
			hidden: {type: 'boolean'},
			references: {type: 'array', subtype: Entities.TimelineGraphSectionReference.name}
		};
	}
	static getComparator() {
		return (section_1, section_2) => {
			if(section_1.isPositioned() && section_2.isPositioned()) {
				return section_2.position.start - section_1.position.start;
			}
			if(section_1.isPositioned()) {
				return -1;
			}
			if(section_2.isPositioned()) {
				return 1;
			}
			return ComparatorUtils.compareFields(section_1, section_2, ['id']);
		};
	}

	constructor(values) {
		super();
		this.timelineGraph = undefined;
		this.id = undefined;
		this.label = {};
		this.useScopePaths = undefined;
		this.eventModelIds = [];
		this.hideExpectedEvent = undefined;
		this.hideDoneEvent = undefined;
		this.datasetModelId = undefined;
		this.dateFieldModelId = undefined;
		this.endDateFieldModelId = undefined;
		this.valueFieldModelId = undefined;
		this.labelFieldModelId = undefined;
		this.metaFieldModelIds = undefined;
		this.type = undefined;
		this.tooltip = {};
		this.color = undefined;
		this.strokeColor = undefined;
		this.opacity = 1;
		this.dashed = undefined;
		this.mark = undefined;
		this.scale = undefined;
		this.position = undefined;
		this.hiddenLegend = undefined;
		this.hidden = undefined;
		this.references = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'label', languages) || this.id;
	}
	isPositioned() {
		//compare start position to undefined because start position may be 0
		return this.position?.start !== undefined;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.TimelineGraphSectionReference:
				return this.references.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.TimelineGraphSectionReference:
				this.references.push(child);
				child.section = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
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
			this.dateFieldModelId = undefined;
			this.endDateFieldModelId = undefined;
			this.valueFieldModelId = undefined;
			this.labelFieldModelId = undefined;
			this.metaFieldModelIds = undefined;
		}
	}

	onChangeFieldModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.node.datasetModel.id) {
			if(this.dateFieldModelId && this.dateFieldModelId === event.oldValue) {
				this.dateFieldModelId = event.newValue;
			}
			if(this.endDateFieldModelId && this.endDateFieldModelId === event.oldValue) {
				this.endDateFieldModelId = event.newValue;
			}
			if(this.valueFieldModelId && this.valueFieldModelId === event.oldValue) {
				this.valueFieldModelId = event.newValue;
			}
			if(this.labelFieldModelId && this.labelFieldModelId === event.oldValue) {
				this.labelFieldModelId = event.newValue;
			}
			if(this.metaFieldModelIds && this.metaFieldModelIds === event.oldValue) {
				this.metaFieldModelIds.replace(event.oldValue, event.newValue);
			}
		}
	}
	onDeleteFieldModel(event) {
		if(this.datasetModelId === event.node.datasetModel.id) {
			if(this.dateFieldModelId === event.node.id) {
				this.dateFieldModelId = undefined;
			}
			if(this.endDateFieldModelId === event.node.id) {
				this.endDateFieldModelId = undefined;
			}
			if(this.valueFieldModelId === event.node.id) {
				this.valueFieldModelId = undefined;
			}
			if(this.labelFieldModelId === event.node.id) {
				this.labelFieldModelId = undefined;
			}
			if(this.metaFieldModelIds?.includes(event.node.id)) {
				this.metaFieldModelIds.removeElement(event.node.id);
			}
		}
	}
	onMoveFieldModel(event) {
		if(this.datasetModelId === event.oldParent.id) {
			//shortcut, if a field model is moved, it cannot be kept
			if(this.dateFieldModelId === event.node.id) {
				this.dateFieldModelId = undefined;
			}
			if(this.endDateFieldModelId === event.node.id) {
				this.endDateFieldModelId = undefined;
			}
			if(this.valueFieldModelId === event.node.id) {
				this.valueFieldModelId = undefined;
			}
			if(this.labelFieldModelId === event.node.id) {
				this.labelFieldModelId = undefined;
			}
			if(this.metaFieldModelIds?.includes(event.node.id)) {
				this.metaFieldModelIds.removeElement(event.node.id);
			}
		}
	}
}
