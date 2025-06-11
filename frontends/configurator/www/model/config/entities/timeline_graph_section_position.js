import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class TimelineGraphSectionPosition extends Node {
	static getProperties() {
		return {
			start: {type: 'number'},
			stop: {type: 'number'}
		};
	}

	constructor(values) {
		super();
		this.start = undefined;
		this.stop = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
