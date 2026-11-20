import {HttpContextToken, HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpStatusCode} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {throwError, TimeoutError} from 'rxjs';
import {catchError, timeout} from 'rxjs/operators';
import {AuthStateService} from '../services/auth-state.service';
import {ErrorContext} from '../error/error-context';
import {LoggingService} from '@core/services/logging.service';
import {NotificationService} from '../services/notification.service';
import {EproAuthStateService} from '../epro/services/epro-auth-state.service';
import {EPRO_PATH, isEproUrl} from '../epro/epro-path';

//these settings are stored in the context of the request, which is never sent to the server

/**
 * Skip the automatic addition of the authentication token
 */
export const SKIP_AUTH_TOKEN = new HttpContextToken<boolean>(() => false);
/**
 * Skip error handling
 * If true, no error will be handled
 * If it is a list of HTTP status codes, only the handling of these codes will be skipped
 */
export const SKIP_ERROR_HANDLING = new HttpContextToken<boolean | HttpStatusCode[]>(() => false);

/**
 * Maximum delay, in milliseconds, between two events of a request
 * Downloads, such as exports, must use a longer delay because they are received in one event only when they are complete
 */
export const REQUEST_TIMEOUT = new HttpContextToken<number>(() => 10_000);

/**
 * Return the URL of the page being displayed or, during a navigation, the URL of the page being navigated to
 */
function getPageUrl(router: Router): string {
	const navigation = router.currentNavigation();
	if(navigation) {
		return router.serializeUrl(navigation.extractedUrl);
	}
	//before the first navigation, the router does not know the URL yet
	return router.navigated ? router.url : window.location.pathname;
}

//the ePRO pages are used by study subjects who authenticate with their own credentials
const eproInterceptor = (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
	const router = inject(Router);
	const eproAuthStateService = inject(EproAuthStateService);
	const notificationService = inject(NotificationService);
	const loggingService = inject(LoggingService);

	const skipErrorHandling = request.context.get(SKIP_ERROR_HANDLING);
	const skipErrors = Array.isArray(skipErrorHandling) ? skipErrorHandling : [];

	let enhancedRequest = request;
	if(!request.context.get(SKIP_AUTH_TOKEN)) {
		const robotCredentials = eproAuthStateService.getRobotCredentials();
		const token = eproAuthStateService.getUserToken();
		if(robotCredentials) {
			const basic = btoa(`${robotCredentials.name}:${robotCredentials.key}`);
			enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Basic ${basic}`)});
		}
		else if(token) {
			enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Bearer ${token.toString()}`)});
		}
	}

	const timedRequest = next(enhancedRequest).pipe(timeout({each: request.context.get(REQUEST_TIMEOUT)}));

	if(skipErrorHandling === true) {
		return timedRequest;
	}

	return timedRequest.pipe(
		catchError((response: HttpErrorResponse | TimeoutError) => {
			if(response instanceof TimeoutError) {
				notificationService.showError('Can not connect to server');
				router.navigate(['/', EPRO_PATH, 'offline']);
				return throwError(() => response);
			}
			if(skipErrors.includes(response.status)) {
				return throwError(() => response);
			}
			switch(response.status) {
				case HttpStatusCode.GatewayTimeout:
				case 0:
					notificationService.showError('Can not connect to server');
					router.navigate(['/', EPRO_PATH, 'offline']);
					break;
				case HttpStatusCode.Unauthorized:
					if(!eproAuthStateService.hasRobotCredentials() && !eproAuthStateService.hasUserToken()) {
						loggingService.error('No stored credentials');
					}
					else {
						eproAuthStateService.deleteRobotCredentials();
						eproAuthStateService.deleteUserToken();
						loggingService.error('No right to perform action or invalid credentials');
					}
					router.navigate(['/', EPRO_PATH, 'login']);
					break;
				case HttpStatusCode.NotFound:
					router.navigate(['/', EPRO_PATH, 'login']);
					break;
			}
			return throwError(() => response);
		})
	);
};

export const authInterceptor: HttpInterceptorFn = (request, next) => {
	const router = inject(Router);
	if(isEproUrl(getPageUrl(router))) {
		return eproInterceptor(request, next);
	}

	const authStateService = inject(AuthStateService);
	const loggingService = inject(LoggingService);

	const skipErrorHandling = request.context.get(SKIP_ERROR_HANDLING);
	const skipErrors = Array.isArray(skipErrorHandling) ? skipErrorHandling : [];

	//if the skip auth token flag is not set, enhance the request with the authorization token
	let enhancedRequest = request;
	if(!request.context.get(SKIP_AUTH_TOKEN)) {
		const token = authStateService.getToken();
		if(token) {
			enhancedRequest = request.clone({headers: request.headers.set('Authorization', `Bearer ${token.toString()}`)});
		}
	}

	const timedRequest = next(enhancedRequest).pipe(timeout({each: request.context.get(REQUEST_TIMEOUT)}));

	//if all errors must be skipped, skip the error handling of the request
	if(skipErrorHandling === true) {
		return timedRequest;
	}

	//else handle the authorization errors
	return timedRequest.pipe(
		catchError((response: HttpErrorResponse) => {
			if(response instanceof TimeoutError) {
				router.navigate(['/error', {context: ErrorContext.NETWORK}]);
			}

			//if the response code is in the skip error handling list, simply return the response
			if(skipErrors.includes(response.status)) {
				return throwError(() => response);
			}
			switch(response.status) {
				//401
				case HttpStatusCode.Unauthorized:
					//no stored credentials
					if(!authStateService.hasToken()) {
						loggingService.error('No stored credentials');
					}
					//simply unauthorized
					else {
						authStateService.tokenIsValid().subscribe(valid => {
							if(!valid) {
								authStateService.removeToken();
								loggingService.error('Invalid credentials');
							}
							else {
								loggingService.error('No right to perform action');
							}
						});
					}
					router.navigate(['/login']);
					break;
				//403
				case HttpStatusCode.Forbidden:
					if(response.error.message === 'Password must be changed') {
						router.navigate(['/change-password/system']);
					}
					else {
						loggingService.error('Forbidden');
						router.navigate(['/error', {context: ErrorContext.FORBIDDEN}]);
					}
					break;
				//404, 500, 502, 504 and no network
				case HttpStatusCode.NotFound:
				case HttpStatusCode.InternalServerError:
				case HttpStatusCode.BadGateway:
				case HttpStatusCode.GatewayTimeout:
				case 0:
					router.navigate(['/error', {context: ErrorContext.NETWORK}]);
					break;
			}
			return throwError(() => response);
		})
	);
};
