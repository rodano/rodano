import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {HttpClient, HttpContext, HttpStatusCode, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {authInterceptor, SKIP_AUTH_TOKEN, SKIP_ERROR_HANDLING} from './auth.interceptor';
import {AuthStateService} from '../services/auth-state.service';
import {LoggingService} from '@core/services/logging.service';

describe('authInterceptor', () => {
	let http: HttpClient;
	let controller: HttpTestingController;
	const router = {navigate: vi.fn()};

	beforeEach(() => {
		router.navigate.mockClear();
		TestBed.configureTestingModule({
			providers: [
				provideHttpClient(withInterceptors([authInterceptor])),
				provideHttpClientTesting(),
				{provide: Router, useValue: router},
				{provide: AuthStateService, useValue: {getToken: () => 'token', hasToken: () => true}},
				{provide: LoggingService, useValue: {error: vi.fn()}}
			]
		});
		http = TestBed.inject(HttpClient);
		controller = TestBed.inject(HttpTestingController);
	});

	afterEach(() => controller.verify());

	it('adds the authentication token unless it is skipped', () => {
		http.get('/a').subscribe();
		http.get('/b', {context: new HttpContext().set(SKIP_AUTH_TOKEN, true)}).subscribe();

		expect(controller.expectOne('/a').request.headers.get('Authorization')).toBe('Bearer token');
		expect(controller.expectOne('/b').request.headers.has('Authorization')).toBe(false);
	});

	it('only skips the handling of the listed status codes', () => {
		const context = new HttpContext().set(SKIP_ERROR_HANDLING, [HttpStatusCode.Unauthorized]);
		http.get('/skipped', {context}).subscribe({error: () => undefined});
		http.get('/handled', {context}).subscribe({error: () => undefined});

		controller.expectOne('/skipped').flush(null, {status: HttpStatusCode.Unauthorized, statusText: 'Unauthorized'});
		expect(router.navigate).not.toHaveBeenCalled();

		controller.expectOne('/handled').flush(null, {status: HttpStatusCode.InternalServerError, statusText: 'Error'});
		expect(router.navigate).toHaveBeenCalledOnce();
	});

	it('skips the handling of all errors', () => {
		http.get('/a', {context: new HttpContext().set(SKIP_ERROR_HANDLING, true)}).subscribe({error: () => undefined});

		controller.expectOne('/a').flush(null, {status: HttpStatusCode.InternalServerError, statusText: 'Error'});
		expect(router.navigate).not.toHaveBeenCalled();
	});
});
