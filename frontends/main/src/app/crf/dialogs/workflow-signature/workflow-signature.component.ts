import {Component, inject} from '@angular/core';
import {Validators, ReactiveFormsModule, FormGroup, FormControl} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowUpdate} from '@core/model/workflow-update';
import {WorkflowStatus} from '@core/model/workflow-status';
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
	data = inject<{workflowableName?: string; action: WorkflowAction; workflow?: WorkflowStatus}>(MAT_DIALOG_DATA);

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

	getResponse(): WorkflowUpdate {
		return {
			workflowId: this.data.action.workflowId,
			actionId: this.data.action.id,
			rationale: undefined,
			email: this.passwordForm.controls.email.value,
			password: this.passwordForm.controls.password.value
		};
	}
}
