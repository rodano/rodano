import {Component, OnInit} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {filter} from 'rxjs/operators';
import {ProjectService} from '@core/services/project.service';
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
		//this.loadStudyConfiguration();

		this.router.events
			.pipe(filter(event => event instanceof NavigationEnd))
			.subscribe(() => {
				this.updateLayoutVisibility();
			});

		this.configurationService.study$.subscribe(study => {
			if(study) {
				console.log('Study updated from service:', study);
				this.study = study;
				this.loadStudyConfiguration();
			}
		});
	}

	private loadStudyConfiguration(): void {
		this.configurationService.getPublicStudy().subscribe({
			next: study => {
				console.log('Loaded study:', study);
				this.study = study;
				this.updateLayoutVisibility();
			},
			error: error => {
				if(error.status === 204) {
					console.log('No study loaded yet');
					this.study = undefined;
				}
				else {
					console.error('Failed to load study configuration', error);
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
