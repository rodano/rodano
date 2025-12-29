import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APIService } from './api.service';
import { Scope } from '../model/scope-dto';
import { Observable } from 'rxjs';
import { WorkflowStatus } from '../model/workflow-status-dto';
import { HttpParamsService } from './http-helper.service';
import { map } from 'rxjs/operators';
import { EventModel } from '../model/event-model-dto';
import { PagedResultScope } from '../model/paged-result-scope-dto';
import { TimelineGraphData } from '../model/timeline-graph-data-dto';
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

	search(predicate: ScopeSearch): Observable<PagedResultScope> {
		const queryParams = this.httpParamsService.toHttpParams(predicate);
		return this.http.get<PagedResultScope>(
			`${this.apiService.getApiUrl()}/scopes`,
			{ params: queryParams }
		);
	}

	get(scopePk: number): Observable<Scope> {
		return this.http.get<Scope>(`${this.apiService.getApiUrl()}/scopes/${scopePk}`);
	}

	getCandidateScope(parentScopePk: number, scopeModelId: string): Observable<Scope> {
		const queryParams = new HttpParams()
			.set('parentScopePk', parentScopePk.toString())
			.set('scopeModelId', scopeModelId);
		return this.http.get<Scope>(`${this.apiService.getApiUrl()}/scopes/candidate`, { params: queryParams });
	}

	create(scope: Scope): Observable<Scope> {
		return this.http.post<Scope>(`${this.apiService.getApiUrl()}/scopes`, scope);
	}

	save(scopePk: number, scope: Scope): Observable<Scope> {
		return this.http.put<Scope>(`${this.apiService.getApiUrl()}/scopes/${scopePk}`, scope);
	}

	getGraphs(scopePk: number): Observable<TimelineGraphData[]> {
		return this.http.get<TimelineGraphData[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/time-graph`);
	}

	getContainedWorkflowStatuses(scopePk: number): Observable<WorkflowStatus[]> {
		return this.http.get<WorkflowStatus[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/containedworkflows`);
	}

	nbOfImportantWorkflowStatuses(scopePk: number): Observable<number> {
		return this.getContainedWorkflowStatuses(scopePk).pipe(
			map(ws => ws.length)
		);
	}

	getAvailableEventModels(scopePk: number): Observable<EventModel[]> {
		return this.http.get<EventModel[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/available-event-models`);
	}
}
