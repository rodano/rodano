import { Routes } from '@angular/router';
import { MainComponent } from './components/main/main.component';
import { HelpComponent } from './components/help/help.component';
import { SurveyComponent } from './components/survey/survey.component';
import { SurveysComponent } from './components/surveys/surveys.component';
import { LoginComponent } from './components/login/login/login.component';
import { JournalComponent } from './components/journal/journal.component';
import { OfflineComponent } from './components/offline/offline.component';
import { LoginQrcodeComponent } from './components/login/qr/login-qrcode.component';
import { AuthGuard } from './guards/authentication.guard';
import { CodeGuard } from './guards/code.guard';

export const APP_ROUTES: Routes = [
	{
		path: 'login',
		component: LoginComponent,
		canActivate: [CodeGuard]
	},
	{
		path: 'login-qrcode',
		component: LoginQrcodeComponent,
	},
	{
		path: 'main',
		component: MainComponent,
		canActivate: [AuthGuard],
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
			},
		]
	},
	{
		path: 'survey/:scopePk/:eventPk/:datasetPk',
		component: SurveyComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'offline',
		component: OfflineComponent
	},
	{
		path: '',
		redirectTo: '/login',
		pathMatch: 'full'
	},
	{
		path: '**',
		redirectTo: '/login'
	}
];
