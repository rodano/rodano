import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {SKIP_AUTH_TOKEN_HEADER, SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';
import {AuthenticationDTO} from '../model/authentication-dto';
import {CredentialsDTO} from '../model/credentials-dto';
import {APIService} from './api.service';
import {ResetPasswordDTO} from '../model/reset-password-dto';
import {ChangePasswordDTO} from '../model/change-password-dto';

@Injectable({
	providedIn: 'root'
})
export class AuthService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	login(credentials: CredentialsDTO): Observable<AuthenticationDTO> {
		return this.http.post<AuthenticationDTO>(
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

	changePassword(changePasswordDTO: ChangePasswordDTO): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/change`,
			changePasswordDTO,
			{
				headers: new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '')
			}
		);
	}

	resetPassword(resetPasswordDTO: ResetPasswordDTO): Observable<any> {
		return this.http.post(
			`${this.apiService.getApiUrl()}/auth/password/reset`,
			resetPasswordDTO,
			{
				headers: new HttpHeaders().set(SKIP_AUTH_TOKEN_HEADER, '').set(SKIP_ERROR_HANDLING_HEADER, '')
			}
		);
	}
}
