import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {identity, Observable} from 'rxjs';
import {concatMap, map, first, shareReplay} from 'rxjs/operators';
import {MenuDTO} from '../model/menu-dto';
import {ScopeModelDTO} from '../model/scope-model-dto';
import {StudyDTO} from '../model/study-dto';
import {WorkflowDTO} from '../model/workflow-dto';
import {APIService} from './api.service';
import {PublicStudyDTO} from '../model/public-study-dto';
import {ProfileDTO} from '../model/profile-dto';
import {CMSLayoutDTO} from '../model/cms-layout-dto';
import {ResourceCategoryDTO} from '../model/resource-category-dto';
import {DatasetModelDTO} from '../model/dataset-model-dto';
import {FormModelDTO} from '../model/form-model-dto';
import {FieldModelDTO} from '../model/field-model-dto';
import {LanguageDTO} from '../model/language-dto';

@Injectable({
	providedIn: 'root'
})
export class ConfigurationService {
	private readonly serviceUrl: string;
	private readonly publicStudy$: Observable<PublicStudyDTO>;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/config`;
		//cache public study
		this.publicStudy$ = this.http.get<StudyDTO>(`${this.serviceUrl}/public-study`).pipe(shareReplay());
	}

	getPublicStudy(): Observable<PublicStudyDTO> {
		return this.publicStudy$;
	}

	getStudy(): Observable<StudyDTO> {
		return this.http.get<StudyDTO>(`${this.serviceUrl}/study`);
	}

	getLanguages(): Observable<LanguageDTO[]> {
		return this.getStudy().pipe(map(study => study.activatedLanguages));
	}

	getScopeModels(): Observable<ScopeModelDTO[]> {
		return this.getStudy().pipe(map(study => study.scopeModels));
	}

	getScopeModel(scopeModelId: string): Observable<ScopeModelDTO> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.id === scopeModelId)
		);
	}

	getRootScopeModel(): Observable<ScopeModelDTO> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.parentIds.length === 0)
		);
	}

	getLeafScopeModel(): Observable<ScopeModelDTO> {
		return this.getScopeModels().pipe(
			concatMap(identity),
			first(s => s.leaf)
		);
	}

	getDatasetModels(): Observable<DatasetModelDTO[]> {
		return this.getStudy().pipe(map(study => study.datasetModels));
	}

	getDatasetModel(datasetModelId: string): Observable<DatasetModelDTO> {
		return this.getDatasetModels().pipe(
			concatMap(identity),
			first(d => d.id === datasetModelId)
		);
	}

	getFormModels(): Observable<FormModelDTO[]> {
		return this.getStudy().pipe(map(study => study.formModels));
	}

	getLeafScopeModelFormModels(): Observable<FormModelDTO[]> {
		return this.getStudy().pipe(map(study => study.leafScopeModel.formModelIds.map(id => study.formModels.find(f => f.id === id)).filter(f => f !== undefined) as FormModelDTO[]));
	}

	getWorkflows(): Observable<WorkflowDTO[]> {
		return this.getStudy().pipe(map(study => study.workflows));
	}

	getProfiles(): Observable<ProfileDTO[]> {
		return this.getStudy().pipe(map(study => study.profiles));
	}

	getProfile(profileId: string): Observable<ProfileDTO> {
		return this.getProfiles().pipe(
			concatMap(identity),
			first(p => p.id === profileId)
		);
	}

	getMenus(): Observable<MenuDTO[]> {
		return this.getStudy().pipe(map(study => study.menus));
	}

	getMenuLayout(menuId: string): Observable<CMSLayoutDTO> {
		return this.http.get<CMSLayoutDTO>(`${this.serviceUrl}/menu/${menuId}/layout`);
	}

	getResourceCategories(): Observable<ResourceCategoryDTO[]> {
		return this.http.get<ResourceCategoryDTO[]>(`${this.serviceUrl}/resource-categories`);
	}

	getSearchableFieldModels(): Observable<FieldModelDTO[]> {
		return this.http.get<FieldModelDTO[]>(`${this.serviceUrl}/searchable-field-models`);
	}

	getAutocompleteOptions(datasetModelId: string, fieldModelId: string, value: string): Observable<string[]> {
		return this.http.get<string[]>(`${this.serviceUrl}/dataset-models/${datasetModelId}/field-models/${fieldModelId}/autocomplete/${value}`);
	}

	private getScopeModelIsRoot(scopeModel: ScopeModelDTO): boolean {
		return scopeModel.parentIds.length === 0;
	}

	private getScopeModelDepth(scopeModels: ScopeModelDTO[], scopeModel: ScopeModelDTO): number {
		if(this.getScopeModelIsRoot(scopeModel)) {
			return 0;
		}
		const parentScopeModel = scopeModels.find(s => s.id === scopeModel.defaultParentId) as ScopeModelDTO;
		return 1 + this.getScopeModelDepth(scopeModels, parentScopeModel);
	}

	getScopeModelsSorted(): Observable<ScopeModelDTO[]> {
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
