import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {ScopeModel} from '@core/model/scope-model';
import {ScopeModelService} from '../api/scope-model.service';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class ScopeModelManagerService {
	private tracker: EntityModificationTracker<ScopeModel>;
	private loaded = false;

	constructor(private scopeModelService: ScopeModelService) {
		this.tracker = new EntityModificationTracker<ScopeModel>(
			sm => sm.scopeModelId,
			['id', 'virtual', 'defaultParentId', 'defaultProfileId', 'scopeFormat', 'expectedNumber', 'maxNumber'],
			['shortname', 'longname', 'description', 'pluralShortname'],
			['parentIds', 'datasetModelIds', 'formModelIds', 'workflowIds', 'workflowStateIds']
		);
	}

	load(projectId: string): Observable<ScopeModel[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.scopeModelService.getScopeModels(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, scopeModel: ScopeModel): Observable<ScopeModel> {
		return this.scopeModelService.createScopeModel(projectId, scopeModel).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(scopeModel: ScopeModel): void {
		this.tracker.updateEntity(scopeModel);
	}

	delete(projectId: string, scopeModelId: string): Observable<void> {
		return this.scopeModelService.deleteScopeModel(projectId, scopeModelId).pipe(
			map(() => {
				this.tracker.removeEntity(scopeModelId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): ScopeModel[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	setAll(scopeModels: ScopeModel[]): void {
		this.tracker.initialize(scopeModels);
	}

	getAll(): ScopeModel[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): ScopeModel | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	isFieldModified(id: string, field: string): boolean {
		return this.tracker.isFieldModified(id, field);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, scopeModelId: string, scopeModel: ScopeModel): Observable<ScopeModel> {
		return this.scopeModelService.updateScopeModel(projectId, scopeModelId, scopeModel);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
