import {booleanAttribute, Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {WorkflowActionDTO} from '@core/model/workflow-action-dto';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
import {WorkflowActionService} from '../services/workflow-action.service';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {WorkflowableDTO} from '@core/utilities/workflowable-dto';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {CRFChangeService} from '../services/crf-change.service';
import {ScopeDTO} from '@core/model/scope-dto';
import {EventDTO} from '@core/model/event-dto';
import {FormDTO} from '@core/model/form-dto';
import {FieldDTO} from '@core/model/field-dto';
import {WorkflowDTO} from '@core/model/workflow-dto';

@Component({
	selector: 'app-workflow-status',
	templateUrl: './workflow-status.component.html',
	styleUrls: ['./workflow-status.component.css'],
	imports: [
		MatIcon,
		MatFormFieldModule,
		MatButton,
		LocalizeMapPipe
	]
})
export class WorkflowStatusComponent implements OnChanges {
	workflowableEntity = WorkflowableEntity;

	@Input({required: true}) entity: WorkflowableEntity;
	@Input({required: true}) workflowable: WorkflowableDTO;

	//workflow status is provided only when the status already exists
	@Input() workflowStatus?: WorkflowStatusDTO;
	@Input() workflow?: WorkflowDTO;

	@Input({transform: booleanAttribute}) rough = false;

	@Output() actionResponse = new EventEmitter<WorkflowableDTO>();

	constructor(
		private workflowActionService: WorkflowActionService,
		private crfChangeService: CRFChangeService
	) { }

	ngOnChanges() {
		if(this.workflowStatus) {
			this.workflow = this.workflowStatus.workflow;
		}
	}

	executeWorkflowAction(action: WorkflowActionDTO) {
		let request;
		if(this.workflowStatus) {
			request = this.workflowActionService.executeActionOnWorkflowable(
				this.entity,
				this.workflowable,
				this.workflowStatus as WorkflowStatusDTO,
				action
			);
		}
		else {
			request = this.workflowActionService.createOnWorkflowable(
				this.entity,
				this.workflowable,
				action
			);
		}
		request.subscribe({
			next: updatedWorkflowable => {
				this.crfChangeService.emitUpdatedWorkflowable(this.entity, updatedWorkflowable);
				this.actionResponse.emit(updatedWorkflowable);
			}
			//error management is done in the service
		});
	}

	get icon(): string {
		return this.workflowStatus?.state.icon ?? this.workflow?.icon ?? 'manufacturing';
	}

	get style(): Record<string, string> {
		if(this.rough) {
			return {};
		}
		const color = this.workflowStatus?.state.color ?? '#000';
		return {
			backgroundColor: `${color + 15}`,
			border: `1px solid ${color}`
		};
	}

	get actions(): WorkflowActionDTO[] {
		if(this.workflowStatus) {
			return this.workflowStatus.state.possibleActions;
		}
		return [this.workflow?.actions.find(a => a.id === this.workflow?.actionId) as WorkflowActionDTO];
	}

	get displayActions(): boolean {
		switch(this.entity) {
			case WorkflowableEntity.SCOPE: {
				const scope = this.workflowable as ScopeDTO;
				return !scope.removed && !scope.locked;
			}
			case WorkflowableEntity.EVENT: {
				const event = this.workflowable as EventDTO;
				return !event.removed && !event.inRemoved && !event.locked && !event.inLocked;
			}
			case WorkflowableEntity.FORM: {
				const form = this.workflowable as FormDTO;
				return !form.removed && !form.inLocked;
			}
			case WorkflowableEntity.FIELD: {
				const field = this.workflowable as FieldDTO;
				return !field.inRemoved && !field.inLocked;
			}
			default: return false;
		}
	}
}
