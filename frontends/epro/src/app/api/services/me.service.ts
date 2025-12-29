import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../model/user-dto';
import { APIService } from './api.service';

@Injectable()
export class MeService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	get(): Observable<User> {
		return this.http.get<User>(`${this.apiService.getApiUrl()}/users/me`);
	}
}
