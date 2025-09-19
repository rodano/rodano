import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Scope} from '../model/scope';
import {ScopeRelationCreation} from '../model/scope-relation-creation';
import {ScopeRelation} from '../model/scope-relation';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {APIService} from './api.service';
import {Rights} from '../model/rights';

@Injectable({
	providedIn: 'root'
})
export class ScopeRelationsService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/scopes`;
	}

	@reviveDates
	getParentRelations(scopePk: number): Observable<ScopeRelation[]> {
		return this.http.get<ScopeRelation[]>(`${this.serviceUrl}/${scopePk}/relations`);
	}

	@reviveDates
	createScopeRelation(scopePk: number, newRelation: ScopeRelationCreation): Observable<ScopeRelation[]> {
		return this.http.post<ScopeRelation[]>(
			`${this.serviceUrl}/${scopePk}/relations`,
			newRelation
		);
	}

	@reviveDates
	makeRelationDefault(scopePk: number, relationPk: number): Observable<ScopeRelation[]> {
		return this.http.put<ScopeRelation[]>(`${this.serviceUrl}/${scopePk}/relations/${relationPk}/default`, {});
	}

	@reviveDates
	endRelation(scopePk: number, relationPk: number, date: Date): Observable<ScopeRelation[]> {
		const params = new HttpParams()
			.set('date', date.toISOString());

		return this.http.put<ScopeRelation[]>(
			`${this.serviceUrl}/${scopePk}/relations/${relationPk}/end`,
			{},
			{params}
		);
	}

	@reviveDates
	transfer(scopePk: number, newRelation: ScopeRelationCreation): Observable<ScopeRelation[]> {
		return this.http.post<ScopeRelation[]>(
			`${this.serviceUrl}/${scopePk}/relations/transfer`,
			newRelation
		);
	}

	@reviveDates
	getParents(scopeModelId: string, right: Rights, onlyDefault = true): Observable<Scope[]> {
		const params = new HttpParams()
			.set('scopeModelId', scopeModelId)
			.set('right', right)
			.set('onlyDefault', onlyDefault);
		return this.http.get<Scope[]>(`${this.serviceUrl}/relations/available-parents`, {params});
	}

	@reviveDates
	getDefaultParentScope(scopePk: number): Observable<Scope> {
		return this.http.get<Scope>(`${this.serviceUrl}/${scopePk}/parents/default`);
	}

	@reviveDates
	getAncestors(scopePk: number, onlyDefault = true): Observable<Scope[]> {
		const params = new HttpParams()
			.set('onlyDefault', onlyDefault);
		return this.http.get<Scope[]>(`${this.serviceUrl}/${scopePk}/ancestors`, {params});
	}
}
