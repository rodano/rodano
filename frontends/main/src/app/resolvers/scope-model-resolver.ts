import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {EMPTY, Observable} from 'rxjs';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {ConfigurationService} from '@core/services/configuration.service';

@Injectable({
	providedIn: 'root'
})
export class ScopeModelResolver implements Resolve<ScopeModelDTO> {
	constructor(
		private configurationService: ConfigurationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<ScopeModelDTO> {
		const scopeModelId = route.paramMap.get('scopeModelId');
		if(!scopeModelId) {
			return EMPTY;
		}
		return this.configurationService.getScopeModel(scopeModelId);
	}
}
