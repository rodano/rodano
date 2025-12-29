import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PagedResultUser } from '../model/paged-result-user-dto';
import { User } from '../model/user-dto';
import { APIService } from './api.service';
import { HttpParamsService } from './http-helper.service';
import { UserSearch } from '../utilities/search/user-search';

@Injectable({
	providedIn: 'root'
})
export class UserService {
	constructor(
		private http: HttpClient,
		private httpHelper: HttpParamsService,
		private apiService: APIService
	) { }

	search(predicate: UserSearch): Observable<PagedResultUser> {
		return this.http.get<PagedResultUser>(`${this.apiService.getApiUrl()}/users`, {params: this.httpHelper.toHttpParams(predicate)});
	}

	getExportUrl(predicate: UserSearch): string {
		const parameters = this.httpHelper.toHttpParams(predicate, ['pageSize', 'pageIndex']);
		return `${this.apiService.getApiUrl()}/users/export?${parameters}`;
	}

	get(userPk: number): Observable<User> {
		return this.http.get<User>(`${this.apiService.getApiUrl()}/users/${userPk}`);
	}

	create(user: User, scopePk: number, profileId: string): Observable<User> {
		return this.http.post<User>(`${this.apiService.getApiUrl()}/users`, user, {
			params: {
				scopePk: scopePk.toString(),
				profile: profileId
			}
		});
	}

	save(userPk: number, user: User): Observable<User> {
		return this.http.put<User>(`${this.apiService.getApiUrl()}/users/${userPk}`, user);
	}

	changePassword(userPk: number, userPassword: string, password: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/password`, {
			oldPassword: userPassword,
			newPassword: password
		});
	}

	changeEmail(userPk: number, userPassword: string, email: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/users/${userPk}/email`, {password: userPassword, email});
	}
}
