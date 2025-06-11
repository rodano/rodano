import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class TimelineGraphSectionReferenceEntry extends Node {
	static getProperties() {
		return {
			reference: {type: Entities.TimelineGraphSectionReference.name, back_reference: true},
			timepoint: {type: 'string'},
			value: {type: 'number'},
			label: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.reference = undefined;
		this.timepoint = undefined;
		this.value = undefined;
		this.label = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
