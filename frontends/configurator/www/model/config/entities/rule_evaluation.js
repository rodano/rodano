import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class RuleEvaluation extends Node {
	static getProperties() {
		return {
			conditionId: {type: 'string'},
			property: {type: 'string'},
			operator: {type: 'string'},
			values: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.conditionId = undefined;
		this.property = undefined;
		this.operator = undefined;
		this.values = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
