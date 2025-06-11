import {Route} from '@angular/router';
import {AuthGuard} from '../guards/authentication.guard';
import {CMSLayoutResolver} from '../resolvers/cms-layout-resolver';
import {DashboardComponent} from './dashboard.component';

export default [
	//the following root is a hack
	//it allows the frontend to hard-code the path to the home page of the application (/dashboard)
	//but the real home page requires a dashboard id to be specified
	{
		path: '',
		redirectTo: 'DASHBOARD', pathMatch: 'full'
	},
	{
		path: ':menuId',
		canActivate: [AuthGuard],
		component: DashboardComponent,
		resolve: {
			layout: CMSLayoutResolver
		}
	}
] satisfies Route[];
