import {Component, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './header/header.component';
import {Environment} from '@core/model/environment';
import {PublicStudyDTO} from '@core/model/public-study-dto';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.css'],
	imports: [HeaderComponent, RouterOutlet]
})
export class AppComponent implements OnInit {
	constructor(private configurationService: ConfigurationService) {}

	environment = Environment;

	study?: PublicStudyDTO;

	ngOnInit() {
		//set the CSS color variables to the body
		this.configurationService.getPublicStudy()
			.subscribe(study => {
				this.study = study;

				document.body.style.setProperty('--mat-sys-primary', study.color);
				document.body.style.setProperty('--mat-sys-on-primary', 'white');

				document.body.style.setProperty('--mat-sys-outline', study.color);
				//document.body.style.setProperty('--mat-icon-color', study.color);

				document.body.style.setProperty('--mat-sys-primary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
				document.body.style.setProperty('--mat-sys-on-primary-container', 'white');
				document.body.style.setProperty('--mat-sys-secondary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
				document.body.style.setProperty('--mat-sys-on-secondary-container', 'white');
			});
	}
}
