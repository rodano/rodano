import {Component, OnInit, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {MatToolbar} from '@angular/material/toolbar';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {AuthStateService} from '../../services/auth-state.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {AppService} from '../../services/app.service';
import {MeService} from '@core/services/me.service';
import {Scope} from '@core/model/scope';
import {environment} from '../../../environments/environment';
import {Study} from '@core/model/study';
import {LocalizerPipe} from '../../pipes/localizer.pipe';

@Component({
	templateUrl: './help.component.html',
	styleUrls: ['./help.component.css'],
	imports: [MatToolbar, MatCardModule, MatListModule, MatIcon, MatButton, LocalizerPipe]
})
export class HelpComponent implements OnInit {
	private router = inject(Router);
	private authStateService = inject(AuthStateService);
	private configurationService = inject(ConfigurationService);
	private meService = inject(MeService);
	readonly appService = inject(AppService);

	readonly study = signal<Study | undefined>(undefined);
	readonly scope = signal<Scope | undefined>(undefined);

	readonly currentVersion = environment.appVersion;

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study.set(study));
		this.meService.getRootScope().subscribe(scope => this.scope.set(scope));
	}

	logout() {
		this.authStateService.deleteRobotCredentials();
		this.appService.updateConnectedStatus();
		this.router.navigate(['/login']);
	}
}
