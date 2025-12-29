import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { AuthService } from '../api/services/auth.service';
import { RobotCredentials } from '../models/robotCredentials';
import { AppService } from './app.service';
import { Authentication } from '../api/model/authentication-dto';
import { Credentials } from '../api/model/credentials-dto';

@Injectable()
export class AuthStateService {

	private static TOKEN_STORAGE_KEY = 'userToken';
	private static API_KEY_STORAGE_KEY = 'robotCredentials';
	private static PROJECT_ID_STORAGE_KEY = 'eproProjectId';

	constructor(
		private authService: AuthService,
		private appService: AppService
	) { }

	public robotLogin(code: string): Observable<RobotCredentials> {
		return this.authService.getRobot(code).pipe(
			tap(robotCred => {
				this.setRobotCredentials(robotCred);
				localStorage.setItem(AuthStateService.PROJECT_ID_STORAGE_KEY, robotCred.projectId);
				this.appService.updateConnectedStatus();
			}),
			switchMap(robotCred => {
				return of(robotCred);
			})
		);
	}

	// Regular login
	public userLogin(email: string, password: string): Observable<Authentication> {
		const credentials = {
			email,
			password
		} as Credentials;

		return this.authService.getNewToken(credentials).pipe(
			tap(authentication => this.setUserToken(authentication.token))
		);
	}

	public setRobotCredentials(robotCredentials: RobotCredentials) {
		localStorage.setItem(AuthStateService.API_KEY_STORAGE_KEY, JSON.stringify(robotCredentials));
	}

	public deleteRobotCredentials() {
		localStorage.removeItem(AuthStateService.API_KEY_STORAGE_KEY);
		localStorage.removeItem(AuthStateService.PROJECT_ID_STORAGE_KEY);
	}

	public getRobotCredentials(): RobotCredentials | undefined {
		const key = localStorage.getItem(AuthStateService.API_KEY_STORAGE_KEY);
		if(key) {
			return JSON.parse(key) as RobotCredentials;
		}
		return undefined;
	}

	public hasRobotCredentials(): boolean {
		return !!this.getRobotCredentials();
	}

	public getProjectId(): string | null {
		return localStorage.getItem(AuthStateService.API_KEY_STORAGE_KEY);
	}

	private setUserToken(token: string) {
		sessionStorage.setItem(AuthStateService.TOKEN_STORAGE_KEY, token);
	}

	public hasUserToken(): boolean {
		return !!sessionStorage.getItem(AuthStateService.TOKEN_STORAGE_KEY);
	}

	public getUserToken(): number | undefined {
		if (!this.hasUserToken()) {
			return undefined;
		}
		const tokenStr = sessionStorage.getItem(AuthStateService.TOKEN_STORAGE_KEY);

		if(tokenStr) {
			return parseInt(tokenStr, 10);
		} else {
			return undefined;
		}
	}

	public deleteUserToken() {
		sessionStorage.removeItem(AuthStateService.TOKEN_STORAGE_KEY);
		localStorage.removeItem(AuthStateService.PROJECT_ID_STORAGE_KEY);
	}
}
