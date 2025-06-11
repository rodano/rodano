import {Component} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	selector: 'app-welcome-text',
	imports: [],
	templateUrl: './welcome-text.component.html',
	styleUrl: './welcome-text.component.css'
})
export class WelcomeTextComponent {
	welcomeTextHml: any;

	constructor(
		private configurationService: ConfigurationService,
		private domSanitizer: DomSanitizer
	) {
		configurationService.getStudy().subscribe(
			response => {
				this.welcomeTextHml = this.domSanitizer.bypassSecurityTrustHtml(response.welcomeText || '');
			}
		);
	}
}
