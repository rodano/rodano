import {Route} from '@angular/router';
import {ScopeListComponent} from './scope-list/scope-list.component';
import {AuthGuard} from '../guards/authentication.guard';
import {ScopeComponent} from './home/scope.component';
import {ScopeSettingsComponent} from './settings/scope-settings.component';
import {ScopeParentsComponent} from './parents/scope-parents.component';
import {ScopeResolver} from '../resolvers/scope-resolver';
import {ScopeModelResolver} from '../resolvers/scope-model-resolver';
import {ScopeCreateComponent} from './create/scope-create.component';
import {ScopeUsersComponent} from './users/scope-users.component';
import {FormResolver} from '../resolvers/form-resolver';
import {FormComponent} from '../crf/form/form.component';

export default [
	{
		path: ':scopeModelId',
		canActivate: [AuthGuard],
		component: ScopeListComponent,
		resolve: {
			scopeModel: ScopeModelResolver
		}
	},
	{
		path: ':scopeModelId/new',
		component: ScopeComponent,
		canActivate: [AuthGuard],
		resolve: {
			scopeModel: ScopeModelResolver
		},
		children: [
			{
				path: '**',
				component: ScopeCreateComponent
			}
		]
	},
	{
		path: ':scopeModelId/:scopePk',
		component: ScopeComponent,
		canActivate: [AuthGuard],
		resolve: {
			scopeModel: ScopeModelResolver,
			scope: ScopeResolver
		},
		children: [
			{
				path: 'settings',
				component: ScopeSettingsComponent
			},
			{
				path: 'parents',
				component: ScopeParentsComponent
			},
			{
				path: 'users',
				component: ScopeUsersComponent
			},
			{
				path: 'form/:formPk',
				component: FormComponent,
				resolve: {
					form: FormResolver
				}
			},
			{
				path: '**',
				redirectTo: 'settings'
			}
		]
	}
] satisfies Route[];
