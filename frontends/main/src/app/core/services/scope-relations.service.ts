import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ScopeDTO} from '../model/scope-dto';
import {ScopeRelationCreationDTO} from '../model/scope-relation-creation-dto';
import {ScopeRelationDTO} from '../model/scope-relation-dto';
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
	getParentRelations(scopePk: number): Observable<ScopeRelationDTO[]> {
		return this.http.get<ScopeRelationDTO[]>(`${this.serviceUrl}/${scopePk}/relations`);
	}

	@reviveDates
	createScopeRelation(scopePk: number, newRelation: ScopeRelationCreationDTO): Observable<ScopeRelationDTO[]> {
		return this.http.post<ScopeRelationDTO[]>(
			`${this.serviceUrl}/${scopePk}/relations`,
			newRelation
		);
	}

	@reviveDates
	makeRelationDefault(scopePk: number, relationPk: number): Observable<ScopeRelationDTO[]> {
		return this.http.put<ScopeRelationDTO[]>(`${this.serviceUrl}/${scopePk}/relations/${relationPk}/default`, {});
	}

	@reviveDates
	endRelation(scopePk: number, relationPk: number, date: Date): Observable<ScopeRelationDTO[]> {
		const params = new HttpParams()
			.set('date', date.toISOString());

		return this.http.put<ScopeRelationDTO[]>(
			`${this.serviceUrl}/${scopePk}/relations/${relationPk}/end`,
			{},
			{params}
		);
	}

	@reviveDates
	transfer(scopePk: number, newRelation: ScopeRelationCreationDTO): Observable<ScopeRelationDTO[]> {
		return this.http.post<ScopeRelationDTO[]>(
			`${this.serviceUrl}/${scopePk}/relations/transfer`,
			newRelation
		);
	}

	@reviveDates
	getParents(scopeModelId: string, right: Rights, onlyDefault = true): Observable<ScopeDTO[]> {
		const params = new HttpParams()
			.set('scopeModelId', scopeModelId)
			.set('right', right)
			.set('onlyDefault', onlyDefault);
		return this.http.get<ScopeDTO[]>(`${this.serviceUrl}/relations/available-parents`, {params});
	}

	@reviveDates
	getDefaultParentScope(scopePk: number): Observable<ScopeDTO> {
		return this.http.get<ScopeDTO>(`${this.serviceUrl}/${scopePk}/parents/default`);
	}

	@reviveDates
	getAncestors(scopePk: number, onlyDefault = true): Observable<ScopeDTO[]> {
		const params = new HttpParams()
			.set('onlyDefault', onlyDefault);
		return this.http.get<ScopeDTO[]>(`${this.serviceUrl}/${scopePk}/ancestors`, {params});
	}
}
