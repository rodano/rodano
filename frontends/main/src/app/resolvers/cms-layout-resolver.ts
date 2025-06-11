import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {CMSLayoutDTO} from '@core/model/cms-layout-dto';
import {ConfigurationService} from '@core/services/configuration.service';

@Injectable({
	providedIn: 'root'
})
export class CMSLayoutResolver implements Resolve<CMSLayoutDTO> {
	constructor(
		private configurationService: ConfigurationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<CMSLayoutDTO> {
		const menuId = route.paramMap.get('menuId') ?? 'DASHBOARD';
		return this.configurationService.getMenuLayout(menuId);
	}
}
