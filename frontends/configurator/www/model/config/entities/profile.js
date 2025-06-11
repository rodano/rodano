import {EntitiesHooks} from '../entities_hooks.js';
import {ComparatorUtils} from '../comparator_utils.js';
import {DisplayableNode} from '../node_displayable.js';
import {Entities} from '../entities.js';

const assignables = {
	[Entities.Feature.name]: 'grantedFeatureIds',
	[Entities.ResourceCategory.name]: 'grantedCategoryIds',
	[Entities.TimelineGraph.name]: 'grantedTimelineGraphIds',
	[Entities.Menu.name]: 'grantedMenuIds',
	[Entities.Report.name]: 'grantedReportIds'
};

const right_assignables = {
	[Entities.Profile.name]: 'grantedProfileIdRights',
	[Entities.ScopeModel.name]: 'grantedScopeModelIdRights',
	[Entities.EventModel.name]: 'grantedEventModelIdRights',
	[Entities.DatasetModel.name]: 'grantedDatasetModelIdRights',
	[Entities.FormModel.name]: 'grantedFormModelIdRights',
	[Entities.PaymentPlan.name]: 'grantedPaymentIdRights',
};

export class Profile extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			workflowIdOfInterest: {type: 'string'},
			orderBy: {type: 'number'},
			grantedProfileIdRights: {type: 'object'},
			grantedDatasetModelIdRights: {type: 'object'},
			grantedScopeModelIdRights: {type: 'object'},
			grantedPaymentIdRights: {type: 'object'},
			grantedEventModelIdRights: {type: 'object'},
			grantedFormModelIdRights: {type: 'object'},
			grantedWorkflowIds: {type: 'object'},
			grantedFeatureIds: {type: 'array'},
			grantedMenuIds: {type: 'array'},
			grantedCategoryIds: {type: 'array'},
			grantedTimelineGraphIds: {type: 'array'},
			grantedReportIds: {type: 'array'}
		};
	}
	static getOrderComparator() {
		return (p1, p2) => ComparatorUtils.compareFields(p1, p2, ['orderBy', 'id']);
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.workflowIdOfInterest = undefined;
		this.orderBy = undefined;
		this.grantedProfileIdRights = {};
		this.grantedDatasetModelIdRights = {};
		this.grantedScopeModelIdRights = {};
		this.grantedPaymentIdRights = {};
		this.grantedEventModelIdRights = {};
		this.grantedFormModelIdRights = {};
		this.grantedWorkflowIds = {};
		this.grantedFeatureIds = [];
		this.grantedMenuIds = [];
		this.grantedCategoryIds = [];
		this.grantedTimelineGraphIds = [];
		this.grantedReportIds = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//assignables
	getAssignables(entity) {
		return this[assignables[entity.name]];
	}
	assign(entity, assignable_id) {
		if(!this[assignables[entity.name]].includes(assignable_id)) {
			this[assignables[entity.name]].push(assignable_id);
		}
	}
	assignNode(node) {
		this.assign(node.getEntity(), node.id);
	}
	unassign(entity, assignable_id) {
		this[assignables[entity.name]].removeElement(assignable_id);
	}
	unassignNode(node) {
		this.unassign(node.getEntity(), node.id);
	}
	isAssigned(entity, assignable_id) {
		return this[assignables[entity.name]].includes(assignable_id);
	}
	isAssignedNode(node) {
		return this.isAssigned(node.getEntity(), node.id);
	}

	//right assignables
	assignRight(entity, assignable_id, right) {
		const profile_right = this[right_assignables[entity.name]];
		if(!profile_right[assignable_id]) {
			profile_right[assignable_id] = [];
		}
		if(!profile_right[assignable_id].includes(right)) {
			profile_right[assignable_id].push(right);
		}
	}
	assignRightNode(node, right) {
		this.assignRight(node.getEntity(), node.id, right);
	}
	unassignRight(entity, assignable_id, right) {
		this[right_assignables[entity.name]][assignable_id].removeElement(right);
	}
	unassignRightNode(node, right) {
		this.unassignRight(node.getEntity(), node.id, right);
	}
	isAssignedRight(entity, assignable_id, right) {
		const rights = this[right_assignables[entity.name]][assignable_id];
		return !!rights && rights.includes(right);
	}
	isAssignedRightNode(node, right) {
		return this.isAssignedRight(node.getEntity(), node.id, right);
	}

	//bus
	onChangeProfileId(event) {
		if(event.oldValue) {
			this.grantedProfileIdRights[event.newValue] = this.grantedProfileIdRights[event.oldValue];
			delete this.grantedProfileIdRights[event.oldValue];
		}
	}
	onDeleteProfile(event) {
		delete this.grantedProfileIdRights[event.node.id];
	}

	onChangeScopeModelId(event) {
		if(event.oldValue) {
			this.grantedScopeModelIdRights[event.newValue] = this.grantedScopeModelIdRights[event.oldValue];
			delete this.grantedScopeModelIdRights[event.oldValue];
		}
	}
	onDeleteScopeModel(event) {
		delete this.grantedScopeModelIdRights[event.node.id];
	}

	onChangePaymentPlanId(event) {
		if(event.oldValue) {
			this.grantedPaymentIdRights[event.newValue] = this.grantedPaymentIdRights[event.oldValue];
			delete this.grantedPaymentIdRights[event.oldValue];
		}
	}
	onDeletePaymentPlan(event) {
		delete this.grantedPaymentIdRights[event.node.id];
	}

	onChangeDatasetModelId(event) {
		if(event.oldValue) {
			this.grantedDatasetModelIdRights[event.newValue] = this.grantedDatasetModelIdRights[event.oldValue];
			delete this.grantedDatasetModelIdRights[event.oldValue];
		}
	}
	onDeleteDatasetModel(event) {
		delete this.grantedDatasetModelIdRights[event.node.id];
	}

	onChangeFormModelId(event) {
		if(event.oldValue) {
			this.grantedFormModelIdRights[event.newValue] = this.grantedFormModelIdRights[event.oldValue];
			delete this.grantedFormModelIdRights[event.oldValue];
		}
	}
	onDeleteFormModel(event) {
		delete this.grantedFormModelIdRights[event.node.id];
	}

	onChangeEventModelId(event) {
		if(event.oldValue) {
			this.grantedEventModelIdRights[event.newValue] = this.grantedEventModelIdRights[event.oldValue];
			delete this.grantedEventModelIdRights[event.oldValue];
		}
	}
	onDeleteEventModel(event) {
		delete this.grantedEventModelIdRights[event.node.id];
	}

	onChangeFeatureId(event) {
		this.grantedFeatureIds.replace(event.oldValue, event.newValue);
	}
	onDeleteFeature(event) {
		this.grantedFeatureIds.removeElement(event.node.id);
	}

	onChangeMenuId(event) {
		this.grantedMenuIds.replace(event.oldValue, event.newValue);
	}
	onDeleteMenu(event) {
		this.grantedMenuIds.removeElement(event.node.id);
	}

	onChangeResourceCategoryId(event) {
		this.grantedCategoryIds.replace(event.oldValue, event.newValue);
	}
	onDeleteResourceCategory(event) {
		this.grantedCategoryIds.removeElement(event.node.id);
	}

	onChangeReportId(event) {
		this.grantedReportIds.replace(event.oldValue, event.newValue);
	}
	onDeleteReport(event) {
		this.grantedReportIds.removeElement(event.node.id);
	}

	onChangeTimelineGraphId(event) {
		this.grantedTimelineGraphIds.replace(event.oldValue, event.newValue);
	}
	onDeleteTimelineGraph(event) {
		this.grantedTimelineGraphIds.removeElement(event.node.id);
	}

	onChangeWorkflowId(event) {
		this.grantedWorkflowIds[event.newValue] = this.grantedWorkflowIds[event.oldValue];
		delete this.grantedWorkflowIds[event.oldValue];
	}
	onDeleteWorkflow(event) {
		delete this.grantedWorkflowIds[event.node.id];
	}

	onChangeActionId(event) {
		if(this.grantedWorkflowIds[event.node.workflow.id]) {
			const child_rights = this.grantedWorkflowIds[event.node.workflow.id].childRights;
			if(child_rights[event.oldValue]) {
				child_rights[event.newValue] = child_rights[event.oldValue];
				delete child_rights[event.oldValue];
			}
		}
	}
	onDeleteAction(event) {
		if(this.grantedWorkflowIds[event.node.workflow.id]) {
			delete this.grantedWorkflowIds[event.node.workflow.id].childRights[event.node.id];
		}
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//TODO make this generic, using ProfileRightAssignables
		for(const [workflow_id, right] of Object.entries(this.grantedWorkflowIds)) {
			if(right && !right.right) {
				const has_child_right = Object.values(right.childRights).some(r => r.system || !r.profileIds.isEmpty());
				if(has_child_right) {
					report.addError(
						`Profile ${this.id} has right on at least one action of workflow ${workflow_id} but not on the workflow itself`,
						this,
						(function(workflow_id) {
							return function() {
								//TODO assign workflow the right way using profile rights method
								this.grantedWorkflowIds[workflow_id].right = true;
							};
						})(workflow_id),
						'Give right on workflow to this profile'
					);
				}
			}
		}
		return report;
	}
}
