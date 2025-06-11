import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {DisplayableNode} from '../node_displayable.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

function draw_workflow_state(state, languages) {
	const state_html = document.createFullElement('li', {style: 'float: left;'});
	if(state.icon) {
		state_html.appendChild(document.createFullElement('img', {src: 'icon.png', alt: 'Icon'}));
	}
	state_html.appendChild(document.createTextNode(state.getLocalizedShortname(languages)));
	return state_html;
}

export class Workflow extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			orderBy: {type: 'number'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			states: {type: 'array', subtype: Entities.WorkflowState.name},
			actions: {type: 'array'},
			rules: {type: 'array'},
			mandatory: {type: 'boolean'},
			initialStateId: {type: 'string'},
			actionId: {type: 'string'},
			unique: {type: 'boolean'},
			aggregateWorkflowId: {type: 'string'},
			message: {type: 'object'},
			icon: {type: 'string'},
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
		this.study = undefined;
		this.id = undefined;
		this.orderBy = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.states = [];
		this.actions = [];
		this.rules = [];
		this.mandatory = true;
		this.initialStateId = undefined;
		this.actionId = undefined;
		this.unique = true;
		this.aggregateWorkflowId = undefined;
		this.message = {};
		this.icon = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getState(state_id) {
		return Utils.getObjectById(this.states, state_id);
	}
	getAction(action_id) {
		return Utils.getObjectById(this.actions, action_id);
	}
	getScopeModels() {
		return this.study.scopeModels.filter(s => s.workflowIds.includes(this.id));
	}
	getEventModels() {
		return this.study.getEventModels().filter(e => e.workflowIds.includes(this.id));
	}
	getFormModels() {
		return this.study.formModels.filter(p => p.workflowIds.includes(this.id));
	}
	getFieldModels() {
		return this.study.datasetModels.flatMap(d => d.fieldModels).filter(f => f.workflowIds.includes(this.id));
	}
	getWorkflowables() {
		const workflowables = [];
		workflowables.pushAll(this.getScopeModels());
		workflowables.pushAll(this.getEventModels());
		workflowables.pushAll(this.getFormModels());
		workflowables.pushAll(this.getFieldModels());
		return workflowables;
	}
	getValidators() {
		return this.study.validators.filter(v => v.workflowId === this.id);
	}
	getAggregatingWorkflows() {
		return this.study.workflows.filter(w => w.aggregateWorkflowId === this.id);
	}

	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.WorkflowState:
				return this.states.slice();
			case Entities.Action:
				return this.actions.slice();
			case Entities.Rule:
				return this.rules.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.WorkflowState:
				this.states.push(child);
				child.workflow = this;
				break;
			case Entities.Action:
				this.actions.push(child);
				child.workflow = this;
				break;
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
			case Entities.ScopeModel:
				return this.getScopeModels();
			case Entities.EventModel:
				return this.getEventModels();
			case Entities.FormModel:
				return this.getFormModels();
			case Entities.FieldModel:
				return this.getFieldModels();
			case Entities.Validator:
				return this.getValidators();
			case Entities.Workflow:
				return this.getAggregatingWorkflows();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onDeleteWorkflowState(event) {
		if(event.node.workflow === this && this.initialStateId === event.node.id) {
			this.initialStateId = undefined;
		}
		this.states.removeElement(event.node);
	}
	onChangeWorkflowStateId(event) {
		if(event.node.workflow === this && this.initialStateId && this.initialStateId === event.oldValue) {
			this.initialStateId = event.newValue;
		}
	}
	onMoveWorkflowState(event) {
		if(event.newParent === this) {
			event.node.workflow.states.removeElement(event.node);
			event.node.workflow = this;
			this.states.push(event.node);
		}
	}

	onDeleteWorkflow(event) {
		if(event.node.id && event.node.id === this.aggregateWorkflowId) {
			this.delete();
		}
	}
	onChangeWorkflowId(event) {
		if(event.node.id && event.node.id === this.aggregateWorkflowId) {
			this.aggregateWorkflowId = event.newValue;
		}
	}

	onDeleteAction(event) {
		if(event.node.workflow === this && this.actionId === event.node.id) {
			this.actionId = undefined;
		}
		this.actions.removeElement(event.node);
	}
	onChangeActionId(event) {
		if(event.node.workflow === this && this.actionId && this.actionId === event.oldValue) {
			this.actionId = event.newValue;
		}
	}
	onMoveAction(event) {
		if(event.newParent === this) {
			event.node.workflow.actions.removeElement(event.node);
			event.node.workflow = this;
			this.actions.push(event.node);
		}
	}

	onDeleteRule(event) {
		this.rules.removeElement(event.node);
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//check validation workflow
		const validator = this.study.validators.find(v => v.workflowId === this.id);
		if(validator) {
			if(this.aggregateWorkflowId) {
				report.addError(`Workflow ${this.id} cannot be both an aggregation and a validation workflow`);
			}
			if(this.states.length < 2) {
				report.addError(`Workflow ${this.id} is a validation workflow and must have at least two states`);
			}
			if(this.unique) {
				report.addError(
					`Workflow ${this.id} is a validation workflow and cannot be unique`,
					this,
					function() {
						this.unique = false;
					},
					'Make workflow unique');
			}
		}
		else {
			if(!this.aggregateWorkflowId && !this.initialStateId) {
				report.addError(`Workflow ${this.id} is neither a validation workflow nor an aggregation workflow and must have an initial state`);
			}
		}
		//unused
		if(!this.isUsed()) {
			report.addInfo(`Workflow ${this.id} is unused`, this, this['delete'], 'Delete workflow');
		}
		return report;
	}

	//html
	createHTML(languages) {
		return document.createElement('ul').appendChildren(this.states.map(s => draw_workflow_state(s, languages)));
	}
}
