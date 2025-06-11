import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Rulables} from '../entities_categories.js';
import {Utils} from './../utils.js';

export class RuleDefinitionAction extends Node {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			label: {type: 'string'},
			entityId: {type: 'string'},
			parameters: {type: 'array', subtype: Entities.RuleDefinitionActionParameter.name}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.label = undefined;
		this.entityId = undefined;
		this.parameters = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.label;
	}
	getParameter(parameter_id) {
		return Utils.getObjectById(this.parameters, parameter_id);
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleDefinitionActionParameter:
				return this.parameters.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.RuleDefinitionActionParameter:
				this.parameters.push(child);
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
	getRelations(entity) {
		switch(entity) {
			case Entities.Rule:
				//FIXME do no use "this.study.getDescendants(Rule)" because this would return only the rules directly attached to the study (i.e. the triggers)
				//this is because getDescendants method browses only one "path" between a node and the parameter entity
				//as soon as getDescendants find a path between Study and Rule, it won't go fetch the rules from other descendants (EventGroup, Document, etc)
				return Rulables
					.flatMap(e => this.study.getDescendants(e))
					.flatMap(n => n.getAllChildren(Entities.Rule))
					.filter(r => r.actions.some(a => a.actionId && a.actionId === this.id && a.getRuleEntity().name === this.entityId));
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onDeleteRuleDefinitionActionParameter(event) {
		this.parameters.removeElement(event.node);
	}
}
