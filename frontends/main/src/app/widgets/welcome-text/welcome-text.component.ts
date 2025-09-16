import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	selector: 'app-welcome-text',
	templateUrl: './welcome-text.component.html',
	styleUrl: './welcome-text.component.css'
})
export class WelcomeTextComponent implements OnInit {
	welcomeTextHtml: SafeHtml;

	constructor(
		private configurationService: ConfigurationService,
		private domSanitizer: DomSanitizer
	) {}

	ngOnInit(): void {
		this.configurationService.getStudy().subscribe(
			response => {
				this.welcomeTextHtml = this.domSanitizer.bypassSecurityTrustHtml(response.welcomeText || '');
			}
		);
	}
}
