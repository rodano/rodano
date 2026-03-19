import {MenuManagerService} from '../manager/menu-manager.service';
import {MenuConfig} from '@core/model/menu-config';

export interface MenuContext {
	menuManager: MenuManagerService;
	menus: MenuConfig[];
	originalMenus: MenuConfig[];
	modifiedMenuIds: Set<string>;
}
