import {Component, OnInit, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {filter, map} from 'rxjs/operators';
import {HeaderComponent} from './header/header.component';
import {Environment} from '@core/model/environment';
import {PublicStudy} from '@core/model/public-study';
import {AdministrationService} from '@core/services/administration.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {isEproUrl} from './epro/epro-path';

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

	//the ePRO pages are used by study subjects and must not display the elements of the staff application
	readonly inEpro = toSignal(
		inject(Router).events.pipe(
			filter(event => event instanceof NavigationEnd),
			map(event => isEproUrl(event.urlAfterRedirects))
		),
		{initialValue: isEproUrl(window.location.pathname)}
	);

	readonly study = signal<PublicStudy | undefined>(undefined);
	readonly inMaintenance = signal(false);

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
		this.configurationService.getPublicStudy().subscribe(study => {
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
		});
	}
}
