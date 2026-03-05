import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {BaseDialogComponent} from '../../base-dialog.component';

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
	templateUrl: './project-settings-client-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule]
})
export class ProjectSettingsClientInfoDialogComponent extends BaseDialogComponent<ClientInfoDialogData> {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<ProjectSettingsClientInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ClientInfoDialogData
	) {
		super(dialogRef, data);
		this.form = this.fb.group({
			clientName: [data.clientName],
			clientEmail: [data.clientEmail, [Validators.email]],
			protocolNo: [data.protocolNo],
			versionNumber: [data.versionNumber],
			versionDate: [data.versionDate]
		});
	}

	onSave(): void {
		if(this.form.valid) {
			this.dialogRef.close(this.form.value);
		}
	}
}
