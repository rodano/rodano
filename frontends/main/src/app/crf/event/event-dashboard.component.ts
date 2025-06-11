import {Component, Input, OnChanges} from '@angular/core';
import {EventDTO} from '@core/model/event-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {EventService} from '@core/services/event.service';
import {differenceInDays} from 'date-fns';
import {WorkflowStatusComponent} from '../workflow-status/workflow-status.component';
import {MatIcon} from '@angular/material/icon';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {IssueViewerComponent} from '../issue-viewer/issue-viewer.component';
import {NotificationService} from 'src/app/services/notification.service';
import {MatButton} from '@angular/material/button';
import {DeleteRestoreComponent} from '../dialogs/delete-restore/delete-restore.component';
import {MatDialog} from '@angular/material/dialog';
import {of, switchMap} from 'rxjs';
import {CRFChangeService} from '../services/crf-change.service';

@Component({
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
export class EventDashboardComponent implements OnChanges {
	workflowableEntity = WorkflowableEntity;

	@Input() scope: ScopeDTO;
	@Input() event: EventDTO;

	dateDifferenceInDays: number;

	constructor(
		private eventService: EventService,
		private notificationService: NotificationService,
		private crfChangeService: CRFChangeService,
		private dialog: MatDialog
	) {}

	ngOnChanges() {
		const date = this.event.date ?? this.event.expectedDate;
		this.dateDifferenceInDays = differenceInDays(date, new Date());
	}

	remove() {
		return this.dialog
			.open(DeleteRestoreComponent, {data: true})
			.afterClosed()
			.pipe(
				switchMap((rationale?: string) => {
					if(rationale) {
						return this.eventService.remove(this.event.scopePk, this.event.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: event => {
					if(event) {
						this.event = event;
						//used by the side menu to refresh the entities
						this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
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
						return this.eventService.restore(this.event.scopePk, this.event.pk, rationale);
					}
					return of(undefined);
				})
			)
			.subscribe({
				next: event => {
					if(event) {
						this.event = event;
						//used by the side menu to refresh the entities
						this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
						this.notificationService.showSuccess('Event restored');
					}
				},
				error: response => {
					this.notificationService.showError(response.error.message);
				}
			});
	}

	lock() {
		this.eventService.lock(this.event.scopePk, this.event.pk).subscribe({
			next: event => {
				this.event = event;
				//used by the side menu to refresh the entities
				this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
				this.notificationService.showSuccess('Event locked');
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	unlock() {
		this.eventService.unlock(this.event.scopePk, this.event.pk).subscribe({
			next: event => {
				this.event = event;
				//used by the side menu to refresh the entities
				this.crfChangeService.emitUpdatedWorkflowable(WorkflowableEntity.EVENT, event);
				this.notificationService.showSuccess('Event unlocked');
			},
			error: response => {
				this.notificationService.showError(response.error.message);
			}
		});
	}

	onWorkflowExecution(newEvent: WorkflowableDTO) {
		this.event = newEvent as EventDTO;
	}
}
