import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {map} from 'rxjs';
import {take} from 'rxjs/operators';
import {AuthStateService} from '../services/auth-state.service';

export const superuserGuard: CanActivateFn = () => {
	const authStateService = inject(AuthStateService);
	const router = inject(Router);

	return authStateService.listenConnectedUser().pipe(
		take(1),
		map(user => {
			if(user?.superuser) {
				return true;
			}
			else {
				router.navigate(['/projects']);
				return false;
			}
		})
	);
};
