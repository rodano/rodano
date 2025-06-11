import {AuthGuard} from '../guards/authentication.guard';
import {UserProfileComponent} from './profile/user-profile.component';
import {UserComponent} from './home/user.component';
import {UserSecurityComponent} from './security/user-security.component';
import {UserRolesComponent} from './roles/user-roles.component';
import {UserResolver} from './resolvers/user-resolver';
import {UserBrowseComponent} from './user-browse/user-browse.component';
import {UserCreateComponent} from './create/user-create.component';
import {Route} from '@angular/router';
import {MeResolver} from '../resolvers/me-resolver';

export default [
	{
		path: '',
		component: UserBrowseComponent,
		canActivate: [AuthGuard]
	},
	{
		path: 'new',
		component: UserComponent,
		canActivate: [AuthGuard],
		children: [
			{
				path: '**',
				component: UserCreateComponent
			}
		]
	},
	{
		path: ':userPk',
		component: UserComponent,
		canActivate: [AuthGuard],
		resolve: {
			user: UserResolver,
			me: MeResolver
		},
		children: [
			{
				path: 'profile',
				component: UserProfileComponent
			},
			{
				path: 'security',
				component: UserSecurityComponent
			},
			{
				path: 'roles',
				component: UserRolesComponent
			},
			{
				path: '**',
				redirectTo: 'profile'
			}
		]
	}
] satisfies Route[];
