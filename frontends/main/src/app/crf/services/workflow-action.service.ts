import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable, EMPTY, of} from 'rxjs';
import {catchError, mergeMap, switchMap, tap} from 'rxjs/operators';
import {WorkflowActionDTO} from '@core/model/workflow-action-dto';
import {FieldDTO} from '@core/model/field-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {EventDTO} from '@core/model/event-dto';
import {WorkflowUpdateDTO} from '@core/model/workflow-update-dto';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {CapitalizeFirstPipe} from 'src/app/pipes/capitalize-first.pipe';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {NotificationService} from 'src/app/services/notification.service';
import {WorkflowRationaleComponent} from '../dialogs/workflow-rationale/workflow-rationale.component';
import {WorkflowSignatureComponent} from '../dialogs/workflow-signature/workflow-signature.component';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';
import {FormDTO} from '@core/model/form-dto';
import {WorkflowableEntity} from '@core/model/workflowable-entity';

@Injectable({
	providedIn: 'root'
})
export class WorkflowActionService {
	private capitalizeFirstPipe: CapitalizeFirstPipe;
	private localizeMapPipe: LocalizeMapPipe;

	constructor(
		private workflowStatusService: WorkflowStatusService,
		private notificationService: NotificationService,
		private dialog: MatDialog
	) {
		this.capitalizeFirstPipe = new CapitalizeFirstPipe();
		this.localizeMapPipe = new LocalizeMapPipe();
	}

	private openWorkflowRationaleDialog(
		action: WorkflowActionDTO,
		workflowStatus?: WorkflowStatusDTO
	): Observable<WorkflowUpdateDTO | undefined> {
		const data = {
			workflow: workflowStatus,
			action
		};
		return this.dialog
			.open<WorkflowRationaleComponent, any, WorkflowUpdateDTO>(WorkflowRationaleComponent, {data})
			.afterClosed();
	}

	private openWorkflowSignatureDialog(
		action: WorkflowActionDTO,
		workflowStatus: WorkflowStatusDTO
	): Observable<WorkflowUpdateDTO | undefined> {
		const data = {
			workflow: workflowStatus,
			action
		};
		return this.dialog
			.open<WorkflowSignatureComponent, any, WorkflowUpdateDTO>(WorkflowSignatureComponent, {data})
			.afterClosed();
	}

	private openWorkflowActionDialog(action: WorkflowActionDTO, workflowStatus?: WorkflowStatusDTO): Observable<WorkflowUpdateDTO | undefined> {
		if(action.documentable) {
			return this.openWorkflowRationaleDialog(action, workflowStatus);
		}
		else if(action.requireSignature && workflowStatus) {
			return this.openWorkflowSignatureDialog(action, workflowStatus);
		}
		throw new Error(`The action ${action.id} is neither documentable, nor requires a signature`);
	}

	private buildWorkflowUpdate(action: WorkflowActionDTO, workflowStatus?: WorkflowStatusDTO): Observable<WorkflowUpdateDTO | undefined> {
		if(!action.documentable && !action.requireSignature) {
			return of({workflowId: action.workflowId, actionId: action.id});
		}
		return this.openWorkflowActionDialog(action, workflowStatus);
	}

	createOnScope(
		scope: ScopeDTO,
		action: WorkflowActionDTO
	): Observable<ScopeDTO> {
		return this.createOnWorkflowable(WorkflowableEntity.SCOPE, scope, action) as Observable<ScopeDTO>;
	}

	createOnEvent(
		event: EventDTO,
		action: WorkflowActionDTO
	): Observable<EventDTO> {
		return this.createOnWorkflowable(WorkflowableEntity.EVENT, event, action) as Observable<EventDTO>;
	}

	createOnField(
		field: FieldDTO,
		action: WorkflowActionDTO
	): Observable<FieldDTO> {
		return this.createOnWorkflowable(WorkflowableEntity.FIELD, field, action) as Observable<FieldDTO>;
	}

	createOnForm(
		form: FormDTO,
		action: WorkflowActionDTO
	): Observable<FormDTO> {
		return this.createOnWorkflowable(WorkflowableEntity.FORM, form, action) as Observable<FormDTO>;
	}

	createOnWorkflowable(
		entity: WorkflowableEntity,
		workflowable: WorkflowableDTO,
		action: WorkflowActionDTO
	): Observable<WorkflowableDTO> {
		const workflowLabel = this.capitalizeFirstPipe.transform(action.workflowId);
		return this.buildWorkflowUpdate(action).pipe(
			mergeMap(workflowUpdate => {
				if(workflowUpdate) {
					switch(entity) {
						case WorkflowableEntity.SCOPE:
							return this.workflowStatusService.createOnScope(workflowable as ScopeDTO, workflowUpdate);
						case WorkflowableEntity.EVENT:
							return this.workflowStatusService.createOnEvent(workflowable as EventDTO, workflowUpdate);
						case WorkflowableEntity.FORM:
							return this.workflowStatusService.createOnForm(workflowable as FormDTO, workflowUpdate);
						case WorkflowableEntity.FIELD:
							return this.workflowStatusService.createOnField(workflowable as FieldDTO, workflowUpdate);
						default:
							throw new Error(`Unable to create workflow status for an object of type ${entity}`);
					}
				}
				else {
					return EMPTY;
				}
			}),
			tap(() => this.notificationService.showSuccess(`${workflowLabel} created`)),
			catchError(response => {
				this.notificationService.showError(`Unable to create workflow ${workflowLabel}: ${response.error.message}`);
				throw response.error;
			})
		);
	}

	executeActionOnScope(
		scope: ScopeDTO,
		workflowStatus: WorkflowStatusDTO,
		action: WorkflowActionDTO
	): Observable<ScopeDTO> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.SCOPE, scope, workflowStatus, action) as Observable<ScopeDTO>;
	}

	executeActionOnEvent(
		event: EventDTO,
		workflowStatus: WorkflowStatusDTO,
		action: WorkflowActionDTO
	): Observable<EventDTO> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FIELD, event, workflowStatus, action) as Observable<EventDTO>;
	}

	executeActionOnField(
		field: FieldDTO,
		workflowStatus: WorkflowStatusDTO,
		action: WorkflowActionDTO
	): Observable<FieldDTO> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FIELD, field, workflowStatus, action) as Observable<FieldDTO>;
	}

	executeActionOnForm(
		form: FormDTO,
		workflowStatus: WorkflowStatusDTO,
		action: WorkflowActionDTO
	): Observable<FormDTO> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FORM, form, workflowStatus, action) as Observable<FormDTO>;
	}

	executeActionOnWorkflowable(
		entity: WorkflowableEntity,
		workflowable: WorkflowableDTO,
		workflowStatus: WorkflowStatusDTO,
		action: WorkflowActionDTO
	): Observable<WorkflowableDTO> {
		const actionLabel = this.capitalizeFirstPipe.transform(this.localizeMapPipe.transform(action.shortname));
		return this.buildWorkflowUpdate(action, workflowStatus).pipe(
			switchMap(workflowUpdate => {
				if(workflowUpdate) {
					switch(entity) {
						case WorkflowableEntity.SCOPE:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnScope(workflowable as ScopeDTO, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnScope(workflowable as ScopeDTO, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.EVENT:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnEvent(workflowable as EventDTO, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnEvent(workflowable as EventDTO, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.FORM:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnForm(workflowable as FormDTO, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnForm(workflowable as FormDTO, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.FIELD:
							return this.workflowStatusService.executeActionOnField(workflowable as FieldDTO, workflowStatus.pk, workflowUpdate);
						default:
							throw new Error(`Unable to execute workflow action for an object of type ${entity}`);
					}
				}
				else {
					return EMPTY;
				}
			}),
			tap(() => this.notificationService.showSuccess(`${actionLabel} performed`)),
			catchError(response => {
				this.notificationService.showError(`Unable to perform action ${actionLabel}: ${response.error.message}`);
				throw response.error;
			})
		);
	}
}
