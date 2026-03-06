import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {BaseDialogComponent} from '../../base-dialog.component';
import { MatCheckbox } from '@angular/material/checkbox';

export interface EmailSettingsDialogData {
	email: string | null;
	smtpTls: boolean;
}

@Component({
	selector: 'app-edit-email-settings-dialog',
	standalone: true,
	templateUrl: './project-settings-email-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatSlideToggleModule, MatCheckbox]
})
export class ProjectSettingsEmailDialogComponent extends BaseDialogComponent<EmailSettingsDialogData> {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<ProjectSettingsEmailDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: EmailSettingsDialogData
	) {
		super(dialogRef, data);
		this.form = this.fb.group({
			email: [data.email, [Validators.email]],
			smtpTls: [data.smtpTls]
		});
	}

	onSave(): void {
		if(this.form.valid) {
			this.dialogRef.close(this.form.value);
		}
	}
}
