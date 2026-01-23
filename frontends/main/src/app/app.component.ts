import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {filter} from 'rxjs/operators';
import {Environment} from '@core/model/environment';
import {ConfigurationService} from '@core/services/configuration.service';
import {PublicStudy} from '@core/model/public-study';
import {CommonModule} from '@angular/common';
import {HeaderComponent} from './header/header.component';

@Component({
	selector: 'app-root',
	standalone: true,
	imports: [RouterOutlet, CommonModule, HeaderComponent],
	templateUrl: './app.component.html',
	styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
	study?: PublicStudy;
	displayContent = true;
	showLayout = false;
	environment = Environment;

	constructor(
		private configurationService: ConfigurationService,
		private router: Router
	) {}

	ngOnInit(): void {
		this.router.events
			.pipe(filter(event => event instanceof NavigationEnd))
			.subscribe(event => {
				const navEnd = event as NavigationEnd;
				if(navEnd.url === '/projects') {
					this.study = undefined;
					this.configurationService.clearStudy();
				}

				this.updateLayoutVisibility();
			});

		this.configurationService.study$.subscribe(study => {
			if(study) {
				console.log('Study updated from service:', study);
				this.study = study;

				if(study.color) {
					document.documentElement.style.setProperty('--mat-sys-primary', study.color);
				}

				this.updateLayoutVisibility();
			}
		});
	}

	private updateLayoutVisibility(): void {
		const currentUrl = this.router.url;
		this.showLayout = currentUrl !== '/login' && currentUrl !== '/projects' && this.study !== undefined;
	}
}
