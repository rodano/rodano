import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

@Injectable()
export class AuthGuard {

	constructor(
		private router: Router,
		private authStateService: AuthStateService
	) { }

	canActivate() {
		if(this.authStateService.hasUserToken() || this.authStateService.hasRobotCredentials()) {
			return true;
		} else {
			this.router.navigate(['/login']);
			return false;
		}
	}
}
