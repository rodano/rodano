import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {MeService} from '@core/services/me.service';
import {User} from '@core/model/user';

@Injectable({
	providedIn: 'root'
})
export class MeResolver implements Resolve<User> {
	constructor(
		private meService: MeService
	) {}

	resolve(): Observable<User> {
		return this.meService.get();
	}
}
