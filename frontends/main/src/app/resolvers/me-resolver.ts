import {Service, inject} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {MeService} from '@core/services/me.service';
import {User} from '@core/model/user';

@Service()
export class MeResolver implements Resolve<User> {
	private readonly meService = inject(MeService);

	resolve(): Observable<User> {
		return this.meService.get();
	}
}
