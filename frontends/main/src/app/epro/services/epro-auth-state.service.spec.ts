import {beforeEach, afterEach, describe, it, expect, vi, type MockedObject} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {EproRobot} from '@core/model/epro-robot';
import {EproService} from '@core/services/epro.service';
import {EproAuthStateService} from './epro-auth-state.service';

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

describe('EproAuthStateService', () => {
	let eproAuthStateService: EproAuthStateService;
	let eproServiceSpy: MockedObject<EproService>;

	beforeEach(() => {
		vi.stubGlobal('localStorage', new InMemoryStorage());
		vi.stubGlobal('sessionStorage', new InMemoryStorage());

		const eproServiceMock = {
			getRobot: vi.fn().mockName('EproService.getRobot')
		};

		TestBed.configureTestingModule({
			providers: [
				EproAuthStateService,
				{provide: EproService, useValue: eproServiceMock}
			]
		});

		eproAuthStateService = TestBed.inject(EproAuthStateService);
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

		eproAuthStateService.robotLogin(key).subscribe(robotCred => {
			expect(robotCred).toEqual(stubRobotCreds);

			expect(eproAuthStateService.getRobotCredentials()).toEqual(stubRobotCreds);

			expect(eproAuthStateService.hasRobotCredentials()).toBe(true);

			eproAuthStateService.deleteRobotCredentials();

			expect(eproAuthStateService.hasRobotCredentials()).toBe(false);
		});
	});

	it('#robotLogin fails correctly', () => {
		const key = 'failKey';

		eproServiceSpy.getRobot.mockReturnValue(throwError(() => new Error('Incorrect key')));

		eproAuthStateService.robotLogin(key).subscribe({
			error: () => {
				expect(eproAuthStateService.getRobotCredentials()).toBeUndefined();
				expect(eproAuthStateService.hasRobotCredentials()).toBe(false);
			}
		});
	});
});
