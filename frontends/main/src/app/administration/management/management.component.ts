import {Component, DestroyRef, OnInit, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {forkJoin} from 'rxjs';
import {Study} from '@core/model/study';
import {AdministrationService} from '@core/services/administration.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../../services/auth-state.service';
import {NotificationService} from '../../services/notification.service';
import {MatAnchor, MatButton} from '@angular/material/button';

@Component({
	templateUrl: './management.component.html',
	styleUrls: ['./management.component.css'],
	imports: [
		MatAnchor,
		MatButton
	]
})
export class ManagementComponent implements OnInit {
	readonly study = signal<Study | undefined>(undefined);
	readonly editConfigurationLink = signal('');
	readonly inMaintenance = signal(false);
	readonly inDebug = signal(false);

	constructor(
		private administrationService: AdministrationService,
		private notificationService: NotificationService,
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		forkJoin({
			study: this.configurationService.getStudy(),
			inMaintenance: this.administrationService.isInMaintenance(),
			inDebug: this.administrationService.isInDebug()
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({study, inMaintenance, inDebug}) => {
			this.study.set(study);
			this.inMaintenance.set(inMaintenance);
			this.inDebug.set(inDebug);
		});

		this.editConfigurationLink.set(`/config/?api_url=/api&bearer_token=${this.authStateService.getToken()}`);
	}

	reloadConfiguration() {
		this.administrationService.reloadConfiguration().subscribe(() => this.notificationService.showSuccess('Configuration reloaded'));
	}

	toggleMaintenance(state: boolean) {
		this.administrationService.setMaintenance(state).subscribe(() => this.inMaintenance.set(state));
	}

	toggleDebug(state: boolean) {
		this.administrationService.setDebug(state).subscribe(() => this.inDebug.set(state));
	}
}
