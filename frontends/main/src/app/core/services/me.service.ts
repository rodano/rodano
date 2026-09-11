import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {catchError, Observable, of} from 'rxjs';
import {User} from '../model/user';
import {APIService} from './api.service';
import {SKIP_ERROR_HANDLING_HEADER} from '../../interceptors/auth.interceptor';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {Scope} from '../model/scope';
import {ScopeMini} from '../model/scope-mini';
import {Rights} from '@core/model/rights';
import {RightEntity} from '@core/enums/right-entity';

@Service()
export class MeService {
	private readonly serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/me`;
	}

	get(): Observable<User> {
		return this.http.get<User>(this.serviceUrl);
	}

	/**
	 * This method tries to get the current user without throwing an error if the user is not logged in
	 * this is helpful when using this API to check is a user if effectively logged in (meaning he has a token and it is valid)
	 * this method will return undefined if the user is not logged in
	 * @returns {Observable<User>} An observable of the user
	 */
	tryToGet(): Observable<User | undefined> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.get<User>(this.serviceUrl, {headers}).pipe(
			catchError(() => of(undefined))
		);
	}

	@reviveDates
	getRootScope(): Observable<Scope> {
		return this.http.get<Scope>(`${this.serviceUrl}/root-scope`);
	}

	@reviveDates
	getRootScopes(): Observable<Scope[]> {
		return this.http.get<Scope[]>(`${this.serviceUrl}/root-scopes`);
	}

	@reviveDates
	getScopes(
		scopeModelIds?: string[]
	): Observable<ScopeMini[]> {
		let params = new HttpParams();
		if(scopeModelIds) {
			params = params.set('scopeModelIds', scopeModelIds.join(','));
		}
		return this.http.get<ScopeMini[]>(`${this.serviceUrl}/scopes`, {params});
	}

	@reviveDates
	getScopesForFeature(
		feature: string,
		scopeModelIds?: string[]
	): Observable<ScopeMini[]> {
		let params = new HttpParams();
		params = params.set('feature', feature);
		if(scopeModelIds) {
			params = params.set('scopeModelIds', scopeModelIds.join(','));
		}
		return this.http.get<ScopeMini[]>(`${this.serviceUrl}/scopes`, {params});
	}

	@reviveDates
	getScopesForRequiredRight(
		rightEntity: RightEntity,
		rightId: string,
		right: Rights,
		scopeModelIds?: string[]
	): Observable<ScopeMini[]> {
		let params = new HttpParams();
		params = params.set('rightEntity', rightEntity);
		params = params.set('rightId', rightId);
		params = params.set('right', right);
		if(scopeModelIds) {
			params = params.set('scopeModelIds', scopeModelIds.join(','));
		}
		return this.http.get<ScopeMini[]>(`${this.serviceUrl}/scopes`, {params});
	}

	impersonate(profileId: string): Observable<User> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.put<User>(`${this.serviceUrl}/impersonate`, {profileId}, {headers});
	}
}
