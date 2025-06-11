import '../../../basic-tools/extension.js';

import {DisplayableNode} from '../node_displayable.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Study} from './study.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';
import {Utils} from '../utils.js';

export class ScopeModel extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			pluralShortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			defaultParentId: {type: 'string'},
			parentIds: {type: 'array'},
			virtual: {type: 'boolean'},
			maxNumber: {type: 'number'},
			expectedNumber: {type: 'string'},
			scopeFormat: {type: 'string'},
			eventGroups: {type: 'array', subtype: Entities.EventGroup.name},
			eventModels: {type: 'array', subtype: Entities.EventModel.name},
			datasetModelIds: {type: 'array'},
			formModelIds: {type: 'array'},
			workflowIds: {type: 'array'},
			defaultProfileId: {type: 'string'},
			layout: {type: 'string'},
			workflowStatesSelectors: {type: 'array'},
			createRules: {type: 'array', subtype: Entities.Rule.name},
			removeRules: {type: 'array', subtype: Entities.Rule.name},
			restoreRules: {type: 'array', subtype: Entities.Rule.name}
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE
	];

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.pluralShortname = {};
		this.longname = {};
		this.description = {};
		this.defaultParentId = undefined;
		this.parentIds = [];
		this.virtual = false;
		this.maxNumber = undefined;
		this.expectedNumber = undefined;
		this.scopeFormat = undefined;
		this.eventGroups = [];
		this.eventModels = [];
		this.datasetModelIds = [];
		this.formModelIds = [];
		this.workflowIds = [];
		this.defaultProfileId = undefined;
		this.layout = undefined;
		this.workflowStatesSelectors = [];
		this.createRules = [];
		this.removeRules = [];
		this.restoreRules = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getScopeModelDefaultParent() {
		return this.defaultParentId ? this.study.getScopeModel(this.defaultParentId) : undefined;
	}
	getScopeModelParents() {
		return this.parentIds.map(Study.prototype.getScopeModel, this.study);
	}
	getScopeModelAncestors() {
		const ancestors = [];
		const parents = this.getScopeModelParents();
		ancestors.pushAll(parents);
		parents.forEach(function(parent) {
			parent.getScopeModelAncestors().forEach(function(ancestor) {
				if(!ancestors.includes(ancestor)) {
					ancestors.push(ancestor);
				}
			});
		});
		return ancestors;
	}
	getScopeModelChildren() {
		const scope_model_id = this.id;
		return this.study.scopeModels.filter(s => s.parentIds.includes(scope_model_id));
	}
	getScopeModelDescendants() {
		const scope_model = this;
		return this.study.scopeModels.filter(s => s.getScopeModelAncestors().includes(scope_model));
	}
	getScopeModelBranch(scope_model) {
		const scope_models = [];
		scope_models.push(this);
		if(this !== scope_model) {
			if(this.parentIds.isEmpty()) {
				throw new Error(`${scope_models[0].id} and ${scope_model.id} are not on the same branch`);
			}
			if(this.parentIds.includes(scope_model.id)) {
				scope_models.push(scope_model);
			}
			else {
				const parent_scope_models = this.getScopeModelParents();
				for(let i = 0; i < parent_scope_models.length; i++) {
					const branch_scope_models = parent_scope_models[i].getScopeModelBranch(scope_model);
					for(let j = 0; j < branch_scope_models.length; j++) {
						const branch_scope_model = branch_scope_models[j];
						if(!scope_models.includes(branch_scope_model)) {
							scope_models.push(branch_scope_model);
						}
					}
				}
			}
		}
		return scope_models;
	}
	isRoot() {
		return this.parentIds.isEmpty();
	}
	isLeaf() {
		return this.study.getLeafScopeModel().id === this.id;
	}
	getEventModel(event_model_id) {
		return Utils.getObjectById(this.eventModels, event_model_id);
	}
	getEventGroup(event_group_id) {
		return Utils.getObjectById(this.eventGroups, event_group_id);
	}
	getWorkflows() {
		return this.workflowIds.map(Study.prototype.getWorkflow, this.study);
	}
	getDatasetModels() {
		return this.datasetModelIds.map(Study.prototype.getDatasetModel, this.study);
	}
	getFormModels() {
		return this.formModelIds.map(Study.prototype.getFormModel, this.study);
	}
	getInceptiveEventModel() {
		const inceptive_event_model = this.eventModels.find(e => e.inceptive);
		if(inceptive_event_model) {
			return inceptive_event_model;
		}
		else {
			throw new Error(`No inceptive event model for scope model ${this.id}`);
		}
	}

	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity, index) {
		switch(entity) {
			case Entities.EventGroup:
				return this.eventGroups.slice();
			case Entities.EventModel:
				return this.eventModels.slice();
			case Entities.CMSLayout:
				return this.layout ? [this.layout] : [];
			case Entities.Rule:
				if(index === 0) {
					return this.createRules.slice();
				}
				else if(index === 1) {
					return this.removeRules.slice();
				}
				else if(index === 2) {
					return this.restoreRules.slice();
				}
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child, index) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.EventGroup:
				this.eventGroups.push(child);
				child.scopeModel = this;
				break;
			case Entities.EventModel:
				this.eventModels.push(child);
				child.scopeModel = this;
				break;
			case Entities.Rule:
				if(index === 0) {
					this.createRules.push(child);
				}
				else if(index === 1) {
					this.removeRules.push(child);
				}
				else if(index === 2) {
					this.restoreRules.push(child);
				}
				child.rulable = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
	getRelations(entity) {
		switch(entity) {
			case Entities.DatasetModel:
				return this.getDatasetModels();
			case Entities.FormModel:
				return this.getFormModels();
			case Entities.Workflow:
				return this.getWorkflows();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}
	//entity scope model is an exception because it is not possible to know if it is used based on the configuration
	//TODO in theory, "isUsed" should be consistent with "getUsage", this is not the case with scope models
	isUsed () {
		return true;
	}

	//bus
	onChangeScopeModelId(event) {
		this.parentIds.replace(event.oldValue, event.newValue);
		if(this.defaultParentId && this.defaultParentId === event.oldValue) {
			this.defaultParentId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		this.parentIds.removeElement(event.node.id);
		if(this.defaultParentId === event.node.id) {
			this.defaultParentId = undefined;
		}
	}

	onDeleteEventGroup(event) {
		this.eventGroups.removeElement(event.node);
	}
	onMoveEventGroup(event) {
		if(event.newParent === this) {
			event.node.scopeModel.eventGroups.removeElement(event.node);
			event.node.scopeModel = this;
			this.eventGroups.push(event.node);
		}
	}

	onDeleteEventModel(event) {
		this.eventModels.removeElement(event.node);
	}
	onMoveEventModel(event) {
		if(event.newParent === this) {
			event.node.scopeModel.eventModels.removeElement(event.node);
			event.node.scopeModel = this;
			this.eventModels.push(event.node);
		}
	}

	onChangeDatasetModelId(event) {
		this.datasetModelIds.replace(event.oldValue, event.newValue);
	}
	onDeleteDatasetModel(event) {
		this.datasetModelIds.removeElement(event.node.id);
	}

	onChangeFormModelId(event) {
		this.formModelIds.replace(event.oldValue, event.newValue);
	}
	onDeleteFormModel(event) {
		this.formModelIds.removeElement(event.node.id);
	}

	onChangeWorkflowId(event) {
		this.workflowIds.replace(event.oldValue, event.newValue);
	}
	onDeleteWorkflow(event) {
		this.workflowIds.removeElement(event.node.id);
	}

	onDeleteWorkflowStatesSelector(event) {
		this.workflowStatesSelectors.removeElement(event.node);
	}
	onMoveWorkflowStatesSelector(event) {
		if(event.newParent === this) {
			event.node.parent.workflowStatesSelectors.removeElement(event.node);
			event.node.parent = this;
			this.workflowStatesSelectors.push(event.node);
		}
	}

	onChangeProfileId(event) {
		if(this.defaultProfileId && this.defaultProfileId === event.oldValue) {
			this.defaultProfileId = event.newValue;
		}
	}
	onDeleteProfile(event) {
		if(this.defaultProfileId === event.node.id) {
			this.defaultProfileId = undefined;
		}
	}

	onDeleteRule(event) {
		this.createRules.removeElement(event.node);
		this.removeRules.removeElement(event.node);
		this.restoreRules.removeElement(event.node);
	}
}
