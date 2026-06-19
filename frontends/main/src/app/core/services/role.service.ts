import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {Role} from '../model/role';

@Service()
export class RoleService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	getRoles(userPk: number): Observable<Role[]> {
		return this.http.get<Role[]>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`);
	}

	create(userPk: number, profileId: string, scopePk: number): Observable<Role> {
		return this.http.post<Role>(`${this.apiService.getApiUrl()}/users/${userPk}/roles`, {scopePk, profileId});
	}

	inviteToRole(userPk: number, rolePk: number): Observable<Role> {
		return this.http.put<Role>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/invite`, undefined);
	}

	enableRole(userPk: number, rolePk: number): Observable<Role> {
		return this.http.put<Role>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/enable`, undefined);
	}

	disableRole(userPk: number, rolePk: number): Observable<Role> {
		return this.http.put<Role>(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/disable`, undefined);
	}

	doAction(userPk: number, rolePk: number, action: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/action`, {action});
	}
}
