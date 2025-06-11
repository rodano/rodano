import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class AttributeCriterion extends Node {
	static getProperties() {
		return {
			layout: {type: Entities.Layout.name, back_reference: true},
			attributeSource: {type: 'string'},
			operator: {type: 'string'},
			values: {type: 'array'},
			attributeTarget: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.layout = undefined;
		this.attributeSource = undefined;
		this.operator = undefined;
		this.values = [];
		this.attributeTarget = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
