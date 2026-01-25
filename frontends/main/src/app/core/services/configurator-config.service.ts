import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ScopeModel} from '@core/model/scope-model';
import {Observable} from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class ConfiguratorConfigService {
	constructor(private http: HttpClient) {}

	getScopeModels(projectId: string): Observable<ScopeModel[]> {
		return this.http.get<ScopeModel[]>(`/api/superuser/configurator/projects/${projectId}/config/scope-models`);
	}

	getScopeModel(projectId: string, scopeModelId: string): Observable<ScopeModel> {
		return this.http.get<ScopeModel>(`/api/superuser/configurator/projects/${projectId}/config/scope-models/${scopeModelId}`);
	}

	createScopeModel(projectId: string, scopeModel: ScopeModel): Observable<ScopeModel> {
		return this.http.post<ScopeModel>(`/api/superuser/configurator/projects/${projectId}/config/scope-models`, scopeModel);
	}

	updateScopeModel(projectId: string, scopeModelId: string, scopeModel: ScopeModel): Observable<ScopeModel> {
		return this.http.put<ScopeModel>(`/api/superuser/configurator/projects/${projectId}/config/scope-models/${scopeModelId}`, scopeModel);
	}

	deleteScopeModel(projectId: string, scopeModelId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/scope-models/${scopeModelId}`);
	}
}
