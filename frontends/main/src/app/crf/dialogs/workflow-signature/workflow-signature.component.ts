import {Component, Inject} from '@angular/core';
import {Validators, ReactiveFormsModule, FormGroup, FormControl} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {WorkflowActionDTO} from '@core/model/workflow-action-dto';
import {ScopeDTO} from '@core/model/scope-dto';
import {EventDTO} from '@core/model/event-dto';
import {WorkflowUpdateDTO} from '@core/model/workflow-update-dto';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';

@Component({
	selector: 'app-workflow-signature',
	templateUrl: './workflow-signature.component.html',
	styleUrls: ['./workflow-signature.component.css'],
	imports: [
		MatDialogModule,
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton,
		LocalizeMapPipe
	]
})
export class WorkflowSignatureComponent {
	passwordForm = new FormGroup({
		email: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		}),
		password: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		})
	});

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {action: WorkflowActionDTO; workflow: WorkflowStatusDTO; object: ScopeDTO | EventDTO}
	) { }

	getResponse(): WorkflowUpdateDTO {
		return {
			workflowId: this.data.action.workflowId,
			actionId: this.data.action.id,
			rationale: undefined,
			email: this.passwordForm.controls.email.value,
			password: this.passwordForm.controls.password.value
		};
	}
}
