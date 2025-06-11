import '../../../basic-tools/extension.js';
import {Entities} from '../entities.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {RuleEntities} from '../rule_entities.js';
import {Utils} from '../utils.js';

export class RuleConditionList extends Node {
	static getProperties() {
		return {
			constraint: {type: Entities.RuleConstraint.name, back_reference: true},
			mode: {type: 'string'},
			conditions: {type: 'array', subtype: Entities.RuleCondition.name}
		};
	}

	constructor(values) {
		super();
		this.constraint = undefined;
		this.mode = 'OR';
		this.conditions = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return `Rule condition list of type ${this.mode}`;
	}
	getRuleEntity() {
		const entry = Object.entries(this.constraint.conditions).find(e => e[1] === this);
		return RuleEntities[entry[0]];
	}
	getDescendantConditions() {
		return [...this.conditions.slice(), ...this.conditions.flatMap(c => c.getDescendantConditions())];
	}
	generateChildId(prefix) {
		return Utils.generateChildConditionId(this, prefix);
	}
	merge(rule_condition_list, aggressive) {
		return Utils.mergeConditions(this, rule_condition_list.conditions, aggressive);
	}
	getResults() {
		const rule_entity = this.getRuleEntity();
		const configuration_entity = rule_entity.getConfigurationEntity();

		//check if container entity is linked to rule condition list configuration entity to be able to restrain results
		let container = this.constraint.getContainer();
		const results = [];
		do {
			//container entity and list configuration entity are equal
			if(container.getEntity() === configuration_entity) {
				//console.log('return only container for entity', entity.name);
				//results are an array containing only the container
				results.push(container);
			}
			//container entity and list configuration entity are directly linked
			//for example, if container entity is "EventModel" and list entity is "DatasetModel"
			if(container.getEntity().isDirectlyRelatedTo(configuration_entity)) {
				//console.log('return only container relations for entity', entity.name);
				//results are an array containing all related entities
				results.pushAll(container.getRelations(configuration_entity).slice());
			}
			//do the same for the container parent
			//this way it will also work if container is an action or an field model, because their parent can be linked to the list configuration entity
		} while(container.hasParent() && (container = container.getParent()));

		//if results have been found, return them
		//TODO actually, all rule condition list configuration entities are necessary linked to the container by a way or another (via an ancestor or a relation)
		//therefore, results should always be returned (even if empty) but let's assume for now that code may be incorrect
		//BUG in fact, code is incorrect, because all relations may be used to retrieve results, not only direct relations
		//think about an field model, in a dataset model, linked to a scope model via an event; when retrieving results for entity scope model, the current code will miss relation to scope models
		if(!results.isEmpty()) {
			return results;
		}

		//in other cases return all node matching rule entity in study
		const study = this.constraint.constrainable.getStudy();
		switch(configuration_entity) {
			//event models must be retrieved from all scope models
			case Entities.EventModel:
				return study.scopeModels.flatMap(s => s.eventModels);
			//field models must be retrieved from all dataset models
			case Entities.FieldModel:
				return study.datasetModels.flatMap(d => d.fieldModels);
			default :
				return study.getChildren(configuration_entity);
		}
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleCondition:
				return this.conditions.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.RuleCondition:
				this.conditions.push(child);
				child.parent = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
}
