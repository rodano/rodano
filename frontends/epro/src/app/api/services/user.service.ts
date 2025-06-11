import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PagedResult } from '../model/paged-result';
import { UserDTO } from '../model/user-dto';
import { UserSearch } from '../model/user-search';
import { APIService } from './api.service';
import { HttpParamsService } from './http-helper.service';

@Injectable({
	providedIn: 'root'
})
export class UserService {
	constructor(
		private http: HttpClient,
		private httpHelper: HttpParamsService,
		private apiService: APIService
	) { }

	search(predicate: UserSearch): Observable<PagedResult<UserDTO>> {
		return this.http.get<PagedResult<UserDTO>>(`${this.apiService.getApiUrl()}/users`, {params: this.httpHelper.toHttpParams(predicate)});
	}

	getExportUrl(predicate: UserSearch): string {
		const parameters = this.httpHelper.toHttpParams(predicate, ['pageSize', 'pageIndex']);
		return `${this.apiService.getApiUrl()}/users/export?${parameters}`;
	}

	get(userPk: number): Observable<UserDTO> {
		return this.http.get<UserDTO>(`${this.apiService.getApiUrl()}/users/${userPk}`);
	}

	create(user: UserDTO, scopePk: number, profileId: string): Observable<UserDTO> {
		return this.http.post<UserDTO>(`${this.apiService.getApiUrl()}/users`, user, {params: {scopePk: scopePk.toString(), profile: profileId}});
	}

	save(userPk: number, user: UserDTO): Observable<UserDTO> {
		return this.http.put<UserDTO>(`${this.apiService.getApiUrl()}/users/${userPk}`, user);
	}

	changePassword(userPk: number, userPassword: string, password: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/password`, {oldPassword: userPassword, newPassword: password});
	}

	changeEmail(userPk: number, userPassword: string, email: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/email`, {password: userPassword, email});
	}
}
