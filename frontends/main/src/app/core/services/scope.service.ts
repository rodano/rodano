import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {APIService} from './api.service';
import {Scope} from '../model/scope';
import {ScopeSearch} from '../utilities/search/scope-search';
import {Observable} from 'rxjs';
import {HttpParamsService} from './http-params.service';
import {PagedResultScope} from '../model/paged-result-scope';
import {TimelineGraphData} from '../model/timeline-graph-data';
import {EventModel} from '../model/event-model';
import {ScopeCandidate} from '../model/scope-candidate';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {FieldModelCriterion} from '@core/model/field-model-criterion';

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
	search(search: ScopeSearch): Observable<PagedResultScope> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultScope>(this.serviceUrl, {params});
	}

	getExportUrl(search: ScopeSearch): string {
		const params = this.httpParamsService.toHttpParams(search, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	@reviveDates
	get(scopePk: number): Observable<Scope> {
		return this.http.get<Scope>(`${this.serviceUrl}/${scopePk}`);
	}

	getCandidate(parentScopePk: number, scopeModelId: string): Observable<ScopeCandidate> {
		const params = new HttpParams()
			.set('parentScopePk', parentScopePk.toString())
			.set('scopeModelId', scopeModelId);
		return this.http.get<ScopeCandidate>(`${this.serviceUrl}/candidate`, {params});
	}

	@reviveDates
	create(scopeCandidate: ScopeCandidate): Observable<Scope> {
		return this.http.post<Scope>(this.serviceUrl, scopeCandidate);
	}

	@reviveDates
	save(scopePk: number, scope: Scope): Observable<Scope> {
		return this.http.put<Scope>(`${this.serviceUrl}/${scopePk}`, scope);
	}

	@reviveDates
	remove(scopePk: number, message: string): Observable<Scope> {
		return this.http.put<Scope>(`${this.serviceUrl}/${scopePk}/remove`, {message});
	}

	@reviveDates
	restore(scopePk: number, message: string): Observable<Scope> {
		return this.http.put<Scope>(`${this.serviceUrl}/${scopePk}/restore`, {message});
	}

	@reviveDates
	lock(scopePk: number): Observable<Scope> {
		return this.http.put<Scope>(`${this.serviceUrl}/${scopePk}/lock`, {});
	}

	@reviveDates
	unlock(scopePk: number): Observable<Scope> {
		return this.http.put<Scope>(`${this.serviceUrl}/${scopePk}/unlock`, {});
	}

	enroll(scopePk: number): Observable<void> {
		return this.http.post<void>(`${this.serviceUrl}/${scopePk}/enrollment/enroll`, {});
	}

	unenroll(scopePk: number): Observable<void> {
		return this.http.post<void>(`${this.serviceUrl}/${scopePk}/enrollment/unenroll`, {});
	}

	countEnrollable(scopePk: number, criteria: FieldModelCriterion[]): Observable<number> {
		return this.http.post<number>(`${this.serviceUrl}/${scopePk}/enrollment/count`, criteria);
	}

	getGraphs(scopePk: number): Observable<TimelineGraphData[]> {
		return this.http.get<TimelineGraphData[]>(`${this.serviceUrl}/${scopePk}/timeline`);
	}

	getAvailableEventModels(scopePk: number): Observable<EventModel[]> {
		return this.http.get<EventModel[]>(`${this.serviceUrl}/${scopePk}/available-event-models`);
	}
}
