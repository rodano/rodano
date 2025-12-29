import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { APIService } from './api.service';
import { Role } from '../model/role-dto';

@Injectable({
	providedIn: 'root'
})
export class RoleService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	get(userPk: number): Observable<Role[]> {
		return this.http.get<Role[]>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`);
	}

	create(userPk: number, profileId: string, scopePk: number): Observable<Role> {
		return this.http.post<Role>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`, {scopePk, profileId});
	}

	doAction(userPk: number, rolePk: number, action: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/action`, {action});
	}
}
