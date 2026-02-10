import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';

export interface EmailSettingsDialogData {
	email: string | null;
	smtpTls: boolean;
}

@Component({
	selector: 'app-edit-email-settings-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatSlideToggleModule
	],
	templateUrl: './project-settings-email-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class ProjectSettingsEmailDialogComponent {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProjectSettingsEmailDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EmailSettingsDialogData
	) {
		this.form = this.fb.group({
			email: [data.email, [Validators.email]],
			smtpTls: [data.smtpTls]
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	onSave(): void {
		if(this.form.valid) {
			this.dialogRef.close(this.form.value);
		}
	}
}
