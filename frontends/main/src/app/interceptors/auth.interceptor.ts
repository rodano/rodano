import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse, HttpStatusCode} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthStateService} from '../services/auth-state.service';
import {ErrorContext} from '../error/error-context';
import {LoggingService} from '@core/services/logging.service';

/**
 * This is the HTTP request header that lets any request possessing it skip the automatic addition of the authentication token
 */
export const SKIP_AUTH_TOKEN_HEADER = 'X-Skip-Auth-Token';
/**
 * This is the HTTP request header that lets any request possessing it skip error handling
 * If the header is empty, no error will be handled
 * If the header contains values (as a comma separated list of HTTP response code), handling of the specified response code will be skipped
 */
export const SKIP_ERROR_HANDLING_HEADER = 'X-Skip-Error-Handling';

@Injectable({
	providedIn: 'root'
})
export class AuthInterceptor implements HttpInterceptor {
	constructor(
		private router: Router,
		private authStateService: AuthStateService,
		private loggingService: LoggingService
	) {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		//retrieve interceptor configuration flags stored in headers
		const skipAuthToken = request.headers.has(SKIP_AUTH_TOKEN_HEADER);
		const skipErrorHandling = request.headers.has(SKIP_ERROR_HANDLING_HEADER);
		const skipErrorHeader = request.headers.get(SKIP_ERROR_HANDLING_HEADER);
		const skipErrors = skipErrorHeader ? skipErrorHeader.split(',').map(code => parseInt(code, 10)) : [];

		//delete these flags, as they must not be sent to the server
		//don't forget that the headers is an immutable object
		let headers = request.headers.delete(SKIP_AUTH_TOKEN_HEADER);
		headers = headers.delete(SKIP_ERROR_HANDLING_HEADER);

		//if the skip auth token flag is not set, enhance the request with the authorization token
		if(!skipAuthToken) {
			const token = this.authStateService.getToken();
			if(token) {
				headers = headers.set('Authorization', `Bearer ${token.toString()}`);
			}
		}

		const enhancedRequest = request.clone({headers});

		//if the skip error handling token is present in the request, skip the error handling of the request
		if(skipErrorHandling) {
			return next.handle(enhancedRequest);
		}

		//else handle the authorization errors
		return next.handle(enhancedRequest).pipe(
			catchError((response: HttpErrorResponse) => {
				//if the response code is in the skip error handling list, simply return the response
				if(skipErrors.includes(response.status)) {
					return throwError(() => response);
				}
				switch(response.status) {
					//401
					case HttpStatusCode.Unauthorized:
						//no stored credentials
						if(!this.authStateService.hasToken()) {
							this.loggingService.error('No stored credentials');
						}
						//simply unauthorized
						else {
							this.authStateService.tokenIsValid().subscribe(valid => {
								if(!valid) {
									this.authStateService.removeToken();
									this.loggingService.error('Invalid credentials');
								}
								else {
									this.loggingService.error('No right to perform action');
								}
							});
						}
						this.router.navigate(['/login']);
						break;
					//403
					case HttpStatusCode.Forbidden:
						if(response.error.message === 'Password must be changed') {
							this.router.navigate(['/change-password/system']);
						}
						else {
							this.loggingService.error('Forbidden');
							this.router.navigate(['/error', {context: ErrorContext.FORBIDDEN}]);
						}
						break;
					//404, 500 and 504 and no network
					case HttpStatusCode.NotFound:
					case HttpStatusCode.InternalServerError:
					case HttpStatusCode.GatewayTimeout:
					case 0:
						this.router.navigate(['/error', {context: ErrorContext.NETWORK}]);
						break;
				}
				return throwError(() => response);
			})
		);
	}
}
