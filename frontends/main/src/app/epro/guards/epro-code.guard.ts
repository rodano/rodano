import {Service, inject} from '@angular/core';
import {ActivatedRouteSnapshot, UrlTree, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {Observable, of} from 'rxjs';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {EproAuthStateService} from '../services/epro-auth-state.service';
import {ConfirmDialogComponent} from '../dialogs/confirm/confirm.dialog';
import {EPRO_PATH} from '../epro-path';

@Service()
export class EproCodeGuard {
	private router = inject(Router);
	private eproAuthStateService = inject(EproAuthStateService);
	private dialog = inject(MatDialog);

	canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> | boolean | UrlTree {
		//User is already logged in
		if(this.eproAuthStateService.hasUserToken() || this.eproAuthStateService.hasRobotCredentials()) {
			return this.router.parseUrl(`/${EPRO_PATH}/main/surveys`);
		}

		//A robot code is passed in the parameters
		if(route.queryParamMap.has('code')) {
			const code = route.queryParamMap.get('code') as string;

			return this.eproAuthStateService.robotLogin(code).pipe(
				switchMap(() => of(this.router.parseUrl(`/${EPRO_PATH}/main/surveys`))),
				catchError(() => {
					return of(true).pipe(
						tap(() => {
							this.dialog.open(ConfirmDialogComponent, {
								data: {
									title: 'Invalid code',
									message: 'Ask for a new invitation'
								}
							});
						})
					);
				})
			);
		}
		else {
			//Otherwise just go to the usual login page
			return true;
		}
	}
}
