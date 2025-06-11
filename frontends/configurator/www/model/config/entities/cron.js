import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {RuleEntities} from '../rule_entities.js';
import {Utils} from '../utils.js';

export class Cron extends Node {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			description: {type: 'object'},
			interval: {type: 'number'},
			intervalUnit: {type: 'string'},
			rules: {type: 'array', subtype: Entities.Rule.name}
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE
	];

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.description = {};
		this.interval = undefined;
		this.intervalUnit = undefined;
		this.rules = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'description', languages) || this.id;
	}

	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.Rule:
				return this.rules.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.Rule:
				this.rules.push(child);
				child.rulable = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
}
