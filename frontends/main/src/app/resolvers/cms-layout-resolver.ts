import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {CMSLayout} from '@core/model/cms-layout';
import {ConfigurationService} from '@core/services/configuration.service';
import {concatMap, map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class CMSLayoutResolver implements Resolve<CMSLayout> {
	constructor(
		private configurationService: ConfigurationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<CMSLayout> {
		const menuCode = route.paramMap.get('menuId') ?? 'DASHBOARD';

		return this.configurationService.getMenus().pipe(
			map(menus => {
				let menu = menus.find(m => m.id === menuCode);
				if(!menu) {
					for(const topMenu of menus) {
						menu = this.findMenuRecursive(topMenu, menuCode);
						if(menu) {
							break;
						}
					}
				}

				if(!menu) {
					throw new Error(`Menu with code ${menuCode} not found`);
				}
				return menu.menuId;
			}),
			concatMap(menuUuid =>
				this.configurationService.getMenuLayout(menuUuid)
			)
		);
	}

	private findMenuRecursive(menu: any, menuCode: string): any {
		for(const submenu of menu.submenus) {
			if(submenu.id === menuCode) {
				return submenu;
			}

			const found = this.findMenuRecursive(submenu, menuCode);
			if(found) {
				return found;
			}
		}
		return null;
	}
}
