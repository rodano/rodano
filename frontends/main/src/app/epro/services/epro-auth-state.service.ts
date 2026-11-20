import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {AuthService} from '@core/services/auth.service';
import {EproService} from '@core/services/epro.service';
import {EproRobot} from '@core/model/epro-robot';
import {Authentication} from '@core/model/authentication';
import {Credentials} from '@core/model/credentials';

@Service()
export class EproAuthStateService {
	private static TOKEN_STORAGE_KEY = 'eproUserToken';
	private static API_KEY_STORAGE_KEY = 'eproRobotCredentials';

	private authService = inject(AuthService);
	private eproService = inject(EproService);

	public robotLogin(code: string): Observable<EproRobot> {
		return this.eproService.getRobot(code).pipe(
			tap(robotCred => this.setRobotCredentials(robotCred))
		);
	}

	//Regular login
	public userLogin(email: string, password: string): Observable<Authentication> {
		const credentials = {
			email,
			password
		} as Credentials;

		return this.authService.login(credentials).pipe(
			tap(authentication => this.setUserToken(authentication.token))
		);
	}

	public setRobotCredentials(robotCredentials: EproRobot) {
		localStorage.setItem(EproAuthStateService.API_KEY_STORAGE_KEY, JSON.stringify(robotCredentials));
	}

	public deleteRobotCredentials() {
		localStorage.removeItem(EproAuthStateService.API_KEY_STORAGE_KEY);
	}

	public getRobotCredentials(): EproRobot | undefined {
		const key = localStorage.getItem(EproAuthStateService.API_KEY_STORAGE_KEY);
		if(key) {
			return JSON.parse(key) as EproRobot;
		}
		return undefined;
	}

	public hasRobotCredentials(): boolean {
		return !!this.getRobotCredentials();
	}

	private setUserToken(token: string) {
		sessionStorage.setItem(EproAuthStateService.TOKEN_STORAGE_KEY, token);
	}

	public hasUserToken(): boolean {
		return !!sessionStorage.getItem(EproAuthStateService.TOKEN_STORAGE_KEY);
	}

	public getUserToken(): number | undefined {
		if(!this.hasUserToken()) {
			return undefined;
		}
		const tokenStr = sessionStorage.getItem(EproAuthStateService.TOKEN_STORAGE_KEY);

		if(tokenStr) {
			return parseInt(tokenStr, 10);
		}
		else {
			return undefined;
		}
	}

	public deleteUserToken() {
		sessionStorage.removeItem(EproAuthStateService.TOKEN_STORAGE_KEY);
	}
}
