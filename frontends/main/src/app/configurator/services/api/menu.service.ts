import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MenuConfig} from '@core/model/menu-config';

@Injectable({
	providedIn: 'root'
})
export class MenuService {
	constructor(private http: HttpClient) {}

	getMenus(projectId: string): Observable<MenuConfig[]> {
		return this.http.get<MenuConfig[]>(`/api/superuser/configurator/projects/${projectId}/config/menus`);
	}

	getMenu(projectId: string, menuId: string): Observable<MenuConfig> {
		return this.http.get<MenuConfig>(`/api/superuser/configurator/projects/${projectId}/config/menus/${menuId}`);
	}

	createMenu(projectId: string, menu: MenuConfig): Observable<MenuConfig> {
		return this.http.post<MenuConfig>(`/api/superuser/configurator/projects/${projectId}/config/menus`, menu);
	}

	updateMenu(projectId: string, menuId: string, menu: MenuConfig): Observable<MenuConfig> {
		return this.http.put<MenuConfig>(`/api/superuser/configurator/projects/${projectId}/config/menus/${menuId}`, menu);
	}

	deleteMenu(projectId: string, menuId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/menus/${menuId}`);
	}
}
