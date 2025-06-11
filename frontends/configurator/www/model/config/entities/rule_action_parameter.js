import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class RuleActionParameter extends Node {
	static getProperties() {
		return {
			action: {type: Entities.RuleAction.name, back_reference: true},
			id: {type: 'string'},
			rulableEntity: {type: 'string'},
			conditionId: {type: 'string'},
			value: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.action = undefined;
		this.id = undefined;
		this.rulableEntity = undefined;
		this.conditionId = undefined;
		this.value = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.id;
	}
	getDefinition() {
		return this.action.getDefinition().getParameter(this.id);
	}
}
