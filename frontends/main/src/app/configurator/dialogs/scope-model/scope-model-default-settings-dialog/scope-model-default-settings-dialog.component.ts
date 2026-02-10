import {ScopeModel} from '@core/model/scope-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {Profile} from '@core/model/profile';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel;
}

@Component({
	selector: 'app-scope-model-default-settings-dialog',
	standalone: true,
	templateUrl: './scope-model-default-settings-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatSelectModule
	]
})
export class ScopeModelDefaultSettingsDialogComponent implements OnInit {
	form: FormGroup;
	saving = false;
	availableProfiles: Profile[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ScopeModelDefaultSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DialogData,
		private snackBar: MatSnackBar
	) {
		this.form = this.fb.group({
			defaultProfileId: ['']
		});
	}

	ngOnInit(): void {
		this.loadAvailableProfiles();
		this.populateForm();
	}

	loadAvailableProfiles(): void {
		//TODO: Replace with actual service call when backend endpoint is ready
	}

	populateForm(): void {
		this.form.patchValue({
			defaultProfileId: this.data.scopeModel.defaultProfileId || ''
		});
	}

	onSave(): void {
		if(this.form.invalid) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();

		const result = {
			defaultProfileId: formValue.defaultProfileId || ''
		};

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
