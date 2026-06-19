import {Component, model} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {Scope} from '@core/model/scope';
import {NotificationService} from '../../services/notification.service';
import {TimelineGraphData} from '@core/model/timeline-graph-data';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {IssueViewerComponent} from '../issue-viewer/issue-viewer.component';
import {MatButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {of, switchMap} from 'rxjs';
import {WorkflowableUpdateService} from '../services/workflowable-update.service';

@Component({
	selector: 'app-scope-dashboard',
	templateUrl: './scope-dashboard.component.html',
	styleUrls: ['./scope-dashboard.component.css'],
	imports: [
		WorkflowStatusComponent,
		AuditTrailButtonComponent,
		IssueViewerComponent,
		MatButton
	]
})
export class ScopeDashboardComponent {
	workflowableEntity = WorkflowableEntity;

	//using a writable signal so it can be updated when the scope is updated (e.g. after lock/unlock/remove/restore)
	//however, this gives the illusion that this will be propagated to the route resolver, which is not the case
	//the route resolver will still have the old scope reference until a new resolve process is triggered
	//this means other components in the same router-outlet will not see the updated scope until a new resolve is triggered, which can be confusing
	//maybe at some point we will have "withComponentModelBinding" instead of "withComponentInputBinding"
	readonly scope = model.required<Scope>();
	graphs: TimelineGraphData[] = [];

	constructor(
		private scopeService: ScopeService,
		private workflowableUpdateService: WorkflowableUpdateService,
		private notificationService: NotificationService,
		private dialog: MatDialog
	) { }

	remove() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: true})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.scopeService.remove(this.scope().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope.set(scope);
						//used by the side menu to refresh the entities
						this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
						this.notificationService.showSuccess(`${this.scope().model.shortname['en']} removed`);
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	restore() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: false})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.scopeService.restore(this.scope().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope.set(scope);
						//used by the side menu to refresh the entities
						this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
						this.notificationService.showSuccess(`${this.scope().model.shortname['en']} restored`);
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	lock() {
		this.scopeService.lock(this.scope().pk).subscribe({
			next: scope => {
				this.scope.set(scope);
				//used by the side menu to refresh the entities
				this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
				this.notificationService.showSuccess(`${this.scope().model.shortname['en']} locked`);
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	unlock() {
		this.scopeService.unlock(this.scope().pk).subscribe({
			next: scope => {
				this.scope.set(scope);
				//used by the side menu to refresh the entities
				this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
				this.notificationService.showSuccess(`${this.scope().model.shortname['en']} unlocked`);
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	onWorkflowExecution(newScope: Workflowable) {
		this.scope.set(newScope as Scope);
	}
}
