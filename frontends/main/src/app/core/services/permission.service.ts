import {Injectable} from '@angular/core';
import {combineLatest, map, Observable} from 'rxjs';
import {ProjectService} from './project.service';
import {User} from '@core/model/user';
import {AuthStateService} from '../../services/auth-state.service';

@Injectable({
	providedIn: 'root'
})
export class PermissionsService {
	constructor(
		private projectService: ProjectService,
		private authStateService: AuthStateService
	) {}

	canWrite(): Observable<boolean> {
		return combineLatest([
			this.projectService.currentProject$,
			this.authStateService.listenConnectedUser()
		]).pipe(
			map(([project, user]) => {
				if(!project || !user) {
					return false;
				}

				const status = project.status || 'ACTIVE';

				if(status === 'ARCHIVED') {
					return false;
				}

				if(status === 'CLOSED') {
					return this.isAdminOrDataCurator(user);
				}

				return true;
			})
		);
	}

	canRead(): Observable<boolean> {
		return combineLatest([
			this.projectService.currentProject$,
			this.authStateService.listenConnectedUser()
		]).pipe(
			map(([project, user]) => {
				if(!project || !user) {
					return false;
				}

				const status = project.status || 'ACTIVE';

				if(status === 'CLOSED') {
					return this.isAdminOrDataCurator(user);
				}

				return true;
			})
		);
	}

	private isAdminOrDataCurator(user: User): boolean {
		return user.roles?.some(role =>
			role.profileId === 'ADMIN'
			|| role.profileId === 'DATAMANGER'
			|| role.profileId === 'DATAENTRY_MASTER'
		) ?? false;
	}
}
