import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {AuthStateService} from '../services/auth-state.service';

@Injectable({
	providedIn: 'root'
})
export class AuthGuard implements CanActivate {
	constructor(
		private authService: AuthStateService,
		private router: Router
	) {
	}

	canActivate(_: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
		if(this.authService.hasToken()) {
			return true;
		}
		//not logged in so redirect to login page with the return url
		this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
		return false;
	}
}
