import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {APIService} from './api.service';
import {ScopeDTO} from '../model/scope-dto';
import {ScopeSearch} from '../utilities/search/scope-search';
import {Observable} from 'rxjs';
import {HttpParamsService} from './http-params.service';
import {PagedResultScopeDTO} from '../model/paged-result-scope-dto';
import {TimelineGraphDataDTO} from '../model/timeline-graph-data-dto';
import {EventModelDTO} from '../model/event-model-dto';
import {ScopeCandidateDTO} from '../model/scope-candidate-dto';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class ScopeService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpParamsService: HttpParamsService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/scopes`;
	}

	@reviveDates
	search(search: ScopeSearch): Observable<PagedResultScopeDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultScopeDTO>(this.serviceUrl, {params});
	}

	getExportUrl(search: ScopeSearch): string {
		const params = this.httpParamsService.toHttpParams(search, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	@reviveDates
	get(scopePk: number): Observable<ScopeDTO> {
		return this.http.get<ScopeDTO>(`${this.serviceUrl}/${scopePk}`);
	}

	getCandidate(parentScopePk: number, scopeModelId: string): Observable<ScopeCandidateDTO> {
		const params = new HttpParams()
			.set('parentScopePk', parentScopePk.toString())
			.set('scopeModelId', scopeModelId);
		return this.http.get<ScopeCandidateDTO>(`${this.serviceUrl}/candidate`, {params});
	}

	@reviveDates
	create(scopeCandidate: ScopeCandidateDTO): Observable<ScopeDTO> {
		return this.http.post<ScopeDTO>(this.serviceUrl, scopeCandidate);
	}

	@reviveDates
	save(scopePk: number, scope: ScopeDTO): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.serviceUrl}/${scopePk}`, scope);
	}

	@reviveDates
	remove(scopePk: number, message: string): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.serviceUrl}/${scopePk}/remove`, {message});
	}

	@reviveDates
	restore(scopePk: number, message: string): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.serviceUrl}/${scopePk}/restore`, {message});
	}

	@reviveDates
	lock(scopePk: number): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.serviceUrl}/${scopePk}/lock`, {});
	}

	@reviveDates
	unlock(scopePk: number): Observable<ScopeDTO> {
		return this.http.put<ScopeDTO>(`${this.serviceUrl}/${scopePk}/unlock`, {});
	}

	getGraphs(scopePk: number): Observable<TimelineGraphDataDTO[]> {
		return this.http.get<TimelineGraphDataDTO[]>(`${this.serviceUrl}/${scopePk}/timeline`);
	}

	getAvailableEventModels(scopePk: number): Observable<EventModelDTO[]> {
		return this.http.get<EventModelDTO[]>(`${this.serviceUrl}/${scopePk}/available-event-models`);
	}
}
