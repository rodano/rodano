import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {MenuConfig} from '@core/model/menu-config';
import {MenuService} from '../api/menu.service';

@Injectable({providedIn: 'root'})
export class MenuManagerService extends BaseManagerService<MenuConfig> {
	constructor(private menuService: MenuService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (m: MenuConfig) => m.menuId;}
	protected getSimpleFields(): (keyof MenuConfig)[] {
		return ['id', 'parentMenuId', 'sortOrder', 'orderBy', 'public', 'homePage'];
	}

	protected getTranslationFields(): (keyof MenuConfig)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof MenuConfig)[] {
		return ['action'];
	}

	protected fetchAll(projectId: string): Observable<MenuConfig[]> {
		return this.menuService.getMenus(projectId);
	}

	protected createEntity(projectId: string, entity: MenuConfig): Observable<MenuConfig> {
		return this.menuService.createMenu(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.menuService.deleteMenu(projectId, id);
	}
}
