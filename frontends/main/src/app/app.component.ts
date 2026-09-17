import {Component, OnInit, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './header/header.component';
import {Environment} from '@core/model/environment';
import {PublicStudy} from '@core/model/public-study';
import {AdministrationService} from '@core/services/administration.service';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.css'],
	imports: [HeaderComponent, RouterOutlet]
})
export class AppComponent implements OnInit {
	constructor(
		private administrationService: AdministrationService,
		private configurationService: ConfigurationService
	) {}

	environment = Environment;

	readonly study = signal<PublicStudy | undefined>(undefined);
	readonly inMaintenance = signal(false);
	readonly displayContent = signal(false);

	ngOnInit() {
		this.administrationService.isInMaintenance().subscribe(inMaintenance => {
			this.inMaintenance.set(inMaintenance);
			if(!inMaintenance) {
				this.loadStudy();
			}
		});
	}

	bypassMaintenance() {
		this.inMaintenance.set(false);
		this.loadStudy();
	}

	private loadStudy() {
		//set the CSS color variables to the body
		this.configurationService.getPublicStudy()
			.subscribe({
				next: study => {
					document.body.style.setProperty('--mat-sys-primary', study.color);
					document.body.style.setProperty('--mat-sys-on-primary', 'white');

					document.body.style.setProperty('--mat-sys-outline', study.color);
					//document.body.style.setProperty('--mat-icon-color', study.color);

					document.body.style.setProperty('--mat-sys-primary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
					document.body.style.setProperty('--mat-sys-on-primary-container', 'white');
					document.body.style.setProperty('--mat-sys-secondary-container', 'color(from var(--mat-sys-primary) display-p3 calc(r - 0.1) calc(g - 0.1) calc(b - 0.1))');
					document.body.style.setProperty('--mat-sys-on-secondary-container', 'white');

					document.body.style.setProperty('--themed-highlight-color', 'color-mix(in srgb, var(--mat-sys-primary) 15%, white)');

					this.study.set(study);
					this.displayContent.set(true);
				},
				error: () => {
					this.displayContent.set(true);
				}
			});
	}
}
