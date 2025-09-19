import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {SKIP_AUTH_TOKEN_HEADER, SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';
import {Authentication} from '../model/authentication';
import {Credentials} from '../model/credentials';
import {APIService} from './api.service';
import {ResetPassword} from '../model/reset-password';
import {ChangePassword} from '../model/change-password';

@Injectable({
	providedIn: 'root'
})
export class AuthService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	login(credentials: Credentials): Observable<Authentication> {
		return this.http.post<Authentication>(
			`${this.apiService.getApiUrl()}/sessions`,
			credentials
		);
	}

	logout(): Observable<void> {
		return this.http.delete<void>(`${this.apiService.getApiUrl()}/sessions`);
	}

	recoverPassword(email: string): Observable<any> {
		const params = new HttpParams().set('email', email);

		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/recover`,
			null,
			{
				params,
				headers: new HttpHeaders().set(SKIP_AUTH_TOKEN_HEADER, '').set(SKIP_ERROR_HANDLING_HEADER, '')
			}

		);
	}

	changePassword(changePassword: ChangePassword): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/change`,
			changePassword,
			{
				headers: new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '')
			}
		);
	}

	resetPassword(resetPassword: ResetPassword): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/reset`,
			resetPassword,
			{
				headers: new HttpHeaders().set(SKIP_AUTH_TOKEN_HEADER, '').set(SKIP_ERROR_HANDLING_HEADER, '')
			}
		);
	}
}
