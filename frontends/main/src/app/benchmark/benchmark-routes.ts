import {Route} from '@angular/router';
import {BenchmarkComponent} from './benchmark.component';
import {AuthGuard} from '../guards/authentication.guard';
import {CMSLayoutResolver} from '../resolvers/cms-layout-resolver';

export default [
	{
		path: '',
		redirectTo: 'BENCHMARKING', pathMatch: 'full'
	},
	{
		path: ':menuId',
		canActivate: [AuthGuard],
		component: BenchmarkComponent,
		resolve: {
			layout: CMSLayoutResolver
		}
	}
] satisfies Route[];
