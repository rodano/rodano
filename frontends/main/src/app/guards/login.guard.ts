import {Router, CanActivate} from '@angular/router';
import {AuthStateService} from '../services/auth-state.service';
import {Injectable} from '@angular/core';
import {DatabaseService} from '@core/services/database.service';
import {map, Observable} from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class LoginGuard implements CanActivate {
	constructor(
		private databaseService: DatabaseService,
		private authService: AuthStateService,
		private router: Router
	) {
	}

	canActivate(): Observable<boolean> {
		//if the database is blank, direct him to the bootstrap page
		return this.databaseService.status().pipe(
			map(response => {
				if(response.status === 'empty') {
					this.router.navigate(['/bootstrap']);
					return false;
				}
				//If the user is already logged in, direct him to the main dashboard
				if(this.authService.hasToken()) {
					this.router.navigate(['/dashboard']);
					return false;
				}
				return true;
			})
		);
	}
}
