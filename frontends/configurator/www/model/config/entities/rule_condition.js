import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {Rule} from './rule.js';
import {Operator} from '../operator.js';
import {RuleEntities} from '../rule_entities.js';
import {RuleConditionList} from './rule_condition_list.js';
import {RuleConditionCriterion} from './rule_condition_criterion.js';
import {Entities} from '../entities.js';

export class RuleCondition extends Node {
	static getProperties() {
		return {
			parent: {back_reference: true},
			id: {type: 'string'},
			criterion: {type: Entities.RuleConditionCriterion.name},
			inverse: {type: 'boolean'},
			dependency: {type: 'boolean'},
			breakType: {type: 'string'},
			mode: {type: 'string'},
			conditions: {type: 'array', subtype: Entities.RuleCondition.name}
		};
	}
	static parseFullDSL(text) {
		const entity = RuleEntities[text.substring(0, text.indexOf('.'))];
		//add separator between each condition
		const dsl = text.trim().substring(text.indexOf('.')).replace(/\[/g, '.[');
		const condition = RuleCondition.parseDSL(dsl);
		return {entity: entity, condition: condition};
	}
	static parseDSL(text) {
		//build model
		const condition = new RuleCondition();
		const criterion = new RuleConditionCriterion();
		condition.criterion = criterion;
		criterion.condition = condition;

		//isolate first condition
		const dsl = text.trim().substring(1);
		let condition_text = dsl.includes('.') ? dsl.substring(0, dsl.indexOf('.')) : dsl;
		let index = 0;

		//inverse
		if(condition_text[index] === '!') {
			condition.inverse = true;
			index++;
		}
		if(condition_text[index] === '[') {
			if(condition_text[condition_text.length - 1] !== ']') {
				throw new Error('Missing closing bracket');
			}
			//remove brackets
			condition_text = condition_text.substring(1, condition_text.length - 1);
			//find property
			let property = '';
			while(index < condition_text.length && condition_text.charCodeAt(index) >= 65 && condition_text.charCodeAt(index) <= 90) {
				property += condition_text[index];
				index++;
			}
			criterion.property = property;
			//find operator
			let symbol = '';
			while(index < condition_text.length && ['=', '!', '*', '<', '>'].includes(condition_text[index])) {
				symbol += condition_text[index];
				index++;
			}
			for(const operator in Operator) {
				if(Operator.hasOwnProperty(operator)) {
					if(Operator[operator].symbol === symbol) {
						criterion.operator = operator;
						break;
					}
				}
			}
			if(!criterion.operator) {
				throw new Error(`Wrong operator ${symbol}`);
			}
			//find values
			let values_text = condition_text.substring(index);
			if(values_text[0] === '(') {
				if(values_text[values_text.length - 1] !== ')') {
					throw new Error('Missing closing parenthesis');
				}
				//remove parenthesis
				values_text = values_text.substring(1, values_text.length - 1);
			}
			criterion.values = values_text.split('|');
		}
		else {
			criterion.property = condition_text.substring(index);
		}
		//continue on next condition
		if(dsl.includes('.')) {
			condition.conditions[0] = RuleCondition.parseDSL(dsl.substring(dsl.indexOf('.')));
		}
		return condition;
	}

	constructor(values) {
		super();
		this.parent = undefined;
		this.id = undefined;
		this.criterion = undefined;
		this.inverse = false;
		this.dependency = false;
		this.breakType = 'NONE';
		this.mode = 'OR';
		this.conditions = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.id;
	}
	//TODO delete this with an improvement on the reviver
	getConstraint() {
		let condition = this;
		while(condition.parent.constructor !== RuleConditionList) {
			condition = condition.parent;
		}
		return condition.parent.constraint;
	}
	getAncestorConditions() {
		const ancestors = [];
		let condition = this;
		while(condition.parent.constructor !== RuleConditionList) {
			condition = condition.parent;
			ancestors.push(condition);
		}
		return ancestors;
	}
	getDescendantConditions() {
		return [...this.conditions.slice(), ...this.conditions.flatMap(c => c.getDescendantConditions())];
	}
	generateChildId() {
		return Utils.generateChildConditionId(this, this.id);
	}
	getCondition(condition_id) {
		if(this.id === condition_id) {
			return this;
		}
		for(let i = this.conditions.length - 1; i >= 0; i--) {
			try {
				return this.conditions[i].getCondition(condition_id);
			}
			catch {
				//condition not found here
			}
		}
		throw new Error(`No condition with id ${condition_id} in condition with id ${this.id}`);
	}
	//retrieve result entity for a condition
	getRuleEntity() {
		//the last parent (the root ancestor) is a rule condition list
		const study = this.getConstraint().constrainable.getStudy();
		const target = study.getAllRuleDefinitionProperty(this.parent.getRuleEntity(), this.criterion.property).target;
		return RuleEntities[target];
	}
	getResults() {
		const results = this.parent.getResults();
		const study = this.getConstraint().constrainable.getStudy();
		const entity = this.parent.getRuleEntity();
		const property = study.getAllRuleDefinitionProperty(entity, this.criterion.property);
		//property leads to an other entity
		if(property.target) {
			const target = RuleEntities[property.target];
			//find configuration entity
			const configuration_entity = entity.getConfigurationEntity();
			//build new results list
			const new_results = [];
			results.forEach(function(result) {
				//specific function to jump
				if(property.jump) {
					try {
						new_results.pushAll(Function.isFunction(property.jump) ? property.jump.call(result) : result[property.jump]());
					}
					catch(exception) {
						//jump function may trigger an exception
						console.log(exception);
					}
				}
				//property does not lead to a different entity
				else if(target === entity) {
					//determine if result is valid
					let is_valid = false;
					//check result is valid for property id
					if(property.id === 'ID') {
						const operator = Operator[this.criterion.operator];
						if(operator.has_value) {
							is_valid = this.criterion.values.some(v => operator.test(result.id, v));
						}
						else {
							is_valid = operator.test(result.id);
						}
					}
					//assuming result is valid for an unrecognized property
					else {
						is_valid = true;
					}
					if(is_valid) {
						new_results.push(result);
					}
				}
				//property leads to a different entity
				else {
					const target_configuration_entity = target.getConfigurationEntity();
					//children
					if(configuration_entity.children.hasOwnProperty(target_configuration_entity.name)) {
						new_results.pushAll(result.getChildren(target_configuration_entity));
					}
					//relations
					else if(configuration_entity.relations.hasOwnProperty(target_configuration_entity.name)) {
						const relations = result.getRelations(target_configuration_entity);
						for(let i = 0; i < relations.length; i++) {
							if(!new_results.includes(relations[i])) {
								new_results.push(relations[i]);
							}
						}
					}
					//parent
					else {
						const parent = result.getParent();
						if(!new_results.includes(parent)) {
							new_results.push(parent);
						}
					}
				}
			}, this);
			return new_results;
		}
		return results;
	}
	getAvailableResults(property) {
		//retrieve available nodes for this condition
		const nodes = this.getParent().getResults();
		//retrieve configuration entity for the property and for the rule entity
		const property_entity = property.getConfigurationEntity();
		const rule_entity = this.getRuleEntity().getConfigurationEntity();
		//if property does not change target entity, available results are the same as for the parent condition
		if(property_entity === rule_entity) {
			return nodes;
		}
		//if property "moves" to an child entity, retrieve child nodes that match the target entity
		if(rule_entity.children.hasOwnProperty(property_entity.name)) {
			return nodes.flatMap(n => n.getChildren(property_entity));
		}
		//if property "moves" to a relation entity, retrieve related nodes that match the target entity
		if(rule_entity.relations.hasOwnProperty(property_entity.name)) {
			return nodes.flatMap(n => n.getRelations(property_entity));
		}
		//otherwise, return nodes matching entity from study
		return this.getConstraint().getContainer().getStudy().getChildren(property_entity);
	}
	isValid() {
		return this.criterion.isValid();
	}
	equals(other_condition) {
		let criterion_equals = this.criterion === undefined && other_condition.criterion === undefined;
		criterion_equals = criterion_equals || this.criterion.equals(other_condition.criterion);
		return criterion_equals &&
			this.inverse === other_condition.inverse &&
			this.breakType === other_condition.breakType &&
			this.mode === other_condition.mode;
	}
	merge(conditions, matching_ids) {
		return Utils.mergeConditions(this, conditions, matching_ids);
	}

	toDSL(entity) {
		let dsl = '';
		if(entity) {
			dsl += entity.name;
		}
		if(this.inverse) {
			dsl += '!';
		}
		//TODO do good check - on property
		if(this.criterion.operator) {
			dsl += (`[${this.criterion.property}`);
			const operator = Operator[this.criterion.operator];
			//TODO do good check - operator has 1 or 2 operands
			if(operator.symbol) {
				dsl += operator.symbol;
				if(this.criterion.values.length > 1) {
					dsl += (`(${this.criterion.values.join('|')})`);
				}
				else {
					dsl += this.criterion.values[0];
				}
			}
			dsl += ']';
		}
		else {
			dsl += (`.${this.criterion.property}`);
		}
		//TODO loop on conditions
		if(!this.conditions.isEmpty()) {
			dsl += this.conditions[0].toDSL();
		}
		return dsl;
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

	//report
	report() {
		const report = new Report(this);
		try {
			this.getRuleEntity();
		}
		catch {
			//retrieving entity throws an exception if the current condition criterion or one of the ancestor condition criteria is invalid
			//the goal here is to find the only condition with the invalid criterion
			const container = this.getConstraint().getContainer();
			const parent_condition = this.getParent();
			try {
				const parent_entity = parent_condition.getRuleEntity();
				let property_definition;
				try {
					property_definition = container.getStudy().getAllRuleDefinitionProperty(parent_entity, this.criterion.property);
				}
				catch {
					//the problem is in an ancestor condition criterion
				}
				if(!property_definition) {
					const container_id = container.constructor === Rule ? container.description : container.id;
					report.addError(`Rule condition criterion property ${this.criterion.property} does not match any known properties of entity ${parent_entity.label} in condition ${this.id} in node "${container_id}"`);
				}
			}
			catch {
				//the problem is in an ancestor condition criterion
			}
		}
		return report;
	}
}
