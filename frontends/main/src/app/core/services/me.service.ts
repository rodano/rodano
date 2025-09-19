import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {catchError, Observable, of} from 'rxjs';
import {User} from '../model/user';
import {APIService} from './api.service';
import {SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {Scope} from '../model/scope';
import {ScopeMini} from '../model/scope-mini';

@Injectable({
	providedIn: 'root'
})
export class MeService {
	private readonly serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
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
	getScopes(feature: string | undefined, excludeLeaf = true, excludeVirtual = false): Observable<ScopeMini[]> {
		let params = new HttpParams();
		if(feature) {
			params = params.set('feature', feature);
		}
		params = params.set('excludeLeaf', excludeLeaf);
		params = params.set('excludeVirtual', excludeVirtual);
		return this.http.get<ScopeMini[]>(`${this.serviceUrl}/scopes`, {params});
	}

	impersonate(profileId: string): Observable<User> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.put<User>(`${this.serviceUrl}/impersonate`, {profileId}, {headers});
	}
}
