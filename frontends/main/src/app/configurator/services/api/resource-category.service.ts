import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ResourceCategory} from '@core/model/resource-category';

@Injectable({
	providedIn: 'root'
})
export class ResourceCategoryService {
	constructor(private http: HttpClient) {}

	getResourceCategories(projectId: string): Observable<ResourceCategory[]> {
		return this.http.get<ResourceCategory[]>(`/api/superuser/configurator/projects/${projectId}/config/resource-categories`);
	}

	getResourceCategory(projectId: string, resourceCategoryId: string): Observable<ResourceCategory> {
		return this.http.get<ResourceCategory>(`/api/superuser/configurator/projects/${projectId}/config/resource-categories/${resourceCategoryId}`);
	}

	createResourceCategory(projectId: string, resourceCategory: ResourceCategory): Observable<ResourceCategory> {
		return this.http.post<ResourceCategory>(`/api/superuser/configurator/projects/${projectId}/config/resource-categories`, resourceCategory);
	}

	updateResourceCategory(projectId: string, resourceCategoryId: string, resourceCategory: ResourceCategory): Observable<ResourceCategory> {
		return this.http.put<ResourceCategory>(`/api/superuser/configurator/projects/${projectId}/config/resource-categories/${resourceCategoryId}`, resourceCategory);
	}

	deleteResourceCategory(projectId: string, resourceCategoryId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/resource-categories/${resourceCategoryId}`);
	}
}
