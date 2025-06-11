import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {RuleEntities} from '../rule_entities.js';

export class RuleDefinitionActionParameter extends Node {
	static getProperties() {
		return {
			id: {type: 'string'},
			label: {type: 'string'},
			type: {type: 'string'},
			dataEntity: {type: 'string'},
			configurationEntity: {type: 'string'},
			options: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.id = undefined;
		this.label = undefined;
		this.type = undefined;
		this.dataEntity = undefined;
		this.configurationEntity = undefined;
		this.options = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getConfigurationEntity() {
		return Entities[this.configurationEntity];
	}
	getDataEntity() {
		return RuleEntities[this.dataEntity];
	}
}
