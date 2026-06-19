import {Router, CanActivate} from '@angular/router';
import {AuthStateService} from '../services/auth-state.service';
import {Service, inject} from '@angular/core';
import {DatabaseService} from '@core/services/database.service';
import {map, Observable} from 'rxjs';

@Service()
export class LoginGuard implements CanActivate {
	private readonly databaseService = inject(DatabaseService);
	private readonly authService = inject(AuthStateService);
	private readonly router = inject(Router);

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
