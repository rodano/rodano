import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {catchError, Observable, of} from 'rxjs';
import {UserDTO} from '../model/user-dto';
import {APIService} from './api.service';
import {SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {ScopeDTO} from '../model/scope-dto';
import {ScopeMiniDTO} from '../model/scope-mini-dto';

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

	get(): Observable<UserDTO> {
		return this.http.get<UserDTO>(this.serviceUrl);
	}

	/**
	 * This method tries to get the current user without throwing an error if the user is not logged in
	 * this is helpful when using this API to check is a user if effectively logged in (meaning he has a token and it is valid)
	 * this method will return undefined if the user is not logged in
	 * @returns {Observable<UserDTO>} An observable of the user
	 */
	tryToGet(): Observable<UserDTO | undefined> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.get<UserDTO>(this.serviceUrl, {headers}).pipe(
			catchError(() => of(undefined))
		);
	}

	@reviveDates
	getRootScope(): Observable<ScopeDTO> {
		return this.http.get<ScopeDTO>(`${this.serviceUrl}/root-scope`);
	}

	@reviveDates
	getRootScopes(): Observable<ScopeDTO[]> {
		return this.http.get<ScopeDTO[]>(`${this.serviceUrl}/root-scopes`);
	}

	@reviveDates
	getScopes(feature: string | undefined, excludeLeaf = true, excludeVirtual = false): Observable<ScopeMiniDTO[]> {
		let params = new HttpParams();
		if(feature) {
			params = params.set('feature', feature);
		}
		params = params.set('excludeLeaf', excludeLeaf);
		params = params.set('excludeVirtual', excludeVirtual);
		return this.http.get<ScopeMiniDTO[]>(`${this.serviceUrl}/scopes`, {params});
	}

	impersonate(profileId: string): Observable<UserDTO> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.put<UserDTO>(`${this.serviceUrl}/impersonate`, {profileId}, {headers});
	}
}
