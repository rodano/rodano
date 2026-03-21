import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class MenuGrantsService {
	constructor(private http: HttpClient) {}

	getMenuGrants(projectId: string): Observable<Record<string, string[]>> {
		return this.http.get<Record<string, string[]>>(`/api/superuser/configurator/projects/${projectId}/config/menu-grants`);
	}

	saveMenuGrants(projectId: string, grants: Record<string, string[]>): Observable<void> {
		return this.http.put<void>(`/api/superuser/configurator/projects/${projectId}/config/menu-grants`, grants);
	}
}
