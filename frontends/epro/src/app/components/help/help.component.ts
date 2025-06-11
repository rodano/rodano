import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { ConfigurationService } from '../../api/services/configuration.service';
import { AppService } from '../../services/app.service';
import { ScopeDTO } from 'src/app/api/model/scope-dto';
import { environment } from '../../../environments/environment';
import { StudyDTO } from 'src/app/api/model/study-dto';
import { LocalizerPipe } from '../../pipes/localizer.pipe';
import { IonicModule } from '@ionic/angular';

@Component({
	templateUrl: './help.component.html',
	styleUrls: ['./help.component.css'],
	standalone: true,
	imports: [IonicModule, LocalizerPipe]
})
export class HelpComponent implements OnInit {

	study: StudyDTO;
	scope: ScopeDTO;
	platformInfo: string;
	devMode: boolean;
	selectedLanguage = 'en';
	currentVersion: string = environment.appVersion;

	constructor(
		private router: Router,
		private authStateService: AuthStateService,
		public appService: AppService,
		private configurationService: ConfigurationService
	) { }

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study = study);
		this.configurationService.getRootScope().subscribe(scope => this.scope = scope);
	}

	public logout(): void {
		this.authStateService.deleteUserToken();
		this.authStateService.deleteRobotCredentials();
		this.appService.updateConnectedStatus();
		this.router.navigate(['/login']);
	}

}
