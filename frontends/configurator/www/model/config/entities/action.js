import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';
import {RuleEntities} from '../rule_entities.js';

export class Action extends DisplayableNode {
	static getProperties() {
		return {
			workflow: {type: Entities.Workflow.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			rules: {type: 'array', subtype: Entities.Rule.name},
			documentable: {type: 'boolean'},
			documentableOptions: {type: 'array'},
			requireSignature: {type: 'boolean'},
			requiredSignatureText: {type: 'object'},
			icon: {type: 'string'}
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.FORM,
		RuleEntities.DATASET,
		RuleEntities.FIELD,
		RuleEntities.WORKFLOW
	];

	constructor(values) {
		super();
		this.workflow = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.rules = [];
		this.documentable = false;
		this.documentableOptions = [];
		this.requireSignature = undefined;
		this.requiredSignatureText = {};
		this.icon = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//rulable and layoutable
	getStudy() {
		return this.workflow.study;
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
	getRelations(entity) {
		switch(entity) {
			case Entities.WorkflowState:
				return this.workflow.states.filter(s => s.possibleActionIds.includes(this.id));
			case Entities.Workflow:
				return this.workflow.actionId === this.id ? [this.workflow] : [];
			case Entities.Action:
				//retrieve all rules actions triggering a workflow action
				return this.workflow.study.workflows.flatMap(w => w.actions).filter(function(action) {
					return action.rules.flatMap(r => r.actions).some(a => a.configurationWorkflowId === action.workflow.id && a.configurationActionId && action.id);
				});
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onDeleteRule(event) {
		this.rules.removeElement(event.node);
	}
}
