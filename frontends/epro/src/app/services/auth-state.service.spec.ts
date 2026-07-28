import {beforeEach, afterEach, describe, it, expect, vi, type MockedObject} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {EproRobot} from '@core/model/epro-robot';
import {EproService} from '@core/services/epro.service';
import {AppService} from './app.service';
import {AuthStateService} from './auth-state.service';

//jsdom doesn't expose a working localStorage for the default "about:blank" origin, so it's stubbed here
class InMemoryStorage implements Storage {
	private readonly store = new Map<string, string>();

	get length(): number {
		return this.store.size;
	}

	clear(): void {
		this.store.clear();
	}

	getItem(key: string): string | null {
		return this.store.get(key) ?? null;
	}

	key(index: number): string | null {
		return Array.from(this.store.keys())[index] ?? null;
	}

	removeItem(key: string): void {
		this.store.delete(key);
	}

	setItem(key: string, value: string): void {
		this.store.set(key, value);
	}
}

describe('AuthStateService', () => {
	let authStateService: AuthStateService;
	let eproServiceSpy: MockedObject<EproService>;

	beforeEach(() => {
		vi.stubGlobal('localStorage', new InMemoryStorage());
		vi.stubGlobal('sessionStorage', new InMemoryStorage());

		const eproServiceMock = {
			getRobot: vi.fn().mockName('EproService.getRobot')
		};

		TestBed.configureTestingModule({
			providers: [
				AuthStateService,
				AppService,
				{provide: EproService, useValue: eproServiceMock}
			]
		});

		authStateService = TestBed.inject(AuthStateService);
		eproServiceSpy = TestBed.inject(EproService) as MockedObject<EproService>;
	});

	afterEach(() => {
		vi.unstubAllGlobals();
	});

	it('#robotLogin works', () => {
		const key = 'a0989ccc';
		const stubRobotCreds = {
			name: 'TastyRobot',
			key
		} as EproRobot;

		eproServiceSpy.getRobot.mockReturnValue(of(stubRobotCreds));

		authStateService.robotLogin(key).subscribe(robotCred => {
			expect(robotCred).toEqual(stubRobotCreds);

			expect(authStateService.getRobotCredentials()).toEqual(stubRobotCreds);

			expect(authStateService.hasRobotCredentials()).toBe(true);

			authStateService.deleteRobotCredentials();

			expect(authStateService.hasRobotCredentials()).toBe(false);
		});
	});

	it('#robotLogin fails correctly', () => {
		const key = 'failKey';

		eproServiceSpy.getRobot.mockReturnValue(throwError(() => new Error('Incorrect key')));

		authStateService.robotLogin(key).subscribe({
			error: () => {
				expect(authStateService.getRobotCredentials()).toBeUndefined();
				expect(authStateService.hasRobotCredentials()).toBe(false);
			}
		});
	});
});
