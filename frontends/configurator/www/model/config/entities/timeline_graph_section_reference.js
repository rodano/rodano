import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class TimelineGraphSectionReference extends Node {
	static getProperties() {
		return {
			section: {type: Entities.TimelineGraphSection.name, back_reference: true},
			label: {type: 'object'},
			tooltip: {type: 'object'},
			color: {type: 'string'},
			dashed: {type: 'boolean'},
			referenceSectionId: {type: 'string'},
			entries: {type: 'array', subtype: Entities.TimelineGraphSectionReferenceEntry.name}
		};
	}

	constructor(values) {
		super();
		this.section = undefined;
		this.label = {};
		this.tooltip = {};
		this.color = undefined;
		this.dashed = false;
		this.referenceSectionId = undefined;
		this.entries = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'label', languages);
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.TimelineGraphSectionReferenceEntry:
				return this.entries.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.TimelineGraphSectionReferenceEntry:
				this.entries.push(child);
				child.reference = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
}
