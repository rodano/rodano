import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';

@Component({
	templateUrl: './main.component.html',
	styleUrls: ['./main.component.css'],
	imports: [
		MatToolbar,
		MatIcon,
		RouterLink,
		RouterLinkActive,
		RouterOutlet
	]
})
export class MainComponent {
	readonly tabs = [
		{path: 'surveys', label: 'Surveys', icon: 'calendar_month'},
		{path: 'journal', label: 'Journal', icon: 'description'},
		{path: 'help', label: 'Help', icon: 'help'}
	];
}
