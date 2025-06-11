import '../../../basic-tools/extension.js';

import {DataType} from '../data_type.js';
import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Operator} from '../operator.js';

export class RuleConditionCriterion extends Node {
	static getProperties() {
		return {
			condition: {type: Entities.RuleCondition.name, back_reference: true},
			property: {type: 'string'},
			operator: {type: 'string'},
			values: {type: 'array'},
			conditionId: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.condition = undefined;
		this.property = undefined;
		this.operator = undefined;
		this.values = [];
		this.conditionId = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	isValid() {
		const study = this.condition.getConstraint().constrainable.getStudy();
		const entity = this.condition.getParent().getRuleEntity();
		if(this.property) {
			const property = study.getAllRuleDefinitionProperty(entity, this.property);
			if(property.type) {
				//criterion base on a property that has a type must have an operator
				if(!this.operator) {
					return false;
				}
				const type = DataType[property.type];
				const operator = Operator[this.operator];
				//operator must exists for the chosen type
				if(!type.operators.includes(operator)) {
					return false;
				}
				//if operator expects a value, it must exist
				if(operator.has_value ^ !this.values.isEmpty()) {
					return false;
				}
				//check values
				if([Operator.EQUALS, Operator.NOT_EQUALS].includes(operator)) {
					//check that criterion values are all available among results
					const node_ids = this.condition.getAvailableResults(property).map(n => n.id);
					return this.values.every(Array.prototype.includes, node_ids);
				}
			}
		}
		return true;
	}
	equals(other_criterion) {
		return this.property === other_criterion.property &&
			this.operator === other_criterion.operator &&
			Object.equals(this.values, other_criterion.values);
	}

	//bus
	onChangeRuleConditionId(event) {
		//there is something to do only if rule conditions are in the same constraint
		if(this.condition.getConstraint() === event.node.getConstraint() && this.conditionId === event.oldValue) {
			this.conditionId = event.newValue;
		}
	}

	onChange(event) {
		if(event.property === 'id') {
			if(this.condition.getRuleEntity().getConfigurationEntity().name === event.entity.name) {
				//TODO wrong for sub nodes
				this.values.replace(event.oldValue, event.newValue);
			}
		}
	}
}
