import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../model/user';
import {UserCreation} from '../model/user-creation';
import {UserSearch} from '../utilities/search/user-search';
import {APIService} from './api.service';
import {HttpParamsService} from './http-params.service';
import {PagedResultUser} from '../model/paged-result-user';
import {SKIP_AUTH_TOKEN_HEADER, SKIP_ERROR_HANDLING_HEADER} from '../../interceptors/auth.interceptor';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class UserService {
	private readonly serviceUrl: string;

	constructor(
		private http: HttpClient,
		private httpParamsService: HttpParamsService,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/users`;
	}

	@reviveDates
	search(predicate: UserSearch): Observable<PagedResultUser> {
		const params = this.httpParamsService.toHttpParams(predicate);
		return this.http.get<PagedResultUser>(`${this.serviceUrl}`, {params});
	}

	getExportUrl(predicate: UserSearch): string {
		const params = this.httpParamsService.toHttpParams(predicate, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	@reviveDates
	get(userPk: number): Observable<User> {
		return this.http.get<User>(`${this.serviceUrl}/${userPk}`);
	}

	@reviveDates
	create(user: UserCreation): Observable<User> {
		return this.http.post<User>(this.serviceUrl, user);
	}

	@reviveDates
	save(userPk: number, user: User): Observable<User> {
		return this.http.put<User>(`${this.serviceUrl}/${userPk}`, user);
	}

	@reviveDates
	remove(userPk: number, rationale: string): Observable<User> {
		const params = new HttpParams()
			.set('rationale', rationale);
		return this.http.put<User>(`${this.serviceUrl}/${userPk}/remove`, undefined, {params});
	}

	@reviveDates
	restore(userPk: number, rationale: string): Observable<User> {
		const params = new HttpParams()
			.set('rationale', rationale);
		return this.http.put<User>(`${this.serviceUrl}/${userPk}/restore`, undefined, {params});
	}

	@reviveDates
	convertToLocal(userPk: number): Observable<User> {
		return this.http.put<User>(`${this.serviceUrl}/${userPk}/convert-to-local`, {});
	}

	@reviveDates
	convertToExternal(userPk: number): Observable<User> {
		return this.http.put<User>(`${this.serviceUrl}/${userPk}/convert-to-external`, {});
	}

	@reviveDates
	changePassword(userPk: number, oldPassword: string, newPassword: string): Observable<User> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		return this.http.post<User>(`${this.serviceUrl}/${userPk}/password`, {oldPassword, newPassword}, {headers});
	}

	@reviveDates
	changeEmail(userPk: number, password: string, email: string): Observable<User> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.post<User>(`${this.serviceUrl}/${userPk}/email`, {password, email}, {headers});
	}

	@reviveDates
	unblock(userPk: number): Observable<User> {
		return this.http.put<User>(`${this.serviceUrl}/${userPk}/unblock`, undefined);
	}

	recoverAccount(recoveryCode: string): Observable<undefined> {
		const headers = new HttpHeaders().set(SKIP_AUTH_TOKEN_HEADER, '').set(SKIP_ERROR_HANDLING_HEADER, '404');
		return this.http.post<undefined>(`${this.serviceUrl}/account-recovery/${recoveryCode}`, undefined, {headers});
	}

	resendEmailVerificationEmail(userPk: number): Observable<undefined> {
		return this.http.post<undefined>(`${this.serviceUrl}/${userPk}/resend-email-verification`, undefined);
	}

	resendAccountActivationEmail(userPk: number): Observable<undefined> {
		return this.http.post<undefined>(`${this.serviceUrl}/${userPk}/resend-account-activation`, undefined);
	}

	verifyUserEmail(verificationCode: string): Observable<undefined> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '404');
		return this.http.post<undefined>(`${this.serviceUrl}/email-verification/${verificationCode}`, undefined, {headers});
	}
}
