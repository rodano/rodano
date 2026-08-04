import {Service, inject} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable, EMPTY, of} from 'rxjs';
import {catchError, mergeMap, switchMap, tap} from 'rxjs/operators';
import {WorkflowAction} from '@core/model/workflow-action';
import {Field} from '@core/model/field';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {WorkflowUpdate} from '@core/model/workflow-update';
import {WorkflowStatus} from '@core/model/workflow-status';
import {WorkflowStatusService} from '@core/services/workflow-status.service';
import {CapitalizeFirstPipe} from '../../pipes/capitalize-first.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {NotificationService} from '../../services/notification.service';
import {WorkflowRationaleComponent} from '../dialogs/workflow-rationale/workflow-rationale.component';
import {WorkflowSignatureComponent} from '../dialogs/workflow-signature/workflow-signature.component';
import {Workflowable} from '@core/utilities/workflowable';
import {Form} from '@core/model/form';
import {WorkflowableEntity} from '@core/model/workflowable-entity';

@Service()
export class WorkflowActionService {
	private capitalizeFirstPipe: CapitalizeFirstPipe;
	private localizeMapPipe: LocalizeMapPipe;

	private readonly workflowStatusService = inject(WorkflowStatusService);
	private readonly notificationService = inject(NotificationService);
	private readonly dialog = inject(MatDialog);

	constructor() {
		this.capitalizeFirstPipe = new CapitalizeFirstPipe();
		this.localizeMapPipe = new LocalizeMapPipe();
	}

	private openWorkflowRationaleDialog(
		action: WorkflowAction,
		workflowStatus?: WorkflowStatus
	): Observable<WorkflowUpdate | undefined> {
		const data = {
			workflow: workflowStatus,
			action
		};
		return this.dialog
			.open<WorkflowRationaleComponent, any, WorkflowUpdate>(WorkflowRationaleComponent, {data})
			.afterClosed();
	}

	private openWorkflowSignatureDialog(
		action: WorkflowAction,
		workflowStatus: WorkflowStatus
	): Observable<WorkflowUpdate | undefined> {
		const data = {
			workflow: workflowStatus,
			action
		};
		return this.dialog
			.open<WorkflowSignatureComponent, any, WorkflowUpdate>(WorkflowSignatureComponent, {data})
			.afterClosed();
	}

	private openWorkflowActionDialog(action: WorkflowAction, workflowStatus?: WorkflowStatus): Observable<WorkflowUpdate | undefined> {
		if(action.documentable) {
			return this.openWorkflowRationaleDialog(action, workflowStatus);
		}
		else if(action.requireSignature && workflowStatus) {
			return this.openWorkflowSignatureDialog(action, workflowStatus);
		}
		throw new Error(`The action ${action.id} is neither documentable, nor requires a signature`);
	}

	private buildWorkflowUpdate(action: WorkflowAction, workflowStatus?: WorkflowStatus): Observable<WorkflowUpdate | undefined> {
		if(!action.documentable && !action.requireSignature) {
			return of({workflowId: action.workflowId, actionId: action.id});
		}
		return this.openWorkflowActionDialog(action, workflowStatus);
	}

	canPerformAction(entity: WorkflowableEntity, workflowable: Workflowable): boolean {
		switch(entity) {
			case WorkflowableEntity.SCOPE: {
				const scope = workflowable as Scope;
				return !scope.removed && !scope.locked;
			}
			case WorkflowableEntity.EVENT: {
				const event = workflowable as Event;
				return !event.removed && !event.inRemoved && !event.locked && !event.inLocked;
			}
			case WorkflowableEntity.FORM: {
				const form = workflowable as Form;
				return !form.removed && !form.inLocked;
			}
			case WorkflowableEntity.FIELD: {
				const field = workflowable as Field;
				return !field.inRemoved && !field.inLocked;
			}
			default: return false;
		}
	}

	createOnScope(
		scope: Scope,
		action: WorkflowAction
	): Observable<Scope> {
		return this.createOnWorkflowable(WorkflowableEntity.SCOPE, scope, action) as Observable<Scope>;
	}

	createOnEvent(
		event: Event,
		action: WorkflowAction
	): Observable<Event> {
		return this.createOnWorkflowable(WorkflowableEntity.EVENT, event, action) as Observable<Event>;
	}

	createOnField(
		field: Field,
		action: WorkflowAction
	): Observable<Field> {
		return this.createOnWorkflowable(WorkflowableEntity.FIELD, field, action) as Observable<Field>;
	}

	createOnForm(
		form: Form,
		action: WorkflowAction
	): Observable<Form> {
		return this.createOnWorkflowable(WorkflowableEntity.FORM, form, action) as Observable<Form>;
	}

	createOnWorkflowable(
		entity: WorkflowableEntity,
		workflowable: Workflowable,
		action: WorkflowAction
	): Observable<Workflowable> {
		const workflowLabel = this.capitalizeFirstPipe.transform(action.workflowId);
		return this.buildWorkflowUpdate(action).pipe(
			mergeMap(workflowUpdate => {
				if(workflowUpdate) {
					switch(entity) {
						case WorkflowableEntity.SCOPE:
							return this.workflowStatusService.createOnScope(workflowable as Scope, workflowUpdate);
						case WorkflowableEntity.EVENT:
							return this.workflowStatusService.createOnEvent(workflowable as Event, workflowUpdate);
						case WorkflowableEntity.FORM:
							return this.workflowStatusService.createOnForm(workflowable as Form, workflowUpdate);
						case WorkflowableEntity.FIELD:
							return this.workflowStatusService.createOnField(workflowable as Field, workflowUpdate);
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
		scope: Scope,
		workflowStatus: WorkflowStatus,
		action: WorkflowAction
	): Observable<Scope> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.SCOPE, scope, workflowStatus, action) as Observable<Scope>;
	}

	executeActionOnEvent(
		event: Event,
		workflowStatus: WorkflowStatus,
		action: WorkflowAction
	): Observable<Event> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FIELD, event, workflowStatus, action) as Observable<Event>;
	}

	executeActionOnField(
		field: Field,
		workflowStatus: WorkflowStatus,
		action: WorkflowAction
	): Observable<Field> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FIELD, field, workflowStatus, action) as Observable<Field>;
	}

	executeActionOnForm(
		form: Form,
		workflowStatus: WorkflowStatus,
		action: WorkflowAction
	): Observable<Form> {
		return this.executeActionOnWorkflowable(WorkflowableEntity.FORM, form, workflowStatus, action) as Observable<Form>;
	}

	executeActionOnWorkflowable(
		entity: WorkflowableEntity,
		workflowable: Workflowable,
		workflowStatus: WorkflowStatus,
		action: WorkflowAction
	): Observable<Workflowable> {
		const actionLabel = this.capitalizeFirstPipe.transform(this.localizeMapPipe.transform(action.shortname));
		return this.buildWorkflowUpdate(action, workflowStatus).pipe(
			switchMap(workflowUpdate => {
				if(workflowUpdate) {
					switch(entity) {
						case WorkflowableEntity.SCOPE:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnScope(workflowable as Scope, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnScope(workflowable as Scope, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.EVENT:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnEvent(workflowable as Event, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnEvent(workflowable as Event, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.FORM:
							if(workflowStatus.workflow.aggregator) {
								return this.workflowStatusService.executeAggregateActionOnForm(workflowable as Form, workflowStatus.workflow.id, workflowUpdate);
							}
							return this.workflowStatusService.executeActionOnForm(workflowable as Form, workflowStatus.pk, workflowUpdate);
						case WorkflowableEntity.FIELD:
							return this.workflowStatusService.executeActionOnField(workflowable as Field, workflowStatus.pk, workflowUpdate);
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
