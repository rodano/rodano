import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {CMSLayout} from '@core/model/cms-layout';
import {ConfigurationService} from '@core/services/configuration.service';

@Injectable({
	providedIn: 'root'
})
export class CMSLayoutResolver implements Resolve<CMSLayout> {
	constructor(
		private configurationService: ConfigurationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<CMSLayout> {
		const menuId = route.paramMap.get('menuId') ?? 'DASHBOARD';
		return this.configurationService.getMenuLayout(menuId);
	}
}
