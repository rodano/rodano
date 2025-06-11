import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class TimelineGraphSectionScale extends Node {
	static getProperties() {
		return {
			min: {type: 'number'},
			max: {type: 'number'},
			decimal: {type: 'number'},
			markInterval: {type: 'number'},
			labelInterval: {type: 'number'},
			position: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.min = undefined;
		this.max = undefined;
		this.decimal = undefined;
		this.markInterval = undefined;
		this.labelInterval = undefined;
		this.position = 'LEFT';
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
