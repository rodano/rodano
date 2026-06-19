import {Service, inject} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {EMPTY, Observable} from 'rxjs';
import {ScopeModel} from '@core/model/scope-model';
import {ConfigurationService} from '@core/services/configuration.service';

@Service()
export class ScopeModelResolver implements Resolve<ScopeModel> {
	private readonly configurationService = inject(ConfigurationService);

	resolve(route: ActivatedRouteSnapshot): Observable<ScopeModel> {
		const scopeModelId = route.paramMap.get('scopeModelId');
		if(!scopeModelId) {
			return EMPTY;
		}
		return this.configurationService.getScopeModel(scopeModelId);
	}
}
