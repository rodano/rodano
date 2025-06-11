import {Component, Input, OnInit} from '@angular/core';
import {RouterOutlet, RouterLink, RouterLinkActive} from '@angular/router';
import {UserDTO} from '@core/model/user-dto';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {MatTooltip} from '@angular/material/tooltip';
import {AuthStateService} from 'src/app/services/auth-state.service';
import {MeService} from '@core/services/me.service';

@Component({
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
	@Input() me: UserDTO;
	//user will be undefined when this component is displayed to create a new user
	@Input() user?: UserDTO;
	pendingRolesNumber = 0;

	constructor(
		private meService: MeService,
		private authStateService: AuthStateService) {}

	ngOnInit() {
		this.meService.get().subscribe(u => this.me = u);
		this.authStateService.listenConnectedUser().subscribe(user => {
			this.pendingRolesNumber = AuthStateService.getUserPendingRolesNumber(user);
		});
	}

	getState(outlet: RouterOutlet) {
		return outlet.activatedRouteData.state;
	}
}
