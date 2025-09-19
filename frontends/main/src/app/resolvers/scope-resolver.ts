import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {ScopeService} from '@core/services/scope.service';
import {Scope} from '@core/model/scope';

@Injectable({
	providedIn: 'root'
})
export class ScopeResolver implements Resolve<Scope> {
	constructor(
		private scopeService: ScopeService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<Scope> {
		//Since this resolver is used on two levels of the router hierarchy, we need
		//to check both the paramMap AND the parent paramMap
		const scopePkParam = route.paramMap.get('scopePk') ?? route.parent?.paramMap.get('scopePk');
		if(!scopePkParam) {
			return EMPTY;
		}
		const scopePk = parseInt(scopePkParam, 10);
		return this.scopeService.get(scopePk);
	}
}
