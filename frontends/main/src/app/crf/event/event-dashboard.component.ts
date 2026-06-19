import {ChangeDetectionStrategy, Component, computed, input, model} from '@angular/core';
import {Event} from '@core/model/event';
import {Scope} from '@core/model/scope';
import {EventService} from '@core/services/event.service';
import {differenceInDays} from 'date-fns';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {MatIcon} from '@angular/material/icon';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {IssueViewerComponent} from '../issue-viewer/issue-viewer.component';
import {NotificationService} from '../../services/notification.service';
import {MatButton} from '@angular/material/button';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {MatDialog} from '@angular/material/dialog';
import {of, switchMap} from 'rxjs';
import {WorkflowableUpdateService} from '../services/workflowable-update.service';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-event-dashboard',
	templateUrl: './event-dashboard.component.html',
	styleUrls: ['./event-dashboard.component.css'],
	providers: [DateUTCPipe],
	imports: [
		MatIcon,
		MatButton,
		IssueViewerComponent,
		WorkflowStatusComponent,
		DateUTCPipe,
		AuditTrailButtonComponent
	]
})
export class EventDashboardComponent {
	workflowableEntity = WorkflowableEntity;

	readonly scope = input.required<Scope>();
	//using a writable signal so it can be updated when the event is updated (e.g. after lock/unlock/remove/restore)
	//however, this gives the illusion that this will be propagated to the route resolver, which is not the case
	//the route resolver will still have the old scope reference until a new resolve process is triggered
	//this means other components in the same router-outlet will not see the updated scope until a new resolve is triggered, which can be confusing
	//maybe at some point we will have "withComponentModelBinding" instead of "withComponentInputBinding"
	readonly event = model.required<Event>();

	readonly dateDifferenceInDays = computed(() => {
		const date = this.event().date ?? this.event().expectedDate;
		return differenceInDays(date, new Date());
	});

	constructor(
		private eventService: EventService,
		private notificationService: NotificationService,
		private workflowableUpdateService: WorkflowableUpdateService,
		private dialog: MatDialog
	) {}

	remove() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: true})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.eventService.remove(this.event().scopePk, this.event().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: event => {
					if(event) {
						this.event.set(event);
						//used by the side menu to refresh the entities
						this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
						this.notificationService.showSuccess('Event removed');
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
						return this.eventService.restore(this.event().scopePk, this.event().pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: event => {
					if(event) {
						this.event.set(event);
						//used by the side menu to refresh the entities
						this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
						this.notificationService.showSuccess('Event restored');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	lock() {
		this.eventService.lock(this.event().scopePk, this.event().pk).subscribe({
			next: event => {
				this.event.set(event);
				//used by the side menu to refresh the entities
				this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
				this.notificationService.showSuccess('Event locked');
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	unlock() {
		this.eventService.unlock(this.event().scopePk, this.event().pk).subscribe({
			next: event => {
				this.event.set(event);
				//used by the side menu to refresh the entities
				this.workflowableUpdateService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
				this.notificationService.showSuccess('Event unlocked');
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	onWorkflowExecution(newEvent: Workflowable) {
		this.event.set(newEvent as Event);
	}
}
