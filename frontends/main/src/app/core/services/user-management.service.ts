import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {UserManagement} from '@core/model/user-management';
import {CreateUserRequest} from '@core/model/create-user-request';
import {UpdateUserRequest} from '@core/model/update-user-request';

@Injectable({
	providedIn: 'root'
})
export class UserManagementService {
	private readonly baseUrl = '/api/superuser/users';

	constructor(private http: HttpClient) {}

	getAllUsers(): Observable<UserManagement[]> {
		return this.http.get<UserManagement[]>(this.baseUrl);
	}

	getUser(userPk: number | undefined): Observable<UserManagement> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.get<UserManagement>(`${this.baseUrl}/${userPk}`);
	}

	createUser(request: CreateUserRequest): Observable<UserManagement> {
		return this.http.post<UserManagement>(`${this.baseUrl}`, request);
	}

	updateUser(userPk: number | undefined, request: UpdateUserRequest): Observable<UserManagement> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.put(`${this.baseUrl}/${userPk}`, request);
	}

	deleteUser(userPk: number | undefined): Observable<void> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.delete<void>(`${this.baseUrl}/${userPk}`);
	}

	restoreUser(userPk: number | undefined): Observable<void> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.post<void>(`${this.baseUrl}/${userPk}/restore`, {});
	}

	toggleSuperuser(userPk: number | undefined, isSuperuser: boolean): Observable<void> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.put<void>(`${this.baseUrl}/${userPk}/superuser`, {isSuperuser});
	}

	unblockUser(userPk: number | undefined): Observable<void> {
		if(userPk === undefined) {
			return throwError(() => new Error('User PK is required'));
		}
		return this.http.post<void>(`${this.baseUrl}/${userPk}/unblock`, {});
	}
}
