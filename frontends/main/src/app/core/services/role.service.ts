import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {RoleDTO} from '../model/role-dto';

@Injectable({
	providedIn: 'root'
})
export class RoleService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getRoles(userPk: number): Observable<RoleDTO[]> {
		return this.http.get<RoleDTO[]>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`);
	}

	create(userPk: number, profileId: string, scopePk: number): Observable<RoleDTO> {
		return this.http.post<RoleDTO>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`, {scopePk, profileId});
	}

	inviteToRole(userPk: number, rolePk: number): Observable<RoleDTO> {
		return this.http.put<RoleDTO>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/invite`, undefined);
	}

	enableRole(userPk: number, rolePk: number): Observable<RoleDTO> {
		return this.http.put<RoleDTO>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/enable`, undefined);
	}

	disableRole(userPk: number, rolePk: number): Observable<RoleDTO> {
		return this.http.put<RoleDTO>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/disable`, undefined);
	}

	doAction(userPk: number, rolePk: number, action: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/action`, {action});
	}
}
