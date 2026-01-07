import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, identity, Observable} from 'rxjs';
import {concatMap, first, map} from 'rxjs/operators';
import {Menu} from '../model/menu';
import {ScopeModel} from '../model/scope-model';
import {Study} from '../model/study';
import {Workflow} from '../model/workflow';
import {APIService} from './api.service';
import {PublicStudy} from '../model/public-study';
import {Profile} from '../model/profile';
import {CMSLayout} from '../model/cms-layout';
import {ResourceCategory} from '../model/resource-category';
import {DatasetModel} from '../model/dataset-model';
import {FormModel} from '../model/form-model';
import {FieldModel} from '../model/field-model';
import {Language} from '../model/language';

@Injectable({
	providedIn: 'root'
})
export class ConfigurationService {
	private readonly serviceUrl: string;
	private readonly publicStudy$: Observable<PublicStudy>;
	private studySubject = new BehaviorSubject<PublicStudy | undefined>(undefined);
	public study$ = this.studySubject.asObservable();

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/config`;

		this.initializeStudy();
	}

	private initializeStudy(): void {
		const projectId = localStorage.getItem('currentProjectId');

		if(projectId) {
			this.getPublicStudy().subscribe({
				next: study => {
					this.studySubject.next(study);
				},
				error: error => {
					console.error('Failed to load study on init', error);
					localStorage.removeItem('currentProjectId');
				}
			});
		}
		else {
			console.log('No project selected, skipping study load');
		}
	}

	getPublicStudy(): Observable<PublicStudy> {
		return this.http.get<PublicStudy>('/api/config/public-study');
	}

	getStudy(): Observable<Study> {
		return this.http.get<Study>(`${this.serviceUrl}/study`);
	}

	setStudy(study: PublicStudy): void {
		this.studySubject.next(study);
	}

	clearStudy(): void {
		this.studySubject.next(undefined);
	}

	getLanguages(): Observable<Language[]> {
		return this.getStudy().pipe(map(study => study.activatedLanguages));
	}

	getScopeModels(): Observable<ScopeModel[]> {
		return this.getStudy().pipe(map(study => study.scopeModels));
	}

	getScopeModel(scopeModelId: string): Observable<ScopeModel> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.scopeModelId === scopeModelId)
		);
	}

	getRootScopeModel(): Observable<ScopeModel> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.parentIds.length === 0)
		);
	}

	getLeafScopeModel(): Observable<ScopeModel> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.leaf)
		);
	}

	getDatasetModels(): Observable<DatasetModel[]> {
		return this.getStudy().pipe(map(study => study.datasetModels));
	}

	getDatasetModel(datasetModelId: string): Observable<DatasetModel> {
		return this.getDatasetModels().pipe(
			concatMap(identity),
			first(d => d.datasetModelId === datasetModelId)
		);
	}

	getFormModels(): Observable<FormModel[]> {
		return this.getStudy().pipe(map(study => study.formModels));
	}

	getLeafScopeModelFormModels(): Observable<FormModel[]> {
		return this.getStudy().pipe(map(study => study.leafScopeModel.formModelIds.map(uuid => study.formModels.find(f => f.formModelId === uuid)).filter(f => f !== undefined) as FormModel[]));
	}

	getWorkflows(): Observable<Workflow[]> {
		return this.getStudy().pipe(map(study => study.workflows));
	}

	getProfiles(): Observable<Profile[]> {
		return this.getStudy().pipe(map(study => study.profiles));
	}

	getProfile(profileId: string): Observable<Profile> {
		return this.getProfiles().pipe(
			concatMap(identity),
			first(p => p.profileId === profileId)
		);
	}

	getMenus(): Observable<Menu[]> {
		return this.getStudy().pipe(map(study => study.menus));
	}

	getMenuLayout(menuId: string): Observable<CMSLayout> {
		return this.http.get<CMSLayout>(`${this.serviceUrl}/menu/${menuId}/layout`);
	}

	getResourceCategories(): Observable<ResourceCategory[]> {
		return this.http.get<ResourceCategory[]>(`${this.serviceUrl}/resource-categories`);
	}

	getSearchableFieldModels(): Observable<FieldModel[]> {
		return this.http.get<FieldModel[]>(`${this.serviceUrl}/searchable-field-models`);
	}

	getAutocompleteOptions(datasetModelId: string, fieldModelId: string, value: string): Observable<string[]> {
		return this.http.get<string[]>(`${this.serviceUrl}/dataset-models/${datasetModelId}/field-models/${fieldModelId}/autocomplete/${value}`);
	}

	private getScopeModelIsRoot(scopeModel: ScopeModel): boolean {
		return scopeModel.parentIds.length === 0;
	}

	private getScopeModelDepth(scopeModels: ScopeModel[], scopeModel: ScopeModel): number {
		if(this.getScopeModelIsRoot(scopeModel)) {
			return 0;
		}
		const parentScopeModel = scopeModels.find(s => s.scopeModelId === scopeModel.defaultParentId) as ScopeModel;
		if(!parentScopeModel) {
			return 1;
		}
		return 1 + this.getScopeModelDepth(scopeModels, parentScopeModel);
	}

	getScopeModelsSorted(): Observable<ScopeModel[]> {
		return this.getScopeModels().pipe(
			map(scopeModels => scopeModels.sort((s1, s2) => {
				const depth1 = this.getScopeModelDepth(scopeModels, s1);
				const depth2 = this.getScopeModelDepth(scopeModels, s2);
				if(depth1 === depth2) {
					return s1.scopeModelId.localeCompare(s2.scopeModelId);
				}
				return depth1 - depth2;
			}))
		);
	}
}
