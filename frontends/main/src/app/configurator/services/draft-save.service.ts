import {Injectable} from '@angular/core';
import {ScopeModelService} from './api/scope-model.service';
import {EventModelService} from './api/event-model.service';
import {EventGroupService} from './api/event-group.service';
import {DatasetModelService} from './api/dataset-model.service';
import {FieldModelService} from './api/field-model.service';
import {ScopeModel} from '@core/model/scope-model';
import {forkJoin, map, Observable, of} from 'rxjs';
import {EventModel} from '@core/model/event-model';
import {EventGroup} from '@core/model/event-group';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModel} from '@core/model/field-model';
import {Validator} from '@core/model/validator';
import {ValidatorService} from './api/validator.service';
import {WorkflowService} from './api/workflow.service';
import {WorkflowStateService} from './api/workflow-state.service';
import {WorkflowActionService} from './api/workflow-action.service';
import {Workflow} from '@core/model/workflow';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowAction} from '@core/model/workflow-action';
import {Profile} from '@core/model/profile';
import {ProfileService} from './api/profile.service';
import {FeatureService} from './api/feature.service';
import {Feature} from '@core/model/feature';
import {PrivacyPolicyService} from './api/privacy-policy.service';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {ResourceCategoryService} from './api/resource-category.service';
import {ResourceCategory} from '@core/model/resource-category';
import {ReportService} from './api/report.service';
import {Report} from '@core/model/report';
import {ChartService} from './api/chart.service';
import {ChartModel} from '@core/model/chart-model';

@Injectable({providedIn: 'root'})
export class DraftSaveService {
	constructor(
		private scopeModelService: ScopeModelService,
		private eventModelService: EventModelService,
		private eventGroupService: EventGroupService,
		private datasetModelService: DatasetModelService,
		private fieldModelService: FieldModelService,
		private validatorService: ValidatorService,
		private workflowService: WorkflowService,
		private workflowStateService: WorkflowStateService,
		private workflowActionService: WorkflowActionService,
		private profileService: ProfileService,
		private featureService: FeatureService,
		private privacyPolicyService: PrivacyPolicyService,
		private resourceCategoryService: ResourceCategoryService,
		private reportService: ReportService,
		private chartService: ChartService
	) {}

	saveScopeModels(
		projectId: string,
		modifiedIds: Set<string>,
		scopeModels: ScopeModel[],
		originalScopeModels: ScopeModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalScopeModels.find(sm => sm.scopeModelId === originalId);
				if(original) {
					saveObservables.push(this.scopeModelService.deleteScopeModel(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const scopeModel = scopeModels.find(sm => sm.scopeModelId === id);
				if(scopeModel) {
					saveObservables.push(this.scopeModelService.createScopeModel(projectId, scopeModel));
				}
			}
			else {
				const scopeModel = scopeModels.find(sm => sm.scopeModelId === id);
				if(scopeModel) {
					saveObservables.push(this.scopeModelService.updateScopeModel(projectId, id, scopeModel));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveEventModels(
		projectId: string,
		modifiedIds: Set<string>,
		eventModels: EventModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const eventModel = eventModels.find(em => em.eventModelId === id);
			if(eventModel) {
				saveObservables.push(this.eventModelService.updateEventModel(projectId, id, eventModel));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveEventGroups(
		projectId: string,
		modifiedIds: Set<string>,
		eventGroups: EventGroup[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const eventGroup = eventGroups.find(eg => eg.eventGroupId === id);
			if(eventGroup) {
				saveObservables.push(this.eventGroupService.updateEventGroup(projectId, id, eventGroup));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveDatasetModels(
		projectId: string,
		modifiedIds: Set<string>,
		datasetModels: DatasetModel[],
		originalDatasetModels: DatasetModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalDatasetModels.find(dm => dm.datasetModelId === originalId);
				if(original) {
					saveObservables.push(this.datasetModelService.deleteDatasetModel(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const datasetModel = datasetModels.find(dm => dm.datasetModelId === id);
				if(datasetModel) {
					saveObservables.push(this.datasetModelService.createDatasetModel(projectId, datasetModel));
				}
			}
			else {
				const datasetModel = datasetModels.find(dm => dm.datasetModelId === id);
				if(datasetModel) {
					saveObservables.push(this.datasetModelService.updateDatasetModel(projectId, id, datasetModel));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveFieldModels(
		projectId: string,
		modifiedIds: Set<string>,
		fieldModels: FieldModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const fieldModel = fieldModels.find(fm => fm.fieldModelId === id);
			if(fieldModel) {
				saveObservables.push(this.fieldModelService.updateFieldModel(projectId, id, fieldModel));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveValidators(
		projectId: string,
		modifiedIds: Set<string>,
		validators: Validator[],
		originalValidators: Validator[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalValidators.find(v => v.validatorId === originalId);
				if(original) {
					saveObservables.push(this.validatorService.deleteValidator(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const validator = validators.find(v => v.validatorId === id);
				if(validator) {
					saveObservables.push(this.validatorService.createValidator(projectId, validator));
				}
			}
			else {
				const validator = validators.find(v => v.validatorId === id);
				if(validator) {
					saveObservables.push(this.validatorService.updateValidator(projectId, id, validator));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveWorkflows(
		projectId: string,
		modifiedIds: Set<string>,
		workflows: Workflow[],
		originalWorkflows: Workflow[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalWorkflows.find(wf => wf.workflowId === originalId);
				if(original) {
					saveObservables.push(this.workflowService.deleteWorkflow(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const workflow = workflows.find(wf => wf.workflowId === id);
				if(workflow) {
					saveObservables.push(this.workflowService.createWorkflow(projectId, this.toPayload(workflow)));
				}
			}
			else {
				const workflow = workflows.find(wf => wf.workflowId === id);
				if(workflow) {
					saveObservables.push(this.workflowService.updateWorkflow(projectId, id, this.toPayload(workflow)));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	private toPayload(workflow: Workflow): any {
		const {...payload} = workflow as any;
		return payload;
	}

	saveWorkflowStates(
		projectId: string,
		modifiedIds: Set<string>,
		workflowStates: WorkflowState[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const workflowState = workflowStates.find(wfs => wfs.workflowStateId === id);
			if(workflowState) {
				saveObservables.push(this.workflowStateService.updateWorkflowState(projectId, id, workflowState));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveWorkflowActions(
		projectId: string,
		modifiedIds: Set<string>,
		workflowActions: WorkflowAction[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			const workflowAction = workflowActions.find(wfa => wfa.workflowActionId === id);
			if(workflowAction) {
				saveObservables.push(this.workflowActionService.updateWorkflowAction(projectId, id, workflowAction));
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveProfiles(
		projectId: string,
		modifiedIds: Set<string>,
		profiles: Profile[],
		originalProfiles: Profile[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalProfiles.find(p => p.profileId === originalId);
				if(original) {
					saveObservables.push(this.profileService.deleteProfile(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const profile = profiles.find(p => p.profileId === id);
				if(profile) {
					saveObservables.push(this.profileService.createProfile(projectId, profile));
				}
			}
			else {
				const profile = profiles.find(p => p.profileId === id);
				if(profile) {
					saveObservables.push(this.profileService.updateProfile(projectId, id, profile));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveFeatures(
		projectId: string,
		modifiedIds: Set<string>,
		features: Feature[],
		originalFeatures: Feature[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalFeatures.find(f => f.featureId === originalId);
				if(original) {
					saveObservables.push(this.featureService.deleteFeature(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const feature = features.find(f => f.featureId === id);
				if(feature) {
					saveObservables.push(this.featureService.createFeature(projectId, feature));
				}
			}
			else {
				const feature = features.find(f => f.featureId === id);
				if(feature) {
					saveObservables.push(this.featureService.updateFeature(projectId, id, feature));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	savePrivacyPolicies(
		projectId: string,
		modifiedIds: Set<string>,
		privacyPolicies: PrivacyPolicy[],
		originalPrivacyPolicies: PrivacyPolicy[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalPrivacyPolicies.find(pp => pp.policyId === originalId);
				if(original) {
					saveObservables.push(this.privacyPolicyService.deletePrivacyPolicy(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const privacyPolicy = privacyPolicies.find(pp => pp.policyId === id);
				if(privacyPolicy) {
					saveObservables.push(this.privacyPolicyService.createPrivacyPolicy(projectId, privacyPolicy));
				}
			}
			else {
				const privacyPolicy = privacyPolicies.find(pp => pp.policyId === id);
				if(privacyPolicy) {
					saveObservables.push(this.privacyPolicyService.updatePrivacyPolicy(projectId, id, privacyPolicy));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveResourceCategories(
		projectId: string,
		modifiedIds: Set<string>,
		resourceCategories: ResourceCategory[],
		originalResourceCategories: ResourceCategory[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalResourceCategories.find(rc => rc.categoryId === originalId);
				if(original) {
					saveObservables.push(this.resourceCategoryService.deleteResourceCategory(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const resourceCategory = resourceCategories.find(rc => rc.categoryId === id);
				if(resourceCategory) {
					saveObservables.push(this.resourceCategoryService.createResourceCategory(projectId, resourceCategory));
				}
			}
			else {
				const resourceCategory = resourceCategories.find(rc => rc.categoryId === id);
				if(resourceCategory) {
					saveObservables.push(this.resourceCategoryService.updateResourceCategory(projectId, id, resourceCategory));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveReports(
		projectId: string,
		modifiedIds: Set<string>,
		reports: Report[],
		originalReports: Report[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalReports.find(r => r.reportId === originalId);
				if(original) {
					saveObservables.push(this.reportService.deleteReport(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const report = reports.find(r => r.reportId === id);
				if(report) {
					saveObservables.push(this.reportService.createReport(projectId, report));
				}
			}
			else {
				const report = reports.find(r => r.reportId === id);
				if(report) {
					saveObservables.push(this.reportService.updateReport(projectId, id, report));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}

	saveCharts(
		projectId: string,
		modifiedIds: Set<string>,
		charts: ChartModel[],
		originalCharts: ChartModel[]
	): Observable<void> {
		const saveObservables: Observable<any>[] = [];

		modifiedIds.forEach(id => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = originalCharts.find(c => c.chartId === originalId);
				if(original) {
					saveObservables.push(this.chartService.deleteChart(projectId, originalId));
				}
			}
			else if(id.startsWith('temp-')) {
				const chart = charts.find(c => c.chartId === id);
				if(chart) {
					saveObservables.push(this.chartService.createChart(projectId, chart));
				}
			}
			else {
				const chart = charts.find(c => c.chartId === id);
				if(chart) {
					saveObservables.push(this.chartService.updateChart(projectId, id, chart));
				}
			}
		});

		return saveObservables.length > 0
			? forkJoin(saveObservables).pipe(map(() => undefined))
			: of(undefined);
	}
}
