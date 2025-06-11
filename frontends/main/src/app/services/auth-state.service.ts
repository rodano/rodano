import {Injectable} from '@angular/core';
import {concat, Observable, Subject} from 'rxjs';
import {map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {CredentialsDTO} from '@core/model/credentials-dto';
import {AuthService} from '@core/services/auth.service';
import {UserDTO} from '@core/model/user-dto';
import {MeService} from '@core/services/me.service';
import {RoleStatus} from '@core/model/role-status';

@Injectable({
	providedIn: 'root'
})
export class AuthStateService {
	private static TOKEN_STORAGE_KEY = 'token';

	static getUserPendingRolesNumber(user?: UserDTO): number {
		return user?.roles.filter(r => r.status === RoleStatus.PENDING).length ?? 0;
	}

	private connectedUserSubject = new Subject<UserDTO | undefined>();

	private connectedUserStream: Observable<UserDTO | undefined>;

	constructor(
		private authService: AuthService,
		private meService: MeService
	) {
		this.connectedUserStream = concat(
			this.meService.tryToGet(),
			this.connectedUserSubject
		).pipe(
			shareReplay()
		);
	}

	private emitUser(user?: UserDTO) {
		this.connectedUserSubject.next(user);
	}

	updateUser(user: UserDTO) {
		this.emitUser(user);
	}

	listenConnectedUser(): Observable<UserDTO | undefined> {
		return this.connectedUserStream;
	}

	private setToken(token: string) {
		//store token in local storage to keep user logged in between page refreshes
		sessionStorage.setItem(AuthStateService.TOKEN_STORAGE_KEY, token);
	}

	public hasToken(): boolean {
		return !!sessionStorage.getItem(AuthStateService.TOKEN_STORAGE_KEY);
	}

	public getToken(): string | undefined {
		if(!this.hasToken()) {
			return undefined;
		}
		return sessionStorage.getItem(AuthStateService.TOKEN_STORAGE_KEY) as string;
	}

	public login(credentials: CredentialsDTO): Observable<UserDTO> {
		return this.authService.login(credentials).pipe(
			tap(a => this.setToken(a.token)),
			switchMap(() => this.meService.get()),
			tap(u => this.emitUser(u))
		);
	}

	public logout(): Observable<void> {
		return this.authService.logout().pipe(
			tap(() => {
				sessionStorage.removeItem(AuthStateService.TOKEN_STORAGE_KEY);
				this.emitUser(undefined);
			})
		);
	}

	public removeToken(): void {
		sessionStorage.removeItem(AuthStateService.TOKEN_STORAGE_KEY);
		this.emitUser(undefined);
	}

	public tokenIsValid(): Observable<boolean> {
		return this.meService.tryToGet().pipe(
			map(u => u !== undefined)
		);
	}
}
