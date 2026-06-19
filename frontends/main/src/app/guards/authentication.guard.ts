import {Service, inject} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {AuthStateService} from '../services/auth-state.service';

@Service()
export class AuthGuard implements CanActivate {
	private readonly authService = inject(AuthStateService);
	private readonly router = inject(Router);

	canActivate(_: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
		if(this.authService.hasToken()) {
			return true;
		}
		//not logged in so redirect to login page with the return url
		this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
		return false;
	}
}
