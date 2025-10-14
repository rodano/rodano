import {HttpClient, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {identity, Observable} from 'rxjs';
import {concatMap, map, first, shareReplay} from 'rxjs/operators';
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

@Service()
export class ConfigurationService {
	private readonly serviceUrl: string;
	private readonly publicStudy$: Observable<PublicStudy>;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/config`;
		//cache public study
		this.publicStudy$ = this.http.get<Study>(`${this.serviceUrl}/public-study`).pipe(shareReplay());
	}

	getPublicStudy(): Observable<PublicStudy> {
		return this.publicStudy$;
	}

	getStudy(): Observable<Study> {
		return this.http.get<Study>(`${this.serviceUrl}/study`);
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
			first(s => s.id === scopeModelId)
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
			first(d => d.id === datasetModelId)
		);
	}

	getFormModels(): Observable<FormModel[]> {
		return this.getStudy().pipe(map(study => study.formModels));
	}

	getLeafScopeModelFormModels(): Observable<FormModel[]> {
		return this.getStudy().pipe(map(study => study.leafScopeModel.formModelIds.map(id => study.formModels.find(f => f.id === id)).filter(f => f !== undefined) as FormModel[]));
	}

	getWorkflows(): Observable<Workflow[]> {
		return this.getStudy().pipe(map(study => study.workflows));
	}

	getWorkflowsOnScope(scopeModel: ScopeModel): Observable<Workflow[]> {
		return this.http.get<Workflow[]>(`${this.serviceUrl}/workflows/${scopeModel.id}`);
	}

	getProfiles(): Observable<Profile[]> {
		return this.getStudy().pipe(map(study => study.profiles));
	}

	getProfile(profileId: string): Observable<Profile> {
		return this.getProfiles().pipe(
			concatMap(identity),
			first(p => p.id === profileId)
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

	getScopeModelFormModels(scopeModelId: string): Observable<FormModel[]> {
		return this.http.get<FormModel[]>(`${this.serviceUrl}/scope-model/${scopeModelId}/form-models`);
	}

	getScopeModelDatasetModels(scopeModelId: string): Observable<DatasetModel[]> {
		return this.http.get<DatasetModel[]>(`${this.serviceUrl}/scope-model/${scopeModelId}/dataset-models`);
	}

	getScopeModelFieldModels(scopeModelId: string): Observable<FieldModel[]> {
		return this.http.get<FieldModel[]>(`${this.serviceUrl}/scope-model/${scopeModelId}/field-models`);
	}

	getAutocompleteOptions(datasetModelId: string, fieldModelId: string, value: string): Observable<string[]> {
		const params = new HttpParams().set('text', value);
		return this.http.get<string[]>(`${this.serviceUrl}/dataset-models/${datasetModelId}/field-models/${fieldModelId}/autocomplete`, {params});
	}

	private getScopeModelIsRoot(scopeModel: ScopeModel): boolean {
		return scopeModel.parentIds.length === 0;
	}

	private getScopeModelDepth(scopeModels: ScopeModel[], scopeModel: ScopeModel): number {
		if(this.getScopeModelIsRoot(scopeModel)) {
			return 0;
		}
		const parentScopeModel = scopeModels.find(s => s.id === scopeModel.defaultParentId) as ScopeModel;
		return 1 + this.getScopeModelDepth(scopeModels, parentScopeModel);
	}

	getScopeModelsSorted(): Observable<ScopeModel[]> {
		return this.getScopeModels().pipe(
			map(scopeModels => scopeModels.sort((s1, s2) => {
				const depth1 = this.getScopeModelDepth(scopeModels, s1);
				const depth2 = this.getScopeModelDepth(scopeModels, s2);
				if(depth1 === depth2) {
					return s1.id.localeCompare(s2.id);
				}
				return depth1 - depth2;
			}))
		);
	}
}
