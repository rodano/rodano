import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

//TODO this class must extend ValueSource
export class ValueSourceCriteria extends Node {
	static getProperties() {
		return {
			scope: {type: 'string'},
			event: {type: 'string'},
			dataset: {type: 'string'},
			attribute: {type: 'string'},
			eventSource: {type: 'string'},
			operator: {type: 'string'},
			value: {type: 'string'},
			values: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.scope = undefined;
		this.event = undefined;
		this.dataset = undefined;
		this.attribute = undefined;
		this.eventSource = undefined;
		this.operator = undefined;
		this.value = undefined;
		this.values = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
