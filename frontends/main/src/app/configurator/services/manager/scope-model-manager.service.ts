import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ScopeModel} from '@core/model/scope-model';
import {ScopeModelService} from '../api/scope-model.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class ScopeModelManagerService extends BaseManagerService<ScopeModel> {
	constructor(private scopeModelService: ScopeModelService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (sm: ScopeModel) => sm.scopeModelId;}
	protected getSimpleFields(): (keyof ScopeModel)[] {
		return ['id', 'virtual', 'defaultParentId', 'defaultProfileId', 'scopeFormat', 'expectedNumber', 'maxNumber'];
	}

	protected getTranslationFields(): (keyof ScopeModel)[] {
		return ['shortname', 'longname', 'description', 'pluralShortname'];
	}

	protected getArrayFields(): (keyof ScopeModel)[] {
		return ['parentIds', 'datasetModelIds', 'formModelIds', 'workflowIds', 'workflowStateIds'];
	}

	protected fetchAll(projectId: string): Observable<ScopeModel[]> {
		return this.scopeModelService.getScopeModels(projectId);
	}

	protected createEntity(projectId: string, entity: ScopeModel): Observable<ScopeModel> {
		return this.scopeModelService.createScopeModel(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.scopeModelService.deleteScopeModel(projectId, id);
	}
}
