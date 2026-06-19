import {Service, inject} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {Scope} from '@core/model/scope';
import {MeService} from '@core/services/me.service';

@Service()
export class RootScopesResolver implements Resolve<Scope[]> {
	private readonly meService = inject(MeService);

	resolve(): Observable<Scope[]> {
		return this.meService.getRootScopes();
	}
}
