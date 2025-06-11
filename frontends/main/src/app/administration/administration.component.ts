import {Component} from '@angular/core';
import {RouterLinkActive, RouterLink, RouterOutlet} from '@angular/router';
import {MatTabsModule} from '@angular/material/tabs';

@Component({
	templateUrl: './administration.component.html',
	styleUrls: ['./administration.component.css'],
	imports: [
		MatTabsModule,
		RouterLinkActive,
		RouterLink,
		RouterOutlet
	]
})
export class AdministrationComponent {
}
