import '../../../basic-tools/extension.js';

import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Formulas} from '../formulas.js';
import {RuleEntities} from '../rule_entities.js';
import {Trigger} from '../trigger.js';
import {Rule} from './rule.js';
import {Study} from './study.js';

/**
 * @typedef {object} AutocompleteResult
 * @property {string} original - The submitted value
 * @property {string} validated_part - The beginning part the submitted value that has already been interpreted
 * @property {string} last_part - The rest of the submitted value
 * @property {any} last_function - The ongoing formula
 * @property {AutocompleteCondition} last_condition - The latest condition
 * @property {AutocompleteProposal[]} proposals - The proposals for the rest of the submitted value
 */

/**
 * @typedef {object} AutocompleteCondition
 * @property {string} id - The id of the condition
 * @property {any} entity - The entity for this condition
 */

/**
 * @typedef {object} AutocompleteProposal
 * @property {string} id - Id of the condition
 * @property {string} label - The container of the object that is currently being revived
 * @property {string} value - The container of the object that is currently being revived
 */

const FORMULA_SYMBOL = '=';
const FORMULA_SYNTAX = [':', '(', ',', ')'];

function generate_options(condition_id, properties) {
	return properties.
		filter(p => p.hasOwnProperty('type')).
		map(p => `=${condition_id}:${p.id}`);
}

export class RuleConstraint extends Node {
	static getProperties() {
		return {
			constrainable: {back_reference: true}, //do not specify type because constrainable could be anything
			conditions: {type: 'object'},
			evaluations: {type: 'array', subtype: Entities.RuleEvaluation.name}
		};
	}

	constructor(values) {
		super();
		this.constrainable = undefined;
		this.conditions = {};
		this.evaluations = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return `${this.constrainable.getLocalizedLabel(languages)} - Constraint`;
	}
	getContainer() {
		return this.constrainable.constructor === Rule ? this.constrainable.rulable : this.constrainable;
	}
	getEntities() {
		const container = this.getContainer();
		if(container.constructor === Study) {
			//retrieve good trigger
			const entry = Object.entries(container.eventActions).find(e => e[1].some(r => r.constraint === this));
			const trigger_id = entry[0];
			return Trigger[trigger_id].rule_entities;
		}
		return container.constructor.RuleEntities;
	}
	//retrieve condition from id
	getCondition(condition_id) {
		for(const condition_list of Object.values(this.conditions)) {
			for(let i = 0; i < condition_list.conditions.length; i++) {
				try {
					return condition_list.conditions[i].getCondition(condition_id);
				}
				catch {
					//condition not found here
				}
			}
		}
		throw new Error(`No condition with id ${condition_id}`);
	}
	//retrieve all available conditions except those at first level
	getAllConditions() {
		return Object.values(this.conditions).flatMap(c => c.getDescendantConditions());
	}
	//retrieve all available conditions including those at first level
	getAllConditionsIds() {
		const condition_ids = [
			//find ids for all conditions
			...this.getAllConditions().map(c => c.id),
			//add available entity ids
			...this.getEntities().map(e => e.name)
		];
		condition_ids.sort();
		return condition_ids;
	}
	//retrieve all conditions whose result is the specified entity
	getAllConditionsIdsForEntity(entity) {
		const condition_ids = [
			...this.getAllConditions().filter(c => c.getRuleEntity() === entity).map(c => c.id),
			entity.name
		];
		condition_ids.sort();
		return condition_ids;
	}
	getAllConditionsProperties() {
		return [
			//add first level conditions
			...this.getEntities().map(e => generate_options(e.name, e.properties)),
			//add other conditions
			...this.getAllConditions().map(c => generate_options(c.id, c.getRuleEntity().properties))
		];
	}
	merge(constraint, aggressive) {
		for(const [entity, condition_list] of Object.entries(constraint.conditions)) {
			if(this.conditions.hasOwnProperty(entity)) {
				this.conditions[entity].merge(condition_list, aggressive);
			}
			else {
				condition_list.constraint = this;
				this.conditions[entity] = condition_list;
			}
		}
	}
	/**
	 *
	 * @param {string} whole_value - The value (including the '=') to autocomplete
	 * @returns {AutocompleteResult} - The result
	 */
	autocomplete(whole_value) {
		const result = {
			original: whole_value,
			validated_part: undefined,
			last_part: undefined,
			last_function: undefined,
			last_condition: undefined,
			proposals: [],
		};
		//only formulas can be completed
		if(whole_value.startsWith(FORMULA_SYMBOL)) {
			//remove first "=" character
			const value = whole_value.substring(1);

			//retain pat of the formula that is complete
			let validated_part = FORMULA_SYMBOL;
			//retain last part and last condition id
			let last_part = '';
			let last_condition_id = undefined;
			let last_function_name = undefined;
			//retain current possibilities
			let possibilities = ['CONDITION', 'FORMULA'];

			//current position in formula
			let index = 0;
			while(index < value.length) {
				const character = value.charAt(index);

				//strip spaces
				if(character === ' ') {
					index++;
					continue;
				}

				//handle syntax characters
				if(FORMULA_SYNTAX.includes(character)) {
					//end of a condition
					if(character === ':') {
						possibilities = ['PROPERTY'];
						last_condition_id = last_part;
					}
					//a comma ends a function parameter
					//a parenthesis starts a function call or a sub-operation
					else if(character === ',' || character === '(' || character === ')') {
						possibilities = ['CONDITION', 'FORMULA'];
						last_condition_id = undefined;
						if(character === '(') {
							last_function_name = last_part;
						}
						if(character === ')') {
							last_function_name = undefined;
						}
					}
					validated_part += `${last_part}${character}`;
					last_part = '';
					index++;
					continue;
				}

				last_part += character;
				index++;
			}

			//convert partial value to lower case to make a case insensitive search
			last_part = last_part.toLowerCase();

			//build list of results
			const study = this.getContainer().getStudy();
			result.validated_part = validated_part;
			result.last_part = last_part;
			if(last_condition_id) {
				//retrieve condition entity or entity itself
				let entity = undefined;
				//id is the id a condition
				try {
					const condition = this.getCondition(last_condition_id);
					entity = condition.getRuleEntity();
				}
				//id is the name of a rule entity
				catch {
					entity = RuleEntities[last_condition_id];
				}
				result.last_condition = {
					id: last_condition_id,
					entity: entity
				};
			}
			if(last_function_name) {
				result.last_function = Formulas[last_function_name];
			}

			//add different types of proposals
			if(possibilities.includes('PROPERTY')) {
				try {
					let properties = study.getAllRuleDefinitionProperties(result.last_condition.entity);
					//filter conditions matching partial value
					if(last_part) {
						properties = properties.filter(p => p.id.toLowerCase().includes(last_part));
						//no need to autocomplete if last part is already complete
						properties = properties.filter(p => p.id.toLowerCase() !== last_part);
					}
					result.proposals.pushAll(properties.map(property => {
						return {
							id: property.id,
							label: property.label,
							value: `${validated_part}${property.id}`
						};
					}));
				}
				catch(error) {
					console.error(`Unable to autocomplete: ${error}`);
				}
			}
			if(possibilities.includes('CONDITION')) {
				let conditions_ids = this.getAllConditionsIds();
				//filter conditions matching partial value
				if(last_part) {
					conditions_ids = conditions_ids.filter(i => i.toLowerCase().startsWith(last_part));
					//no need to autocomplete if last part is already complete
					conditions_ids = conditions_ids.filter(i => i.toLowerCase() !== last_part);
				}
				result.proposals.pushAll(conditions_ids.map(id => {
					return {
						id: id,
						label: `Condition ${id}`,
						value: `${validated_part}${id}:`
					};
				}));
			}
			if(possibilities.includes('FORMULA')) {
				let formulas = Object.entries(Formulas);
				//filter formulas matching partial value
				if(last_part) {
					formulas = formulas.filter(e => e[0].toLowerCase().startsWith(last_part));
					//no need to autocomplete if last part is already complete
					formulas = formulas.filter(e => e[0].toLowerCase() !== last_part);
				}
				result.proposals.pushAll(formulas.map(entry => {
					return {
						id: entry[0],
						label: entry[1].label,
						value: `${validated_part}${entry[0]}(${entry[1].parameters.isEmpty() ? ')' : ''}`
					};
				}));
			}
		}
		return result;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleConditionList:
				return Object.values(this.conditions);
			case Entities.RuleEvaluation:
				return this.evaluations.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.RuleEvaluation:
				this.evaluations.push(child);
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
}
