import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, convertToParamMap, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {MeService} from '@core/services/me.service';
import {firstValueFrom, isObservable, of} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {USER} from '../../../test/stubs';
import {userSecurityGuard} from './user-security.guard';

describe('userSecurityGuard', () => {
	const meService = {get: vi.fn()};
	const router = {createUrlTree: vi.fn()};
	const route = {parent: {paramMap: convertToParamMap({userPk: '2'})}} as ActivatedRouteSnapshot;

	beforeEach(() => {
		vi.resetAllMocks();
		TestBed.configureTestingModule({providers: [
			{provide: MeService, useValue: meService},
			{provide: Router, useValue: router}
		]});
	});

	async function checkAccess(pk: number, admin: boolean) {
		meService.get.mockReturnValue(of({...USER, pk, admin}));
		const result = TestBed.runInInjectionContext(() => userSecurityGuard(route, {} as RouterStateSnapshot));
		return isObservable(result) ? firstValueFrom(result) : result;
	}

	it('allows administrators to view another account security', async () => {
		expect(await checkAccess(1, true)).toBe(true);
	});

	it('allows non-administrators to view their own security', async () => {
		expect(await checkAccess(2, false)).toBe(true);
	});

	it('redirects non-administrators viewing another account to its profile', async () => {
		const redirect = new UrlTree();
		router.createUrlTree.mockReturnValue(redirect);
		expect(await checkAccess(1, false)).toBe(redirect);
		expect(router.createUrlTree).toHaveBeenCalledWith(['/users', '2', 'profile']);
	});
});
