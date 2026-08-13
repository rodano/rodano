import {Component, input} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {Scope} from '@core/model/scope';
import {SideMenuComponent} from '../side-menu/side-menu.component';

@Component({
	selector: 'app-crf',
	templateUrl: './crf.component.html',
	styleUrls: ['./crf.component.css'],
	imports: [
		SideMenuComponent,
		RouterOutlet
	]
})
export class CRFComponent {
	readonly scope = input.required<Scope>();
}
