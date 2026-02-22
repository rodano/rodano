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
import {ProfileManagerService} from '../../../services/manager/profile-manager.service';
import {LanguageService} from '../../../services/language.service';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel;
}

@Component({
	selector: 'app-scope-model-default-settings-dialog',
	standalone: true,
	templateUrl: './scope-model-default-settings-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
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
		private profileManager: ProfileManagerService,
		private languageService: LanguageService,
		private dialogRef: MatDialogRef<ScopeModelDefaultSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DialogData,
		private snackBar: MatSnackBar
	) {
		this.form = this.fb.group({
			defaultProfileId: ['']
		});
	}

	ngOnInit(): void {
		this.availableProfiles = this.profileManager.getAll();
		this.populateForm();
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
			defaultProfileId: formValue.defaultProfileId || null
		};

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	getProfileLabel(profile: Profile): string {
		const name = this.languageService.getDefaultTranslation(profile.shortname) || profile.id;
		return `${name} (${profile.id})`;
	}
}
