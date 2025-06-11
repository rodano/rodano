import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ToastController } from '@ionic/angular';
import { from, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthStateService } from './services/auth-state.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
	constructor(
		private router: Router,
		private authStateService: AuthStateService,
		private toastCtrl: ToastController
	) { }

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return from(this.handle(request, next));
	}

	async handle(request: HttpRequest<any>, next: HttpHandler): Promise<HttpEvent<any>> {
		let enhancedRequest;
		const robotCredentials = this.authStateService.getRobotCredentials();
		const token = this.authStateService.getUserToken();

		if(robotCredentials) {
			// enhance request with authorization basic key
			const basic = btoa(`${robotCredentials.name}:${robotCredentials.key}`);
			enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Basic ${basic}`)});
		}
		else if(token) {
			// enhance request with authorization bearer token
			enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Bearer ${token.toString()}`)});
		}
		else {
			enhancedRequest = request;
		}

		const errToast = await this.toastCtrl.create({
			position: 'bottom',
			header: 'Error',
			message: 'Can not connect to server',
			color: 'danger',
			duration: 3000
		});

		return next.handle(enhancedRequest).pipe(
			catchError((response: HttpErrorResponse) => {
				// only catch specific HTTP errors here
				// do not try to catch general errors (response having an error message) and give feedback from here (e.g. with an alert)
				// general errors must be handled by the code issuing this request
				switch(response.status) {
					case 0:
					case 504:
						errToast.present();
						this.router.navigate(['/offline']);
						break;
					case 401:
						// no stored credentials
						if(!this.authStateService.hasRobotCredentials() && !this.authStateService.hasUserToken()) {
							console.log('No stored credentials');
						}
						// simply unauthorized
						else {
							this.authStateService.deleteRobotCredentials();
							this.authStateService.deleteUserToken();
							console.log('No right to perform action or invalid credentials');
						}
						this.router.navigate(['/login']);
						break;
					case 404:
						// if URL has not been found, server can be considered as an invalid server
						this.router.navigate(['/login']);
						break;
				}
				return throwError(response);
			})
		).toPromise() as Promise<HttpEvent<any>>;
	}
}
