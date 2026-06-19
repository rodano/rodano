import {Service, inject} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {CMSLayout} from '@core/model/cms-layout';
import {ConfigurationService} from '@core/services/configuration.service';

@Service()
export class CMSLayoutResolver implements Resolve<CMSLayout> {
	private readonly configurationService = inject(ConfigurationService);

	resolve(route: ActivatedRouteSnapshot): Observable<CMSLayout> {
		const menuId = route.paramMap.get('menuId') ?? 'DASHBOARD';
		return this.configurationService.getMenuLayout(menuId);
	}
}
