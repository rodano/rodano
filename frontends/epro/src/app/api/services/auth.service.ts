import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RobotCredentials } from 'src/app/models/robotCredentials';
import { APIService } from './api.service';
import { Authentication } from '../model/authentication-dto';
import { Credentials } from '../model/credentials-dto';

@Injectable()
export class AuthService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	getNewToken(credentials: Credentials): Observable<Authentication> {
		return this.http.post<Authentication>(`${this.apiService.getApiUrl()}/tokens`, credentials);
	}

	// Retrieve robot from key
	getRobot(key: string): Observable<RobotCredentials> {
		return this.http.post<RobotCredentials>(`${this.apiService.getApiUrl()}/epro/robot?key=${key}`, null);
	}

	changePassword(userPassword: string, password: string) {
		const payload = {oldPassword: userPassword, newPassword: password};
		return this.http.post(`${this.apiService.getApiUrl()}/auth/password/change`, payload);
	}
}
