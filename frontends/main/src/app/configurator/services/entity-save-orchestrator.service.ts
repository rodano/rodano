import {Injectable} from '@angular/core';
import {DraftSaveService} from './draft-save.service';
import {forkJoin, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ScopeModelContext} from './contexts/scope-model-context';
import {DatasetModelContext} from './contexts/dataset-model-context';
import {ValidatorContext} from './contexts/validator-context';

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
				context.modifiedEventModelIds,
				context.eventModels
			),
			this.draftSaveService.saveEventGroups(
				projectId,
				context.modifiedEventGroupIds,
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
}
