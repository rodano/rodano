import {Injectable} from '@angular/core';
import {ScopeModelManagerService} from './manager/scope-model-manager.service';
import {DatasetModelManagerService} from './manager/dataset-model-manager.service';
import {FormModelManagerService} from './manager/form-model-manager.service';
import {WorkflowManagerService} from './manager/workflow-manager.service';
import {ProfileManagerService} from './manager/profile-manager.service';
import {WorkflowSummaryManagerService} from './manager/workflow-summary-manager.service';
import {WorkflowWidgetManagerService} from './manager/workflow-widget-manager.service';
import {CronManagerService} from './manager/cron-manager.service';
import {MenuManagerService} from './manager/menu-manager.service';
import {TimelineGraphManagerService} from './manager/timeline-graph-manager.service';
import {ReportManagerService} from './manager/report-manager.service';
import {ChartManagerService} from './manager/chart-manager.service';
import {ValidatorManagerService} from './manager/validator-manager.service';
import {EventModelManagerService} from './manager/event-model-manager.service';
import {EventGroupManagerService} from './manager/event-group-manager.service';
import {FieldModelManagerService} from './manager/field-model-manager.service';
import {WorkflowStateManagerService} from './manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from './manager/workflow-action-manager.service';
import {TimelineGraphSectionManagerService} from './manager/timeline-graph-section-manager.service';
import {PrivacyPolicyManagerService} from './manager/privacy-policy-manager.service';
import {ResourceCategoryManagerService} from './manager/resource-category-manager.service';
import { FormLayoutManagerService } from './manager/form-layout-manager.service';

export interface UsageResult {
	entityType: string;
	entityId: string;
	id: string;
	label: string;
	shortname?: Record<string, string>;
	title?: Record<string, string>;
}

@Injectable({providedIn: 'root'})
export class UsedByService {
	constructor(
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private formModelManager: FormModelManagerService,
		private formLayoutManager: FormLayoutManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private workflowSummaryManager: WorkflowSummaryManagerService,
		private workflowWidgetManager: WorkflowWidgetManagerService,
		private timelineGraphManager: TimelineGraphManagerService,
		private timelineGraphSectionManager: TimelineGraphSectionManagerService,
		private privacyPolicyManager: PrivacyPolicyManagerService,
		private resourceCategoryManager: ResourceCategoryManagerService,
		private profileManager: ProfileManagerService,
		private cronManager: CronManagerService,
		private menuManager: MenuManagerService,
		private reportManager: ReportManagerService,
		private chartManager: ChartManagerService,
		private validatorManager: ValidatorManagerService
	) {}

	getUsages(entityId: string): UsageResult[] {
		const allEntities = [
			...this.scopeModelManager.getAll().map(e => ({
				type: 'scope-model', id: e.scopeModelId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.eventModelManager.getAll().map(e => ({
				type: 'event-model', id: e.eventModelId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.eventGroupManager.getAll().map(e => ({
				type: 'event-group', id: e.eventGroupId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.datasetModelManager.getAll().map(e => ({
				type: 'dataset-model', id: e.datasetModelId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.fieldModelManager.getAll().map(e => ({
				type: 'field-model', id: e.fieldModelId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.formModelManager.getAll().map(e => ({
				type: 'form-model', id: e.formModelId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.formLayoutManager.getAll().map(e => ({
				type: 'form-layout', id: e.formLayoutId, label: e.id,
				shortname: undefined, title: undefined, obj: e
			})),
			...this.workflowManager.getAll().map(e => ({
				type: 'workflow', id: e.workflowId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.workflowStateManager.getAll().map(e => ({
				type: 'workflow-state', id: e.workflowStateId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.workflowActionManager.getAll().map(e => ({
				type: 'workflow-action', id: e.workflowActionId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.workflowSummaryManager.getAll().map(e => ({
				type: 'workflow-summary', id: e.workflowSummaryId, label: e.id,
				shortname: undefined, title: e.title, obj: e
			})),
			...this.workflowWidgetManager.getAll().map(e => ({
				type: 'workflow-widget', id: e.workflowWidgetId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.timelineGraphManager.getAll().map(e => ({
				type: 'timeline-graph', id: e.timelineGraphId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.timelineGraphSectionManager.getAll().map(e => ({
				type: 'timeline-graph-section', id: e.graphSectionId, label: e.id,
				shortname: e.label, title: undefined, obj: e
			})),
			...this.privacyPolicyManager.getAll().map(e => ({
				type: 'privacy-policy', id: e.policyId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.resourceCategoryManager.getAll().map(e => ({
				type: 'resource-category', id: e.categoryId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.profileManager.getAll().map(e => ({
				type: 'profile', id: e.profileId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.cronManager.getAll().map(e => ({
				type: 'cron', id: e.cronId, label: e.id,
				shortname: e.description, title: undefined, obj: e
			})),
			...this.menuManager.getAll().map(e => ({
				type: 'menu', id: e.menuId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.reportManager.getAll().map(e => ({
				type: 'report', id: e.reportId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.chartManager.getAll().map(e => ({
				type: 'chart', id: e.chartId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			})),
			...this.validatorManager.getAll().map(e => ({
				type: 'validator', id: e.validatorId, label: e.id,
				shortname: e.shortname, title: undefined, obj: e
			}))
		];

		return allEntities
			.filter(e => e.id !== entityId && this.containsId(e.obj, entityId))
			.map(e => ({
				entityType: e.type,
				entityId: e.id,
				id: e.label,
				label: e.label,
				shortname: e.shortname,
				title: e.title
			}));
	}

	private containsId(obj: any, targetId: string, visited = new Set<any>()): boolean {
		if(obj === null || obj === undefined) {
			return false;
		}
		if(visited.has(obj)) {
			return false;
		}
		if(typeof obj === 'string') {
			return obj === targetId;
		}
		if(typeof obj !== 'object') {
			return false;
		}

		visited.add(obj);
		return Object.values(obj).some(v => this.containsId(v, targetId, visited));
	}
}
