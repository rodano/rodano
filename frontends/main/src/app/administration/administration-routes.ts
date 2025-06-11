import {Route} from '@angular/router';
import {AuthGuard} from '../guards/authentication.guard';
import {AdministrationComponent} from './administration.component';
import {ConnectedUsersComponent} from './connected-users/connected-users.component';
import {DetailsComponent} from './details/details.component';
import {ManagementComponent} from './management/management.component';
import {ScheduledTasksComponent} from './scheduled-tasks/scheduled-tasks.component';

export default [
	{
		path: '',
		component: AdministrationComponent,
		canActivate: [AuthGuard],
		children: [
			{
				path: 'management',
				component: ManagementComponent
			},
			{
				path: 'details',
				component: DetailsComponent
			},
			{
				path: 'connected-users',
				component: ConnectedUsersComponent
			},
			{
				path: 'scheduled-tasks',
				component: ScheduledTasksComponent
			},
			{
				path: '**',
				redirectTo: 'management'
			}
		]
	}
] satisfies Route[];
