import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {ToastController} from '@ionic/angular/standalone';
import {throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthStateService} from './services/auth-state.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
	const router = inject(Router);
	const authStateService = inject(AuthStateService);
	const toastCtrl = inject(ToastController);

	async function presentOfflineToast() {
		const errToast = await toastCtrl.create({
			position: 'bottom',
			header: 'Error',
			message: 'Can not connect to server',
			color: 'danger',
			duration: 3000
		});
		await errToast.present();
	}

	let enhancedRequest;
	const robotCredentials = authStateService.getRobotCredentials();
	const token = authStateService.getUserToken();

	if(robotCredentials) {
		//enhance request with authorization basic key
		const basic = btoa(`${robotCredentials.name}:${robotCredentials.key}`);
		enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Basic ${basic}`)});
	}
	else if(token) {
		//enhance request with authorization bearer token
		enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Bearer ${token.toString()}`)});
	}
	else {
		enhancedRequest = request;
	}

	return next(enhancedRequest).pipe(
		catchError((response: HttpErrorResponse) => {
			//only catch specific HTTP errors here
			//do not try to catch general errors (response having an error message) and give feedback from here (e.g. with an alert)
			//general errors must be handled by the code issuing this request
			switch(response.status) {
				case 0:
				case 504:
					presentOfflineToast();
					router.navigate(['/offline']);
					break;
				case 401:
					//no stored credentials
					if(!authStateService.hasRobotCredentials() && !authStateService.hasUserToken()) {
						console.log('No stored credentials');
					}
					//simply unauthorized
					else {
						authStateService.deleteRobotCredentials();
						authStateService.deleteUserToken();
						console.log('No right to perform action or invalid credentials');
					}
					router.navigate(['/login']);
					break;
				case 404:
					//if URL has not been found, server can be considered as an invalid server
					router.navigate(['/login']);
					break;
			}
			return throwError(() => response);
		})
	);
};
