import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AuthStateService} from '../../services/auth-state.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {AppService} from '../../services/app.service';
import {MeService} from '@core/services/me.service';
import {Scope} from '@core/model/scope';
import {environment} from '../../../environments/environment';
import {Study} from '@core/model/study';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {IonButton, IonButtons, IonCard, IonCardContent, IonCardHeader, IonCardSubtitle, IonCardTitle, IonContent, IonHeader, IonIcon, IonItem, IonLabel, IonList, IonTitle, IonToolbar} from '@ionic/angular/standalone';

@Component({
	templateUrl: './help.component.html',
	styleUrls: ['./help.component.css'],
	standalone: true,
	imports: [IonButton, IonButtons, IonCard, IonCardContent, IonCardHeader, IonCardSubtitle, IonCardTitle, IonContent, IonHeader, IonIcon, IonItem, IonLabel, IonList, IonTitle, IonToolbar, LocalizerPipe]
})
export class HelpComponent implements OnInit {
	study: Study;
	scope: Scope;
	platformInfo: string;
	devMode: boolean;
	selectedLanguage = 'en';
	currentVersion: string = environment.appVersion;

	constructor(
		private router: Router,
		private authStateService: AuthStateService,
		public appService: AppService,
		private configurationService: ConfigurationService,
		private meService: MeService
	) { }

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study = study);
		this.meService.getRootScope().subscribe(scope => this.scope = scope);
	}

	public logout(): void {
		this.authStateService.deleteRobotCredentials();
		this.appService.updateConnectedStatus();
		this.router.navigate(['/login']);
	}
}
