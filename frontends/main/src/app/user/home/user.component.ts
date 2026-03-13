import {ChangeDetectionStrategy, Component, InjectionToken, OnInit, WritableSignal, inject, model, signal} from '@angular/core';
import {ActivatedRoute, RouterOutlet, RouterLink, RouterLinkActive} from '@angular/router';
import {User} from '@core/model/user';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {MatTooltip} from '@angular/material/tooltip';
import {AuthStateService} from 'src/app/services/auth-state.service';

export const USER_TOKEN = new InjectionToken<WritableSignal<User>>('user');

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './user.component.html',
	styleUrls: ['./user.component.css'],
	imports: [
		MatIconButton,
		RouterLink,
		MatIcon,
		MatTooltip,
		MatTabsModule,
		MatTooltip,
		RouterLinkActive,
		RouterOutlet,
		AuditTrailButtonComponent,
		MatBadge
	]
})
export class UserComponent implements OnInit {
	//user a static signal instead of an input because the user must be shared with the child components
	readonly user = inject(USER_TOKEN);
	readonly me = model.required<User>();

	readonly pendingRolesNumber = signal(0);

	constructor(
		private authStateService: AuthStateService,
		private route: ActivatedRoute) {}

	ngOnInit() {
		const user = this.route.snapshot.data['user'];
		this.user.set(user);
		this.authStateService.listenConnectedUser().subscribe(user => {
			this.pendingRolesNumber.set(AuthStateService.getUserPendingRolesNumber(user));
		});
	}
}
