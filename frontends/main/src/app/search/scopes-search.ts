import {Route} from '@angular/router';
import {SearchComponent} from './scopes-search.component';
import {AuthGuard} from '../guards/authentication.guard';
import {ScopeModelResolver} from '../resolvers/scope-model-resolver';

export default [
	{
		path: '',
		canActivate: [AuthGuard],
		component: SearchComponent

	},
	{
		path: ':scopeModelId',
		canActivate: [AuthGuard],
		component: SearchComponent,
		resolve: {
			scopeModel: ScopeModelResolver
		}
	}
] satisfies Route[];
