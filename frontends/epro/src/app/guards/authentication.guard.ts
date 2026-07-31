import {Service, inject} from '@angular/core';
import {Router} from '@angular/router';
import {AuthStateService} from '../services/auth-state.service';

@Service()
export class AuthGuard {
	private router = inject(Router);
	private authStateService = inject(AuthStateService);

	canActivate() {
		if(this.authStateService.hasUserToken() || this.authStateService.hasRobotCredentials()) {
			return true;
		}
		else {
			this.router.navigate(['/login']);
			return false;
		}
	}
}
