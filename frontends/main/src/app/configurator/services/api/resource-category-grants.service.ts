import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class ResourceCategoryGrantsService {
	constructor(private http: HttpClient) {}

	getResourceCategoryGrants(projectId: string): Observable<Record<string, string[]>> {
		return this.http.get<Record<string, string[]>>(`/api/superuser/configurator/projects/${projectId}/config/category-grants`);
	}

	saveResourceCategoryGrants(projectId: string, grants: Record<string, string[]>): Observable<void> {
		return this.http.put<void>(`/api/superuser/configurator/projects/${projectId}/config/category-grants`, grants);
	}
}
