import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {StaticActions} from '../static_actions.js';
import {RuleEntities} from '../rule_entities.js';
import {Entities} from '../entities.js';

export class RuleAction extends Node {
	static getProperties() {
		return {
			rule: {type: Entities.Rule.name, back_reference: true},
			id: {type: 'string'},
			label: {type: 'object'},
			optional: {type: 'boolean'},
			configurationWorkflowId: {type: 'string'},
			configurationActionId: {type: 'string'},
			staticActionId: {type: 'string'},
			rulableEntity: {type: 'string'},
			conditionId: {type: 'string'},
			actionId: {type: 'string'},
			parameters: {type: 'array', subtype: Entities.RuleActionParameter.name}
		};
	}

	constructor(values) {
		super();
		this.rule = undefined;
		this.id = undefined;
		this.label = {};
		this.optional = false;
		this.configurationWorkflowId = undefined;
		this.configurationActionId = undefined;
		this.staticActionId = undefined;
		this.rulableEntity = undefined;
		this.conditionId = undefined;
		this.actionId = undefined;
		this.parameters = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.id || 'Action';
	}
	isProxy() {
		return this.configurationWorkflowId && this.configurationActionId;
	}
	getRuleEntity() {
		if(this.rulableEntity) {
			return RuleEntities[this.rulableEntity];
		}
		if(this.conditionId) {
			return this.rule.constraint.getCondition(this.conditionId).getRuleEntity();
		}
		throw new Error(`No rule entity for action ${this.id}`);
	}
	getDefinition() {
		if(this.isProxy()) {
			throw new Error(`No definition for proxy action ${this.id}`);
		}
		if(this.staticActionId) {
			return StaticActions[this.staticActionId];
		}
		if(this.rulableEntity || this.conditionId) {
			return this.rule.getStudy().getAllRuleDefinitionAction(this.getRuleEntity(), this.actionId);
		}
		throw new Error(`No definition for action ${this.id}`);
	}
	getParameter(parameter_id) {
		return Utils.getObjectById(this.parameters, parameter_id);
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.RuleActionParameter:
				return this.parameters.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.RuleActionParameter:
				this.parameters.push(child);
				child.action = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChangeRuleConditionId(event) {
		//there is something to do only if rule conditions are in the same rule
		if(this.rule === event.node.getConstraint().constrainable && this.conditionId === event.oldValue) {
			this.conditionId = event.newValue;
		}
	}

	//report
	report() {
		const report = new Report(this);
		//check if action is valid except for actions that call an other workflow action (proxy action)
		if(!this.isProxy()) {
			let definition;
			try {
				definition = this.getDefinition();
				//check parameters only for valid rule actions
				if(definition && this.parameters) {
					this.parameters.forEach(function(parameter) {
						let parameter_definition;
						try {
							parameter_definition = parameter.getDefinition();
							if(!parameter_definition.optional && !parameter.rulableEntity && !parameter.conditionId && !parameter.value) {
								report.addError(`Rule action parameter ${parameter.id} is empty in rule "${parameter.action.rule.description}"`);
							}
						}
						catch {
							//unable to find the definition
						}
						if(!parameter_definition) {
							report.addError(`Rule action parameter ${parameter.id} does not match any known parameters of action ${parameter.action.actionId} in rule "${parameter.action.rule.description}"`);
						}
					});
				}
			}
			catch {
				//unable to find the definition
			}
			if(!definition) {
				report.addError(`Rule action ${this.id} does not match any known actions in rule "${this.rule.description}"`);
			}
		}
		return report;
	}
}
