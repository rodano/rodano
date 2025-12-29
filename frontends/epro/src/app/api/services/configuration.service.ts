import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { concatMap, map, first } from 'rxjs/operators';
import { APIService } from './api.service';
import { PublicStudy } from '../model/public-study-dto';
import { Cell } from '../model/cell-dto';
import { Scope } from '../model/scope-dto';
import { DatasetModel } from '../model/dataset-model-dto';
import { EventModel } from '../model/event-model-dto';
import { FormModel } from '../model/form-model-dto';
import { ScopeModel } from '../model/scope-model-dto';
import { Study } from '../model/study-dto';
import { Layout } from '../model/layout-dto';
import { Profile } from '../model/profile-dto';
import { Workflow } from '../model/workflow-dto';
import { Menu } from '../model/menu-dto';
import { CMSLayout } from '../model/cms-layout-dto';
import { AuthStateService } from '../../services/auth-state.service';

@Injectable()
export class ConfigurationService {
	private studySubject = new BehaviorSubject<PublicStudy | undefined>(undefined);
	public study$ = this.studySubject.asObservable();

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private authStateService: AuthStateService
	) {
		this.initializeStudy();
	}

	private initializeStudy(): void {
		const projectId = localStorage.getItem('eproProjectId');

		if (projectId) {
			this.getPublicStudy().subscribe({
				next: study => {
					this.studySubject.next(study);
				},
				error: error => {
					console.error('Failed to load study', error);
					localStorage.removeItem('eproProjectId');
					this.studySubject.next(undefined);
				}
			});
		} else {
			console.log('No project selected for ePRO - waiting for invitation code');
			this.studySubject.next(undefined);
		}
	}

	getPublicStudy(): Observable<PublicStudy> {
		return this.http.get<PublicStudy>(`${this.apiService.getApiUrl()}/config/public-study`);
	}

	setStudy(study: PublicStudy): void {
		this.studySubject.next(study);
	}

	reloadStudy(): void {
		this.initializeStudy();
	}

	getStudy(): Observable<Study> {
		return this.http.get<Study>(`${this.apiService.getApiUrl()}/config/study`);
	}

	getCurrentScope(): Observable<Scope> {
		const robotCreds = this.authStateService.getRobotCredentials();

		if (!robotCreds) {
			throw new Error('No robot credentials found');
		}

		return this.http.get<Scope>(
			`${this.apiService.getApiUrl()}/scopes/${robotCreds.scopePk}`
		);
	}

	getScopeModels(): Observable<ScopeModel[]> {
		return this.getStudy().pipe(map(study => study.scopeModels));
	}

	getLeafScopeModel(): Observable<ScopeModel> {
		return this.getScopeModels().pipe(
			concatMap(scopeModel => scopeModel),
			first(scopeModel => scopeModel.leaf)
		);
	}

	getEvents(): Observable<EventModel[]> {
		return this.getStudy().pipe(map(study => study.leafScopeModel.eventModels));
	}

	getDatasetModels(): Observable<DatasetModel[]> {
		return this.getStudy().pipe(map(study => study.datasetModels));
	}

	getFormModels(): Observable<FormModel[]> {
		return this.getStudy().pipe(map(study => study.formModels));
	}

	getWorkflows(): Observable<Workflow[]> {
		return this.getStudy().pipe(map(study => study.workflows));
	}

	/**
		* @deprecated This endpoint will be removed on next release
		*/
	getRootScope(): Observable<Scope> {
		return this.http.get<Scope>(`${this.apiService.getApiUrl()}/config/root-scope`);
	}

	getRootScopes(): Observable<Scope[]> {
		return this.http.get<Scope[]>(`${this.apiService.getApiUrl()}/config/root-scopes`);
	}

	getProfiles(): Observable<Profile[]> {
		return this.getStudy().pipe(map(study => study.profiles));
	}

	getMenus(): Observable<Menu[]> {
		return this.getStudy().pipe(map(study => study.menus));
	}

	getMenuLayout(menuId: string): Observable<CMSLayout> {
		return this.http.get<CMSLayout>(`${this.apiService.getApiUrl()}/config/menu/${menuId}/layout`);
	}

	getLayoutCells(layout: Layout): Cell[] {
		return layout.lines
			.flatMap(l => l.cells);
	}

	constructDatasetFieldModelMap(layouts: Layout[]): Record<string, string[]> {
		const datasetMap: Record<string, string[]> = {};
		layouts.flatMap(l => this.getLayoutCells(l)).forEach(c => {
			if (datasetMap[c.datasetModelId]) {
				datasetMap[c.datasetModelId].push(c.fieldModelId);
			} else {
				datasetMap[c.datasetModelId] = [c.fieldModelId];
			}
		});

		return datasetMap;
	}
}
