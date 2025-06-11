import {Component, OnInit} from '@angular/core';
import {StudyDTO} from '@core/model/study-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {MatTabsModule} from '@angular/material/tabs';
import {RouterLink, RouterLinkActive, RouterOutlet, Routes} from '@angular/router';
import {RequirementsComponent} from './requirements/requirements.component';
import {SupportComponent} from './support/support.component';
import {AuthGuard} from '../guards/authentication.guard';

@Component({
	templateUrl: './help.component.html',
	styleUrls: ['./help.component.css'],
	imports: [
		MatTabsModule,
		RouterLinkActive,
		RouterLink,
		RouterOutlet
	]
})
export class HelpComponent implements OnInit {
	static ROUTES: Routes = [
		{
			path: '',
			component: HelpComponent,
			canActivate: [AuthGuard],
			children: [
				{
					path: 'requirements',
					component: RequirementsComponent
				},
				{
					path: 'support',
					component: SupportComponent
				},
				{
					path: '**',
					redirectTo: 'requirements'
				}
			]
		}
	];

	study: StudyDTO;

	constructor(
		public configurationService: ConfigurationService
	) {}

	ngOnInit() {
		this.configurationService.getStudy().subscribe(study => this.study = study);
	}
}
