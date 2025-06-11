import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {UserDTO} from '../model/user-dto';
import {UserCreationDTO} from '../model/user-creation-dto';
import {UserSearch} from '../utilities/search/user-search';
import {APIService} from './api.service';
import {HttpParamsService} from './http-params.service';
import {PagedResultUserDTO} from '../model/paged-result-user-dto';
import {SKIP_AUTH_TOKEN_HEADER, SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';
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
	search(predicate: UserSearch): Observable<PagedResultUserDTO> {
		const params = this.httpParamsService.toHttpParams(predicate);
		return this.http.get<PagedResultUserDTO>(`${this.serviceUrl}`, {params});
	}

	getExportUrl(predicate: UserSearch): string {
		const params = this.httpParamsService.toHttpParams(predicate, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	@reviveDates
	get(userPk: number): Observable<UserDTO> {
		return this.http.get<UserDTO>(`${this.serviceUrl}/${userPk}`);
	}

	@reviveDates
	create(user: UserCreationDTO): Observable<UserDTO> {
		return this.http.post<UserDTO>(this.serviceUrl, user);
	}

	@reviveDates
	save(userPk: number, user: UserDTO): Observable<UserDTO> {
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}`, user);
	}

	@reviveDates
	remove(userPk: number, rationale: string): Observable<UserDTO> {
		const params = new HttpParams()
			.set('rationale', rationale);
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}/remove`, undefined, {params});
	}

	@reviveDates
	restore(userPk: number, rationale: string): Observable<UserDTO> {
		const params = new HttpParams()
			.set('rationale', rationale);
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}/restore`, undefined, {params});
	}

	@reviveDates
	convertToLocal(userPk: number): Observable<UserDTO> {
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}/convert-to-local`, {});
	}

	@reviveDates
	convertToExternal(userPk: number): Observable<UserDTO> {
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}/convert-to-external`, {});
	}

	@reviveDates
	changePassword(userPk: number, oldPassword: string, newPassword: string): Observable<UserDTO> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		return this.http.post<UserDTO>(`${this.serviceUrl}/${userPk}/password`, {oldPassword, newPassword}, {headers});
	}

	@reviveDates
	changeEmail(userPk: number, password: string, email: string): Observable<UserDTO> {
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '');
		return this.http.post<UserDTO>(`${this.serviceUrl}/${userPk}/email`, {password, email}, {headers});
	}

	@reviveDates
	unblock(userPk: number): Observable<UserDTO> {
		return this.http.put<UserDTO>(`${this.serviceUrl}/${userPk}/unblock`, undefined);
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
