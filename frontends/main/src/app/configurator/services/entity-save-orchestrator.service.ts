import {Injectable} from '@angular/core';
import {DraftSaveService} from './draft-save.service';
import {forkJoin, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ScopeModelContext} from './contexts/scope-model-context';
import {DatasetModelContext} from './contexts/dataset-model-context';

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
			})
		);
	}

	resetScopeModelsToOriginals(context: ScopeModelContext): void {
		context.scopeModelManager.resetToOriginals();
		context.eventModelManager.resetToOriginals();
		context.eventGroupManager.resetToOriginals();
	}

	resetDatasetModelsToOriginals(context: DatasetModelContext): void {
		context.datasetModelManager.resetToOriginals();
		context.fieldModelManager.resetToOriginals();
	}
}
