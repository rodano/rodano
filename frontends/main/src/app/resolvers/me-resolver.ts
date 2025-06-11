import {Injectable} from '@angular/core';
import {Resolve} from '@angular/router';
import {Observable} from 'rxjs';
import {MeService} from '@core/services/me.service';
import {UserDTO} from '@core/model/user-dto';

@Injectable({
	providedIn: 'root'
})
export class MeResolver implements Resolve<UserDTO> {
	constructor(
		private meService: MeService
	) {}

	resolve(): Observable<UserDTO> {
		return this.meService.get();
	}
}
