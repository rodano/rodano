import {Component, computed, inject, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {WorkflowUpdate} from '@core/model/workflow-update';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowStatus} from '@core/model/workflow-status';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatRadioButton, MatRadioGroup} from '@angular/material/radio';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

@Component({
	selector: 'app-workflow-rationale',
	templateUrl: 'workflow-rationale.component.html',
	styleUrls: ['./workflow-rationale.component.css'],
	imports: [
		MatDialogModule,
		ReactiveFormsModule,
		MatLabel,
		MatError,
		MatRadioGroup,
		MatRadioButton,
		MatFormField,
		MatInput,
		MatButton,
		MatIcon,
		LocalizeMapPipe
	]
})
export class WorkflowRationaleComponent {
	data = inject<{workflowableName?: string; action: WorkflowAction; workflow?: WorkflowStatus}>(MAT_DIALOG_DATA);
	rationale = new FormControl<string>('', {nonNullable: true, validators: [Validators.required]});

	readonly otherOption = signal(false);
	readonly icon = computed(() => this.data.workflow?.state.icon ?? this.data.workflow?.workflow.icon ?? 'manufacturing');
	readonly color = computed(() => this.data.workflow?.state.color ?? '#000');

	getResponse(): WorkflowUpdate {
		return {
			workflowId: this.data.action.workflowId,
			actionId: this.data.action.id,
			rationale: this.rationale.value,
			email: undefined,
			password: undefined
		};
	}
}
