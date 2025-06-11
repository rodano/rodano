import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APIService } from './api.service';
import { ScopeDTO } from '../model/scope-dto';
import { Observable } from 'rxjs';
import { WorkflowStatusDTO } from '../model/workflow-status-dto';
import { HttpParamsService } from './http-helper.service';
import { map } from 'rxjs/operators';
import { EventModelDTO } from '../model/event-model-dto';
import { PagedResultScopeDTO } from '../model/paged-result-scope-dto';
import { TimelineGraphDataDTO } from '../model/timeline-graph-data-dto';
import { ScopeSearch } from '../utilities/search/scope-search';

@Injectable({
	providedIn: 'root'
})
export class ScopeService {

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpParamsService: HttpParamsService
	) { }

	search(predicate: ScopeSearch): Observable<PagedResultScopeDTO> {
		const queryParams = this.httpParamsService.toHttpParams(predicate);
		return this.http.get<PagedResultScopeDTO>(
			`${this.apiService.getApiUrl()}/scopes`,
			{ params: queryParams }
		);
	}

	get(scopePk: number): Observable<ScopeDTO> {
		return this.http.get<ScopeDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}`);
	}

	getCandidateScope(parentScopePk: number, scopeModelId: string): Observable<ScopeDTO> {
		const queryParams = new HttpParams()
			.set('parentScopePk', parentScopePk.toString())
			.set('scopeModelId', scopeModelId);
		return this.http.get<ScopeDTO>(`${this.apiService.getApiUrl()}/scopes/candidate`, { params: queryParams });
	}

	create(scope: ScopeDTO): Observable<ScopeDTO> {
		return this.http.post<ScopeDTO>(`${this.apiService.getApiUrl()}/scopes`, scope);
	}

	save(scopePk: number, scope: ScopeDTO): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}`, scope);
	}

	getGraphs(scopePk: number): Observable<TimelineGraphDataDTO[]> {
		return this.http.get<TimelineGraphDataDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/time-graph`);
	}

	getContainedWorkflowStatuses(scopePk: number): Observable<WorkflowStatusDTO[]> {
		return this.http.get<WorkflowStatusDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/containedworkflows`);
	}

	nbOfImportantWorkflowStatuses(scopePk: number): Observable<number> {
		return this.getContainedWorkflowStatuses(scopePk).pipe(
			map(ws => ws.length)
		);
	}

	getAvailableEventModels(scopePk: number): Observable<EventModelDTO[]> {
		return this.http.get<EventModelDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/available-event-models`);
	}
}
