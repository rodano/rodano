import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../api/services/auth.service';
import { RobotCredentials } from '../models/robotCredentials';
import { AppService } from './app.service';
import { AuthStateService } from './auth-state.service';

describe('AuthStateService', () => {
	let authStateService: AuthStateService;
	let authServiceSpy: jasmine.SpyObj<AuthService>;

	beforeEach(() => {
		const spy = jasmine.createSpyObj('AuthService', ['getRobot']);

		TestBed.configureTestingModule({
			providers: [
				AuthStateService,
				AppService,
				{ provide: AuthService, useValue: spy }
			]
		});

		authStateService = TestBed.inject(AuthStateService);
		authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
	});

	it('#robotLogin works', () => {
		const key = 'a0989ccc';
		const stubRobotCreds = {
			name: 'TastyRobot',
			key
		} as RobotCredentials;

		authServiceSpy.getRobot.and.returnValue(of(stubRobotCreds));

		authStateService.robotLogin(key).subscribe(
			robotCred => {
				expect(robotCred).toEqual(stubRobotCreds);

				expect(authStateService.getRobotCredentials()).toEqual(stubRobotCreds);

				expect(authStateService.hasRobotCredentials()).toBeTrue();

				authStateService.deleteRobotCredentials();

				expect(authStateService.hasRobotCredentials()).toBeFalse();
			},
			fail
		);
	});

	it('#robotLogin fails correctly', () => {
		const key = 'failKey';

		authServiceSpy.getRobot.and.returnValue(throwError('Incorrect key'));

		authStateService.robotLogin(key).subscribe(
			fail,
			() => {
				expect(authStateService.getRobotCredentials()).toBeUndefined();
				expect(authStateService.hasRobotCredentials()).toBeFalse();
			}
		);
	});

});
