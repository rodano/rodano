import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {ScopeDTO} from '@core/model/scope-dto';
import {MeService} from '@core/services/me.service';

@Injectable({
	providedIn: 'root'
})
export class RootScopesResolver implements Resolve<ScopeDTO[]> {
	constructor(
		private meService: MeService
	) {}

	resolve(): Observable<ScopeDTO[]> {
		return this.meService.getRootScopes();
	}
}
