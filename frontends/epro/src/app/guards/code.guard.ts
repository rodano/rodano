import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, UrlTree, Router } from '@angular/router';
import { AlertController } from '@ionic/angular';
import { Observable, of } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { AuthStateService } from '../services/auth-state.service';
import { ConfigurationService } from '../api/services/configuration.service';

@Injectable({
	providedIn: 'root'
})
export class CodeGuard {

	constructor(
		private router: Router,
		private authStateService: AuthStateService,
		private alertCtrl: AlertController,
		private configurationService: ConfigurationService
	) { }

	canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> | boolean | UrlTree {
		// User is already logged in
		if(this.authStateService.hasUserToken() || this.authStateService.hasRobotCredentials()) {
			return this.router.parseUrl('/main/surveys');
		}

		// A robot code is passed in the parameters
		if(route.queryParamMap.has('code')) {
			const code = route.queryParamMap.get('code') as string;

			return this.authStateService.robotLogin(code).pipe(
				tap(() => {
					this.configurationService.reloadStudy();
				}),
				switchMap(() => of(this.router.parseUrl('/main/surveys'))),
				catchError(() => {
					return of(true).pipe(
						tap(async () => {
							const alert = await this.alertCtrl.create({
								header: 'Invalid code',
								message: 'Ask for a new invitation',
								buttons: ['OK']
							});
							await alert.present();
						})
					);
				})
			);
		} else {
			// Otherwise just go to the usual login page
			return true;
		}
	}
}
