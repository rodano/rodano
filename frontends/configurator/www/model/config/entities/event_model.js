import {EntitiesHooks} from '../entities_hooks.js';
import {ComparatorUtils} from '../comparator_utils.js';
import {DisplayableNode} from '../node_displayable.js';
import {Study} from './study.js';
import {DateAggregationFunction} from '../date_aggregation_function.js';
import {EventTimeUnit} from '../event_time_unit.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';
import {ScopeModel} from './scope_model.js';

export class EventModel extends DisplayableNode {
	static getProperties() {
		return {
			scopeModel: {type: Entities.ScopeModel.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			eventGroupId: {type: 'string'},
			inceptive: {type: 'boolean'},
			datasetModelIds: {type: 'array'},
			formModelIds: {type: 'array'},
			workflowIds: {type: 'array'},
			number: {type: 'number'},
			mandatory: {type: 'boolean'},
			maxOccurrence: {type: 'number'},
			preventAdd: {type: 'boolean'},
			deadline: {type: 'number'},
			deadlineUnit: {type: 'string'},
			deadlineReferenceEventModelIds: {type: 'array'},
			deadlineAggregationFunction: {type: 'string'},
			interval: {type: 'number'},
			intervalUnit: {type: 'string'},
			impliedEventModelIds: {type: 'array'},
			blockedEventModelIds: {type: 'array'},
			labelPattern: {type: 'string'},
			icon: {type: 'string'},
			constraint: {type: Entities.RuleConstraint.name},
			createRules: {type: 'array', subtype: Entities.Rule.name},
			removeRules: {type: 'array', subtype: Entities.Rule.name},
			restoreRules: {type: 'array', subtype: Entities.Rule.name}
		};
	}
	static getExportComparator() {
		return (e1, e2) => ComparatorUtils.compareFields(e1, e2, ['number', 'id']);
	}
	static getSchedulingComparator() {
		return (e1, e2) => ComparatorUtils.compareField(e1, e2, EventModel.prototype.getDeadlineAbsolute);
	}
	static getComparator() {
		return EventModel.getExportComparator();
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT
	];

	constructor(values) {
		super();
		this.scopeModel = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.eventGroupId = undefined;
		this.inceptive = undefined;
		this.datasetModelIds = [];
		this.formModelIds = [];
		this.workflowIds = [];
		this.number = undefined;
		this.mandatory = undefined;
		this.maxOccurrence = undefined;
		this.preventAdd = undefined;
		this.deadline = undefined;
		this.deadlineUnit = undefined;
		this.deadlineReferenceEventModelIds = undefined;
		this.deadlineAggregationFunction = undefined;
		this.interval = undefined;
		this.intervalUnit = undefined;
		this.impliedEventModelIds = [];
		this.blockedEventModelIds = [];
		this.labelPattern = undefined;
		this.icon = undefined;
		this.constraint = undefined;
		this.createRules = [];
		this.removeRules = [];
		this.restoreRules = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getEventGroup() {
		return this.eventGroupId && this.scopeModel.getEventGroup(this.eventGroupId);
	}
	isPlanned() {
		return this.deadline && this.deadlineUnit && this.deadlineReferenceEventModelIds && !this.deadlineReferenceEventModelIds.isEmpty() && this.deadlineAggregationFunction;
	}
	getImpliedEventModels() {
		return this.impliedEventModelIds.map(ScopeModel.prototype.getEventModel, this.scopeModel);
	}
	getBlockedEventModels() {
		return this.blockedEventModelIds.map(ScopeModel.prototype.getEventModel, this.scopeModel);
	}
	getDeadlineReferenceEventModels() {
		return this.deadlineReferenceEventModelIds.map(ScopeModel.prototype.getEventModel, this.scopeModel);
	}
	getDeadlineAbsolute() {
		if(!this.isPlanned()) {
			return 0;
		}
		const reference_deadline = this.getDeadlineReferenceEventModels()
			.map(e => e.getDeadlineAbsolute())
			.reduce(DateAggregationFunction[this.deadlineAggregationFunction].accumulator);
		return reference_deadline + this.getDeadlineInSeconds();
	}
	getDeadlineInSeconds() {
		if(this.deadline) {
			return this.deadline * EventTimeUnit[this.deadlineUnit].seconds;
		}
		return 0;
	}
	getIntervalInSeconds() {
		if(this.interval) {
			return this.interval * EventTimeUnit[this.intervalUnit].seconds;
		}
		return 0;
	}
	getFormModels() {
		return this.formModelIds.map(Study.prototype.getFormModel, this.scopeModel.study);
	}
	getDatasetModels() {
		return this.datasetModelIds.map(Study.prototype.getDatasetModel, this.scopeModel.study);
	}
	getWorkflows() {
		return this.workflowIds.map(Study.prototype.getWorkflow, this.scopeModel.study);
	}

	//rulable and layoutable
	getStudy() {
		return this.scopeModel.study;
	}

	//tree
	getChildren(entity, index) {
		switch(entity) {
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
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
			case Entities.EventGroup: {
				const event_group = this.getEventGroup();
				return event_group ? [event_group] : [];
			}
			case Entities.DatasetModel:
				return this.getDatasetModels();
			case Entities.FormModel:
				return this.getFormModels();
			case Entities.Workflow:
				return this.getWorkflows();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}
	//entity event model is an exception because it is not possible to know if it is used based on the configuration
	//TODO in theory, "isUsed" should be consistent with "getUsage", this is not the case with event models
	isUsed () {
		return true;
	}

	//bus
	onChangeEventGroupId(event) {
		if(this.eventGroupId && this.eventGroupId === event.oldValue) {
			this.eventGroupId = event.newValue;
		}
	}
	onDeleteEventGroup(event) {
		if(this.eventGroupId === event.node.id) {
			this.eventGroupId = undefined;
		}
	}

	onChangeEventModelId(event) {
		this.impliedEventModelIds.replace(event.oldValue, event.newValue);
		this.blockedEventModelIds.replace(event.oldValue, event.newValue);
	}
	onDeleteEventModel(event) {
		this.impliedEventModelIds.removeElement(event.node.id);
		this.blockedEventModelIds.removeElement(event.node.id);
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

	onDeleteRule(event) {
		this.createRules.removeElement(event.node);
		this.removeRules.removeElement(event.node);
		this.restoreRules.removeElement(event.node);
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//event group must be mandatory if event is inceptive
		if(!this.mandatory && this.inceptive) {
			report.addError(`Event model ${this.id} is not mandatory whereas it is the inceptive event`);
		}
		return report;
	}
}
