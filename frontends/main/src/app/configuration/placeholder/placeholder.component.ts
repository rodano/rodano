import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {Router, NavigationEnd} from '@angular/router';
import {filter} from 'rxjs/operators';
import {MatIcon} from '@angular/material/icon';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
	selector: 'app-placeholder',
	templateUrl: './placeholder.component.html',
	imports: [
		MatIcon
	],
	styleUrls: ['./placeholder.component.css']
})
export class PlaceholderComponent implements OnInit {
	currentRoute = '';
	currentIcon = 'settings';
	currentText = 'Configuration';

	destroyRef = inject(DestroyRef);

	constructor(private router: Router) {}

	/**
		* Subscribes to router events to update content when the route changes.
		* Also sets initial values based on the current URL in case the component
		* is loaded directly (e.g., via browser refresh).
		*/
	ngOnInit(): void {
		//Set route info on init (in case it's the first load and no NavigationEnd fired yet)
		const initialUrl = this.router.url;
		this.currentRoute = initialUrl;
		this.updateContentBasedOnRoute(initialUrl);

		//Also listen for navigation changes
		this.router.events
			.pipe(
				filter(event => event instanceof NavigationEnd),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe((event: NavigationEnd) => {
				this.currentRoute = event.urlAfterRedirects;
				this.updateContentBasedOnRoute(this.currentRoute);
			});
	}

	/**
		* Updates the displayed icon and label text based on the current route.
		* @param route The full path of the current active route.
		*/
	private updateContentBasedOnRoute(route: string) {
		if(route.includes('chart')) {
			this.currentIcon = 'bar_chart';
			this.currentText = 'Charts';
		}
		else if(route === '/configuration') {
			this.currentIcon = 'settings';
			this.currentText = 'Configuration';
		}
	}
}
