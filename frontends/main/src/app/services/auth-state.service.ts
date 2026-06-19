import {Service, inject} from '@angular/core';
import {concat, Observable, Subject} from 'rxjs';
import {map, shareReplay, switchMap, tap} from 'rxjs/operators';
import {Credentials} from '@core/model/credentials';
import {AuthService} from '@core/services/auth.service';
import {User} from '@core/model/user';
import {MeService} from '@core/services/me.service';
import {RoleStatus} from '@core/model/role-status';

@Service()
export class AuthStateService {
	private static TOKEN_STORAGE_KEY = 'token';

	static getUserPendingRolesNumber(user?: User): number {
		return user?.roles.filter(r => r.status === RoleStatus.PENDING).length ?? 0;
	}

	private connectedUserSubject = new Subject<User | undefined>();

	private connectedUserStream: Observable<User | undefined>;

	private readonly authService = inject(AuthService);
	private readonly meService = inject(MeService);

	constructor() {
		this.connectedUserStream = concat(
			this.meService.tryToGet(),
			this.connectedUserSubject
		).pipe(
			shareReplay()
		);
	}

	private emitUser(user?: User) {
		this.connectedUserSubject.next(user);
	}

	updateUser(user: User) {
		this.emitUser(user);
	}

	listenConnectedUser(): Observable<User | undefined> {
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

	public login(credentials: Credentials): Observable<User> {
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
