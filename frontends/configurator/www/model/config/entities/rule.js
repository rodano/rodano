import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class Rule extends Node {
	static getProperties() {
		return {
			rulable: {back_reference: true},
			description: {type: 'string'},
			constraint: {type: 'string'},
			actions: {type: 'array', subtype: Entities.RuleAction.name},
			message: {type: 'object'},
			tags: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.rulable = undefined;
		this.description = undefined;
		this.constraint = undefined;
		this.actions = [];
		this.message = {};
		this.tags = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.description;
	}

	//rulable and layoutable
	getStudy() {
		return this.rulable.getStudy();
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
			case Entities.RuleAction:
				return this.actions.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.RuleAction:
				this.actions.push(child);
				child.rule = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
}
