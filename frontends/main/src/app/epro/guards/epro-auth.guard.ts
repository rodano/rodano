import {Service, inject} from '@angular/core';
import {Router} from '@angular/router';
import {EproAuthStateService} from '../services/epro-auth-state.service';
import {EPRO_PATH} from '../epro-path';

@Service()
export class EproAuthGuard {
	private router = inject(Router);
	private eproAuthStateService = inject(EproAuthStateService);

	canActivate() {
		if(this.eproAuthStateService.hasUserToken() || this.eproAuthStateService.hasRobotCredentials()) {
			return true;
		}
		else {
			this.router.navigate(['/', EPRO_PATH, 'login']);
			return false;
		}
	}
}
