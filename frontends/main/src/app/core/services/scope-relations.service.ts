import {HttpClient, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {ScopeRelationCreation} from '../model/scope-relation-creation';
import {ScopeRelation} from '../model/scope-relation';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {APIService} from './api.service';

@Service()
export class ScopeRelationsService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	constructor() {
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
}
