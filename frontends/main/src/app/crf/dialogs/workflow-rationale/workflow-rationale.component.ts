import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {WorkflowUpdateDTO} from '@core/model/workflow-update-dto';
import {WorkflowActionDTO} from '@core/model/workflow-action-dto';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
import {CapitalizeFirstPipe} from '../../../pipes/capitalize-first.pipe';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
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
		LocalizeMapPipe,
		CapitalizeFirstPipe
	]
})
export class WorkflowRationaleComponent {
	rationale = new FormControl<string>('', {nonNullable: true, validators: [Validators.required]});
	otherOption = false;

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {action: WorkflowActionDTO; workflow?: WorkflowStatusDTO}
	) { }

	getResponse(): WorkflowUpdateDTO {
		return {
			workflowId: this.data.action.workflowId,
			actionId: this.data.action.id,
			rationale: this.rationale.value,
			email: undefined,
			password: undefined
		};
	}
}
