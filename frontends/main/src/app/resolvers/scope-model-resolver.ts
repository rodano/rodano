import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {EMPTY, Observable} from 'rxjs';
import {ScopeModel} from '@core/model/scope-model';
import {ConfigurationService} from '@core/services/configuration.service';

@Injectable({
	providedIn: 'root'
})
export class ScopeModelResolver implements Resolve<ScopeModel> {
	constructor(
		private configurationService: ConfigurationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<ScopeModel> {
		const scopeModelId = route.paramMap.get('scopeModelId');
		if(!scopeModelId) {
			return EMPTY;
		}
		return this.configurationService.getScopeModel(scopeModelId);
	}
}
