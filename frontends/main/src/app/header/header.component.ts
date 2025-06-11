import {Component, OnInit, DestroyRef} from '@angular/core';
import {Router, RouterLinkActive, RouterLink} from '@angular/router';
import {MenuDTO} from '@core/model/menu-dto';
import {switchMap} from 'rxjs/operators';
import {MatMenuModule} from '@angular/material/menu';
import {MatIcon} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {UserDTO} from '@core/model/user-dto';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatBadge} from '@angular/material/badge';
import {MatTooltip} from '@angular/material/tooltip';
import {forkJoin, of} from 'rxjs';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../services/auth-state.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {PublicStudyDTO} from '@core/model/public-study-dto';
import {Environment} from '@core/model/environment';
import {ProfileDTO} from '@core/model/profile-dto';
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

	study?: PublicStudyDTO;
	logo?: string;
	user?: UserDTO;
	menus?: MenuDTO[];
	profiles: ProfileDTO[] = [];
	pendingRolesNumber = 0;

	constructor(
		private authStateService: AuthStateService,
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService,
		private router: Router) {}

	ngOnInit() {
		//set the CSS color variables to the body
		this.configurationService.getPublicStudy()
			.subscribe(study => {
				this.study = study;
				if(study.logo) {
					this.logo = btoa(study.logo);
				}

				document.body.style.setProperty('--mat-sys-primary', study.color);
				document.body.style.setProperty('--mat-sys-on-primary', 'white');

				document.body.style.setProperty('--mat-sys-outline', study.color);
				//document.body.style.setProperty('--mat-icon-color', study.color);

				document.body.style.setProperty('--mat-sys-primary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
				document.body.style.setProperty('--mat-sys-on-primary-container', 'white');
				document.body.style.setProperty('--mat-sys-secondary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
				document.body.style.setProperty('--mat-sys-on-secondary-container', 'white');
			});

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
			this.profiles = profiles.filter(profile => profile.id !== this.adminProfileId);
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
