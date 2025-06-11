import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RobotCredentials } from 'src/app/models/robotCredentials';
import { APIService } from './api.service';
import { AuthenticationDTO } from '../model/authentication-dto';
import { CredentialsDTO } from '../model/credentials-dto';

@Injectable()
export class AuthService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	getNewToken(credentials: CredentialsDTO): Observable<AuthenticationDTO> {
		return this.http.post<AuthenticationDTO>(`${this.apiService.getApiUrl()}/tokens`, credentials);
	}

	// Retrieve robot from key
	getRobot(key: string): Observable<RobotCredentials> {
		const payload = new FormData();
		payload.append('key', key);
		return this.http.post<RobotCredentials>(`${this.apiService.getApiUrl()}/epro/robot`, payload);
	}

	changePassword(userPassword: string, password: string) {
		const payload = {oldPassword: userPassword, newPassword: password};
		return this.http.post(`${this.apiService.getApiUrl()}/auth/password/change`, payload);
	}
}
