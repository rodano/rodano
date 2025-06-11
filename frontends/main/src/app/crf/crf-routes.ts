import {Route} from '@angular/router';
import {ScopeDashboardComponent} from './scope/scope-dashboard.component';
import {CRFComponent} from './crf/crf.component';
import {FormComponent} from './form/form.component';
import {EventDashboardComponent} from './event/event-dashboard.component';
import {WorkflowStatusPathResolver} from './resolvers/workflow-status-path-resolver';
import {ScopeResolver} from '../resolvers/scope-resolver';
import {EventResolver} from '../resolvers/event-resolver';
import {FormResolver} from '../resolvers/form-resolver';
import {TimelineComponent} from './timeline/timeline.component';
import {UnsavedChangesGuard} from './guards/unsaved-changes/unsaved-changes.guard';

export default [
	{
		path: ':scopePk',
		component: CRFComponent,
		resolve: {
			scope: ScopeResolver
		},
		runGuardsAndResolvers: 'always',
		children: [
			{
				path: '',
				component: TimelineComponent
			},
			{
				path: 'dashboard',
				component: ScopeDashboardComponent
			},
			{
				path: 'form/:formPk',
				component: FormComponent,
				resolve: {
					form: FormResolver
				},
				canDeactivate: [UnsavedChangesGuard]
			},
			{
				path: 'event/:eventPk',
				component: EventDashboardComponent,
				resolve: {
					event: EventResolver
				}
			},
			{
				path: 'event/:eventPk/form/:formPk',
				component: FormComponent,
				resolve: {
					event: EventResolver,
					form: FormResolver
				},
				canDeactivate: [UnsavedChangesGuard]
			},
			{
				path: 'status/:statusPk',
				component: FormComponent,
				resolve: {
					formValue: WorkflowStatusPathResolver
				}
			}
		]
	}
] satisfies Route[];
