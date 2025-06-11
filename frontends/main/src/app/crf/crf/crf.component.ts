import {Component, Input} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ScopeDTO} from '@core/model/scope-dto';
import {SideMenuComponent} from '../side-menu/side-menu.component';

@Component({
	selector: 'app-crf',
	templateUrl: './crf.component.html',
	styleUrls: ['./crf.component.scss'],
	imports: [
		SideMenuComponent,
		RouterOutlet
	]
})
export class CRFComponent {
	@Input() scope: ScopeDTO;
}
