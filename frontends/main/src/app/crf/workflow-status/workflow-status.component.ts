import {ChangeDetectionStrategy, booleanAttribute, Component, computed, input, output} from '@angular/core';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowStatus} from '@core/model/workflow-status';
import {WorkflowActionService} from '../services/workflow-action.service';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {Workflowable} from '@core/utilities/workflowable';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {CRFChangeService} from '../services/crf-change.service';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {Form} from '@core/model/form';
import {Field} from '@core/model/field';
import {Workflow} from '@core/model/workflow';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-workflow-status',
	templateUrl: './workflow-status.component.html',
	styleUrls: ['./workflow-status.component.css'],
	imports: [
		MatIcon,
		MatFormFieldModule,
		MatButton,
		LocalizeMapPipe,
		AuditTrailButtonComponent
	]
})
export class WorkflowStatusComponent {
	workflowableEntity = WorkflowableEntity;

	readonly entity = input.required<WorkflowableEntity>();
	readonly workflowable = input.required<Workflowable>();

	//workflow status is provided only when the status already exists
	readonly workflowStatus = input<WorkflowStatus>();
	readonly workflow = input<Workflow>();

	readonly rough = input(false, {transform: booleanAttribute});

	readonly actionResponse = output<Workflowable>();

	readonly effectiveWorkflow = computed(() => this.workflowStatus()?.workflow ?? this.workflow());

	constructor(
		private workflowActionService: WorkflowActionService,
		private crfChangeService: CRFChangeService
	) { }

	executeWorkflowAction(action: WorkflowAction) {
		let request;
		if(this.workflowStatus()) {
			request = this.workflowActionService.executeActionOnWorkflowable(
				this.entity(),
				this.workflowable(),
				this.workflowStatus() as WorkflowStatus,
				action
			);
		}
		else {
			request = this.workflowActionService.createOnWorkflowable(
				this.entity(),
				this.workflowable(),
				action
			);
		}
		request.subscribe({
			next: updatedWorkflowable => {
				this.crfChangeService.emitUpdatedWorkflowable(this.entity(), updatedWorkflowable);
				this.actionResponse.emit(updatedWorkflowable);
			}
			//error management is done in the service
		});
	}

	readonly icon = computed(() => this.workflowStatus()?.state.icon ?? this.effectiveWorkflow()?.icon ?? 'manufacturing');

	readonly style = computed(() => {
		if(this.rough()) {
			return {} as Record<string, string>;
		}
		const color = this.workflowStatus()?.state.color ?? '#000';
		return {
			backgroundColor: `${color + 15}`,
			border: `1px solid ${color}`
		};
	});

	readonly actions = computed<WorkflowAction[]>(() => {
		if(this.workflowStatus()) {
			return this.workflowStatus()!.state.possibleActions;
		}
		return [this.effectiveWorkflow()?.actions.find(a => a.id === this.effectiveWorkflow()?.actionId) as WorkflowAction];
	});

	readonly displayActions = computed<boolean>(() => {
		switch(this.entity()) {
			case WorkflowableEntity.SCOPE: {
				const scope = this.workflowable() as Scope;
				return !scope.removed && !scope.locked;
			}
			case WorkflowableEntity.EVENT: {
				const event = this.workflowable() as Event;
				return !event.removed && !event.inRemoved && !event.locked && !event.inLocked;
			}
			case WorkflowableEntity.FORM: {
				const form = this.workflowable() as Form;
				return !form.removed && !form.inLocked;
			}
			case WorkflowableEntity.FIELD: {
				const field = this.workflowable() as Field;
				return !field.inRemoved && !field.inLocked;
			}
			default: return false;
		}
	});
}
