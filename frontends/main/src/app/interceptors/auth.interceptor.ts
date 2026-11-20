import {HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpStatusCode} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {throwError, TimeoutError} from 'rxjs';
import {catchError, timeout} from 'rxjs/operators';
import {AuthStateService} from '../services/auth-state.service';
import {ErrorContext} from '../error/error-context';
import {LoggingService} from '@core/services/logging.service';

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

export const authInterceptor: HttpInterceptorFn = (request, next) => {
	const router = inject(Router);
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
