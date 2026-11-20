import {Route} from '@angular/router';
import {EproComponent} from './epro.component';
import {MainComponent} from './components/main/main.component';
import {HelpComponent} from './components/help/help.component';
import {SurveyComponent} from './components/survey/survey.component';
import {SurveysComponent} from './components/surveys/surveys.component';
import {LoginComponent} from './components/login/login/login.component';
import {JournalComponent} from './components/journal/journal.component';
import {OfflineComponent} from './components/offline/offline.component';
import {LoginQrcodeComponent} from './components/login/qr/login-qrcode.component';
import {EproAuthGuard} from './guards/epro-auth.guard';
import {EproCodeGuard} from './guards/epro-code.guard';

export default [
	{
		path: '',
		component: EproComponent,
		children: [
			{
				path: 'login',
				component: LoginComponent,
				canActivate: [EproCodeGuard]
			},
			{
				path: 'login-qrcode',
				component: LoginQrcodeComponent
			},
			{
				path: 'main',
				component: MainComponent,
				canActivate: [EproAuthGuard],
				children: [
					{
						path: 'surveys',
						component: SurveysComponent
					},
					{
						path: 'journal',
						component: JournalComponent
					},
					{
						path: 'help',
						component: HelpComponent
					}
				]
			},
			{
				path: 'survey/:scopePk/:eventPk/:datasetPk',
				component: SurveyComponent,
				canActivate: [EproAuthGuard]
			},
			{
				path: 'offline',
				component: OfflineComponent
			},
			{
				path: '**',
				redirectTo: 'login'
			}
		]
	}
] satisfies Route[];
