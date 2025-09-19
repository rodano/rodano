import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {Scope} from '@core/model/scope';
import {MeService} from '@core/services/me.service';

@Injectable({
	providedIn: 'root'
})
export class RootScopesResolver implements Resolve<Scope[]> {
	constructor(
		private meService: MeService
	) {}

	resolve(): Observable<Scope[]> {
		return this.meService.getRootScopes();
	}
}
