import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Constrainables, Rulables} from '../entities_categories.js';

export class RuleDefinitionProperty extends Node {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			label: {type: 'string'},
			entityId: {type: 'string'},
			target: {type: 'string'},
			type: {type: 'string'},
			configurationEntity: {type: 'string'},
			options: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.label = undefined;
		this.entityId = undefined;
		this.target = undefined;
		this.type = undefined;
		this.configurationEntity = undefined;
		this.options = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.label;
	}
	getConfigurationEntity() {
		return Entities[this.configurationEntity];
	}

	//tree
	getRelations(entity) {
		switch(entity) {
			case Entities.Rule: {
				const filter_constraint = constraint => {
					return constraint.getAllConditions().some(c => c.criterion.property && c.criterion.property === this.id && c.getEntity().name === this.entityId);
				};
				const nodes = [];
				//browse all rules in rulables
				nodes.pushAll(Rulables
					.flatMap(e => this.study.getDescendants(e))
					.flatMap(n => n.getAllChildren(Entities.Rule))
					.filter(r => filter_constraint(r.constraint)));
				//browser all contraints in constrainables
				nodes.pushAll(Constrainables
					.flatMap(e => this.study.getDescendants(e))
					.filter(n => n.getAllChildren(Entities.RuleConstraint).some(filter_constraint)));
				return nodes;
			}
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}
}
