import {HttpClient, HttpContext, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {SKIP_AUTH_TOKEN, SKIP_ERROR_HANDLING} from '../../interceptors/auth.interceptor';
import {Authentication} from '../model/authentication';
import {Credentials} from '../model/credentials';
import {APIService} from './api.service';
import {ResetPassword} from '../model/reset-password';
import {ChangePassword} from '../model/change-password';

@Service()
export class AuthService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

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
				context: new HttpContext().set(SKIP_AUTH_TOKEN, true).set(SKIP_ERROR_HANDLING, true)
			}

		);
	}

	changePassword(changePassword: ChangePassword): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/change`,
			changePassword,
			{
				context: new HttpContext().set(SKIP_ERROR_HANDLING, true)
			}
		);
	}

	resetPassword(resetPassword: ResetPassword): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/reset`,
			resetPassword,
			{
				context: new HttpContext().set(SKIP_AUTH_TOKEN, true).set(SKIP_ERROR_HANDLING, true)
			}
		);
	}
}
