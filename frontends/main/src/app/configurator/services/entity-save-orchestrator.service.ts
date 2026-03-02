import {Injectable} from '@angular/core';
import {DraftSaveService} from './draft-save.service';
import {forkJoin, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ScopeModelContext} from './contexts/scope-model-context';
import {DatasetModelContext} from './contexts/dataset-model-context';
import {ValidatorContext} from './contexts/validator-context';
import {WorkflowContext} from './contexts/workflow-context';
import {ProfileContext} from './contexts/profile-context';
import {FeatureContext} from './contexts/feature-context';
import {PrivacyPolicyContext} from './contexts/privacy-policy-context';
import {ResourceCategoryContext} from './contexts/resource-category-context';
import {ReportContext} from './contexts/report-context';
import {ChartContext} from './contexts/chart-context';

@Injectable({providedIn: 'root'})
export class EntitySaveOrchestratorService {
	constructor(private draftSaveService: DraftSaveService) {}

	saveScopeModels(projectId: string, context: ScopeModelContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveScopeModels(
				projectId,
				context.modifiedScopeModelIds,
				context.scopeModels,
				context.originalScopeModels
			),
			this.draftSaveService.saveEventModels(
				projectId,
				context.modifiedEventModels,
				context.eventModels
			),
			this.draftSaveService.saveEventGroups(
				projectId,
				context.modifiedEventGroups,
				context.eventGroups
			)
		]).pipe(
			map(() => {
				context.scopeModelManager.syncOriginalsWithCurrent();
				context.eventModelManager.syncOriginalsWithCurrent();
				context.eventGroupManager.syncOriginalsWithCurrent();

				context.scopeModelManager.invalidate();
				context.eventModelManager.invalidate();
				context.eventGroupManager.invalidate();
			})
		);
	}

	saveDatasetModels(projectId: string, context: DatasetModelContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveDatasetModels(
				projectId,
				context.modifiedDatasetModelIds,
				context.datasetModels,
				context.originalDatasetModels
			),
			this.draftSaveService.saveFieldModels(
				projectId,
				context.modifiedFieldModels,
				context.fieldModels
			)
		]).pipe(
			map(() => {
				context.datasetModelManager.syncOriginalsWithCurrent();
				context.fieldModelManager.syncOriginalsWithCurrent();

				context.datasetModelManager.invalidate();
				context.fieldModelManager.invalidate();
			})
		);
	}

	saveValidators(projectId: string, context: ValidatorContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveValidators(
				projectId,
				context.modifiedValidatorIds,
				context.validators,
				context.originalValidators
			)
		]).pipe(
			map(() => {
				context.validatorManager.syncOriginalsWithCurrent();

				context.validatorManager.invalidate();
			})
		);
	}

	saveWorkflows(projectId: string, context: WorkflowContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveWorkflows(
				projectId,
				context.modifiedWorkflowIds,
				context.workflows,
				context.originalWorkflows
			),
			this.draftSaveService.saveWorkflowStates(
				projectId,
				context.modifiedWorkflowStateIds,
				context.workflowStates
			),
			this.draftSaveService.saveWorkflowActions(
				projectId,
				context.modifiedWorkflowActionIds,
				context.workflowActions
			)
		]).pipe(
			map(() => {
				context.workflowManager.syncOriginalsWithCurrent();
				context.workflowStateManager.syncOriginalsWithCurrent();
				context.workflowActionManager.syncOriginalsWithCurrent();

				context.workflowManager.invalidate();
				context.workflowStateManager.invalidate();
				context.workflowActionManager.invalidate();
			})
		);
	}

	saveProfiles(projectId: string, context: ProfileContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveProfiles(
				projectId,
				context.modifiedProfileIds,
				context.profiles,
				context.originalProfiles
			)
		]).pipe(
			map(() => {
				context.profileManager.syncOriginalsWithCurrent();

				context.profileManager.invalidate();
			})
		);
	}

	saveFeatures(projectId: string, context: FeatureContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveFeatures(
				projectId,
				context.modifiedFeatureIds,
				context.features,
				context.originalFeatures
			)
		]).pipe(
			map(() => {
				context.featureManager.syncOriginalsWithCurrent();

				context.featureManager.invalidate();
			})
		);
	}

	savePrivacyPolicies(projectId: string, context: PrivacyPolicyContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.savePrivacyPolicies(
				projectId,
				context.modifiedPrivacyPolicyIds,
				context.privacyPolicies,
				context.originalPrivacyPolicies
			)
		]).pipe(
			map(() => {
				context.privacyPolicyManager.syncOriginalsWithCurrent();

				context.privacyPolicyManager.invalidate();
			})
		);
	}

	saveResourceCategories(projectId: string, context: ResourceCategoryContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveResourceCategories(
				projectId,
				context.modifiedResourceCategoryIds,
				context.resourceCategories,
				context.originalResourceCategories
			)
		]).pipe(
			map(() => {
				context.resourceCategoryManager.syncOriginalsWithCurrent();

				context.resourceCategoryManager.invalidate();
			})
		);
	}

	saveReports(projectId: string, context: ReportContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveReports(
				projectId,
				context.modifiedReportIds,
				context.reports,
				context.originalReports
			)
		]).pipe(
			map(() => {
				context.reportManager.syncOriginalsWithCurrent();

				context.reportManager.invalidate();
			})
		);
	}

	saveCharts(projectId: string, context: ChartContext): Observable<void> {
		return forkJoin([
			this.draftSaveService.saveCharts(
				projectId,
				context.modifiedChartIds,
				context.charts,
				context.originalCharts
			)
		]).pipe(
			map(() => {
				context.chartManager.syncOriginalsWithCurrent();

				context.chartManager.invalidate();
			})
		);
	}

	resetScopeModelsToOriginals(context: ScopeModelContext): void {
		context.scopeModelManager.resetToOriginals();
		context.eventModelManager.resetToOriginals();
		context.eventGroupManager.resetToOriginals();

		context.scopeModelManager.invalidate();
		context.eventModelManager.invalidate();
		context.eventGroupManager.invalidate();
	}

	resetDatasetModelsToOriginals(context: DatasetModelContext): void {
		context.datasetModelManager.resetToOriginals();
		context.fieldModelManager.resetToOriginals();

		context.datasetModelManager.invalidate();
		context.fieldModelManager.invalidate();
	}

	resetValidatorsToOriginals(context: ValidatorContext): void {
		context.validatorManager.resetToOriginals();

		context.validatorManager.invalidate();
	}

	resetWorkflowsToOriginals(context: WorkflowContext): void {
		context.workflowManager.resetToOriginals();
		context.workflowStateManager.resetToOriginals();
		context.workflowActionManager.resetToOriginals();

		context.workflowManager.invalidate();
		context.workflowStateManager.invalidate();
		context.workflowActionManager.invalidate();
	}

	resetProfilesToOriginals(context: ProfileContext): void {
		context.profileManager.resetToOriginals();

		context.profileManager.invalidate();
	}

	resetFeaturesToOriginals(context: FeatureContext): void {
		context.featureManager.resetToOriginals();

		context.featureManager.invalidate();
	}

	resetPrivacyPoliciesToOriginals(context: PrivacyPolicyContext): void {
		context.privacyPolicyManager.resetToOriginals();

		context.privacyPolicyManager.invalidate();
	}

	resetResourceCategoriesToOriginals(context: ResourceCategoryContext): void {
		context.resourceCategoryManager.resetToOriginals();

		context.resourceCategoryManager.invalidate();
	}

	resetReportsToOriginals(context: ReportContext): void {
		context.reportManager.resetToOriginals();

		context.reportManager.invalidate();
	}

	resetChartsToOriginals(context: ChartContext): void {
		context.chartManager.resetToOriginals();

		context.chartManager.invalidate();
	}
}
