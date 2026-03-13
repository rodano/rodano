import {ChangeDetectionStrategy, Component, OnInit, signal} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-welcome-text',
	templateUrl: './welcome-text.component.html',
	styleUrl: './welcome-text.component.css'
})
export class WelcomeTextComponent implements OnInit {
	readonly welcomeTextHtml = signal<SafeHtml>('');

	constructor(
		private configurationService: ConfigurationService,
		private domSanitizer: DomSanitizer
	) {}

	ngOnInit(): void {
		this.configurationService.getStudy().subscribe(
			response => {
				this.welcomeTextHtml.set(this.domSanitizer.bypassSecurityTrustHtml(response.welcomeText || ''));
			}
		);
	}
}
