import {Component, Input} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {Scope} from '@core/model/scope';
import {NotificationService} from 'src/app/services/notification.service';
import {TimelineGraphData} from '@core/model/timeline-graph-data';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {IssueViewerComponent} from '../issue-viewer/issue-viewer.component';
import {MatButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {of, switchMap} from 'rxjs';
import {CRFChangeService} from '../services/crf-change.service';

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

	@Input() scope: Scope;
	graphs: TimelineGraphData[] = [];

	constructor(
		private scopeService: ScopeService,
		private crfChangeService: CRFChangeService,
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
						return this.scopeService.remove(this.scope.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope = scope;
						//used by the side menu to refresh the entities
						this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
						this.notificationService.showSuccess(`${this.scope.model.shortname['en']} removed`);
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
						return this.scopeService.restore(this.scope.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: scope => {
					if(scope) {
						this.scope = scope;
						//used by the side menu to refresh the entities
						this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
						this.notificationService.showSuccess(`${this.scope.model.shortname['en']} restored`);
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	lock() {
		this.scopeService.lock(this.scope.pk).subscribe({
			next: scope => {
				this.scope = scope;
				//used by the side menu to refresh the entities
				this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
				this.notificationService.showSuccess(`${this.scope.model.shortname['en']} locked`);
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	unlock() {
		this.scopeService.unlock(this.scope.pk).subscribe({
			next: scope => {
				this.scope = scope;
				//used by the side menu to refresh the entities
				this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.SCOPE, scope);
				this.notificationService.showSuccess(`${this.scope.model.shortname['en']} unlocked`);
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	onWorkflowExecution(newScope: Workflowable) {
		this.scope = newScope as Scope;
	}
}
