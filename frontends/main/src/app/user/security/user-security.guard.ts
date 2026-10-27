import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {MeService} from '@core/services/me.service';
import {map} from 'rxjs/operators';

export const userSecurityGuard: CanActivateFn = route => {
	const meService = inject(MeService);
	const router = inject(Router);
	const userPk = route.parent?.paramMap.get('userPk');
	return meService.get().pipe(
		map(me => me.admin || String(me.pk) === userPk ? true : router.createUrlTree(['/users', userPk, 'profile']))
	);
};
