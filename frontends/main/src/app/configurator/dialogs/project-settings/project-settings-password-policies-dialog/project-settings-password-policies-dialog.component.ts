import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface PasswordPoliciesDialogData {
	passwordStrong: boolean;
	passwordLength: number | null;
	passwordValidityDuration: number | null;
	passwordUnique: boolean;
}

@Component({
	selector: 'app-edit-password-policies-dialog',
	standalone: true,
	templateUrl: './project-settings-password-policies-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatSlideToggleModule]
})
export class ProjectSettingsPasswordPoliciesDialogComponent extends BaseDialogComponent<PasswordPoliciesDialogData> {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<ProjectSettingsPasswordPoliciesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: PasswordPoliciesDialogData
	) {
		super(dialogRef, data);
		this.form = this.fb.group({
			passwordStrong: [data.passwordStrong],
			passwordLength: [data.passwordLength, [Validators.min(1), Validators.max(128)]],
			passwordValidityDuration: [data.passwordValidityDuration, [Validators.min(0), Validators.max(3650)]],
			passwordUnique: [data.passwordUnique]
		});
	}

	onSave(): void {
		if(this.form.valid) {
			this.dialogRef.close(this.form.value);
		}
	}
}
