import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {NotificationService} from '../../services/notification.service';
import {User} from '@core/model/user';
import {UserService} from '@core/services/user.service';

@Injectable({
	providedIn: 'root'
})
export class UserResolver implements Resolve<User> {
	constructor(
		private userService: UserService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<User> {
		//Since this resolver is used on two levels of the router hierarchy, we need
		//to check both the paramMap AND the parent paramMap
		const userPkParam = route.paramMap.get('userPk') ?? route.parent?.paramMap.get('userPk');
		if(!userPkParam) {
			this.router.navigate(['/users']);
			this.notificationService.showError('Could not find user');
			return EMPTY;
		}
		const userPk = parseInt(userPkParam, 10);
		return this.userService.get(userPk);
	}
}
