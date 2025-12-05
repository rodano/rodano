import {Component, OnInit, DestroyRef, Input} from '@angular/core';
import {Router, RouterLinkActive, RouterLink} from '@angular/router';
import {Menu} from '@core/model/menu';
import {switchMap} from 'rxjs/operators';
import {MatMenuModule} from '@angular/material/menu';
import {MatIcon} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {User} from '@core/model/user';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatBadge} from '@angular/material/badge';
import {MatTooltip} from '@angular/material/tooltip';
import {forkJoin, of} from 'rxjs';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../services/auth-state.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {PublicStudy} from '@core/model/public-study';
import {Environment} from '@core/model/environment';
import {Profile} from '@core/model/profile';
import {MeService} from '@core/services/me.service';
import {NotificationService} from '../services/notification.service';

@Component({
	selector: 'app-header',
	templateUrl: './header.component.html',
	styleUrls: ['./header.component.scss'],
	imports: [
		MatToolbarModule,
		MatButtonModule,
		MatTooltip,
		RouterLinkActive,
		RouterLink,
		MatIcon,
		MatMenuModule,
		LocalizeMapPipe,
		MatBadge
	]
})
export class HeaderComponent implements OnInit {
	environment = Environment;
	adminProfileId = 'ADMIN';

	@Input() study: PublicStudy;

	logo?: string;
	user?: User;
	menus?: Menu[];
	profiles: Profile[] = [];
	pendingRolesNumber = 0;

	constructor(
		private authStateService: AuthStateService,
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService,
		private router: Router) {}

	ngOnInit() {
		if(this.study.logo) {
			this.logo = btoa(this.study.logo);
		}

		this.authStateService.listenConnectedUser().pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(user => {
				return forkJoin({
					user: of(user),
					menus: user ? this.configurationService.getMenus() : of([]),
					profiles: user ? this.configurationService.getProfiles() : of([])
				});
			})
		).subscribe(({user, menus, profiles}) => {
			this.menus = menus;
			this.user = user;
			//remove admin profile from the list of profiles because it is hard-coded in the menu
			this.profiles = profiles.filter(profile => profile.profileId !== this.adminProfileId);
			this.pendingRolesNumber = AuthStateService.getUserPendingRolesNumber(user);
		});
	}

	switchProfile(profileId: string) {
		this.meService.impersonate(profileId).subscribe({
			next: u => this.authStateService.updateUser(u),
			error: () => this.notificationService.showError('Only superusers can switch profile')
		});
	}

	logout(): void {
		this.authStateService.logout().subscribe(
			() => this.router.navigate(['/login'])
		);
	}
}
