import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';

export interface ClientInfoDialogData {
	clientName: string | null;
	clientEmail: string | null;
	protocolNo: string | null;
	versionNumber: string | null;
	versionDate: string | null;
}

@Component({
	selector: 'app-edit-client-info-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule
	],
	templateUrl: './project-settings-client-info-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class ProjectSettingsClientInfoDialogComponent {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProjectSettingsClientInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ClientInfoDialogData
	) {
		this.form = this.fb.group({
			clientName: [data.clientName],
			clientEmail: [data.clientEmail, [Validators.email]],
			protocolNo: [data.protocolNo],
			versionNumber: [data.versionNumber],
			versionDate: [data.versionDate]
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
