import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { concatMap, map, first } from 'rxjs/operators';
import { APIService } from './api.service';
import { PublicStudyDTO } from '../model/public-study-dto';
import { CellDTO } from '../model/cell-dto';
import { ScopeDTO } from '../model/scope-dto';
import { DatasetModelDTO } from '../model/dataset-model-dto';
import { EventModelDTO } from '../model/event-model-dto';
import { FormModelDTO } from '../model/form-model-dto';
import { ScopeModelDTO } from '../model/scope-model-dto';
import { StudyDTO } from '../model/study-dto';
import { LayoutDTO } from '../model/layout-dto';
import { ProfileDTO } from '../model/profile-dto';
import { WorkflowDTO } from '../model/workflow-dto';
import { MenuDTO } from '../model/menu-dto';
import { CMSLayoutDTO } from '../model/cms-layout-dto';

@Injectable()
export class ConfigurationService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	getPublicStudy(): Observable<PublicStudyDTO> {
		return this.http.get<PublicStudyDTO>(`${this.apiService.getApiUrl()}/config/public-study`);
	}

	getStudy(): Observable<StudyDTO> {
		return this.http.get<StudyDTO>(`${this.apiService.getApiUrl()}/config/study`);
	}

	getScopeModels(): Observable<ScopeModelDTO[]> {
		return this.getStudy().pipe(map(study => study.scopeModels));
	}

	getLeafScopeModel(): Observable<ScopeModelDTO> {
		return this.getScopeModels().pipe(
			concatMap(scopeModel => scopeModel),
			first(scopeModel => scopeModel.leaf)
		);
	}

	getEvents(): Observable<EventModelDTO[]> {
		return this.getStudy().pipe(map(study => study.eventModels));
	}

	getDatasetModels(): Observable<DatasetModelDTO[]> {
		return this.getStudy().pipe(map(study => study.datasetModels));
	}

	getFormModels(): Observable<FormModelDTO[]> {
		return this.getStudy().pipe(map(study => study.formModels));
	}

	getWorkflows(): Observable<WorkflowDTO[]> {
		return this.getStudy().pipe(map(study => study.workflows));
	}

	/**
	 * @deprecated This endpoint will be removed on next release
	 */
	getRootScope(): Observable<ScopeDTO> {
		return this.http.get<ScopeDTO>(`${this.apiService.getApiUrl()}/config/root-scope`);
	}

	getRootScopes(): Observable<ScopeDTO[]> {
		return this.http.get<ScopeDTO[]>(`${this.apiService.getApiUrl()}/config/root-scopes`);
	}

	getProfiles(): Observable<ProfileDTO[]> {
		return this.getStudy().pipe(map(study => study.profiles));
	}

	getMenus(): Observable<MenuDTO[]> {
		return this.getStudy().pipe(map(study => study.menus));
	}

	getMenuLayout(menuId: string): Observable<CMSLayoutDTO> {
		return this.http.get<CMSLayoutDTO>(`${this.apiService.getApiUrl()}/config/menu/${menuId}/layout`);
	}

	getLayoutCells(layout: LayoutDTO): CellDTO[] {
		return layout.lines
			.flatMap(l => l.columns)
			.flatMap(c => c.cells);
	}

	constructDatasetFieldModelMap(layouts: LayoutDTO[]): Record<string, string[]> {
		const datasetMap: Record<string, string[]> = {};
		layouts.flatMap(l => this.getLayoutCells(l)).forEach(c => {
			if(datasetMap[c.datasetModelId]) {
				datasetMap[c.datasetModelId].push(c.fieldModelId);
			} else {
				datasetMap[c.datasetModelId] = [c.fieldModelId];
			}
		});

		return datasetMap;
	}
}
