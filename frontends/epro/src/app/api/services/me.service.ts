import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserDTO } from '../model/user-dto';
import { APIService } from './api.service';

@Injectable()
export class MeService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	get(): Observable<UserDTO> {
		return this.http.get<UserDTO>(`${this.apiService.getApiUrl()}/users/me`);
	}
}
