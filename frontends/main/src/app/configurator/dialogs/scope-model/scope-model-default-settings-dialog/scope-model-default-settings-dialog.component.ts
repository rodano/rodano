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
import {BaseDialogComponent} from '../../base-dialog.component';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel;
}

@Component({
	selector: 'app-scope-model-default-settings-dialog',
	standalone: true,
	templateUrl: './scope-model-default-settings-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatSelectModule]
})
export class ScopeModelDefaultSettingsDialogComponent extends BaseDialogComponent<DialogData> implements OnInit {
	form: FormGroup;
	saving = false;
	availableProfiles: Profile[] = [];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		private profileManager: ProfileManagerService,
		dialogRef: MatDialogRef<ScopeModelDefaultSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: DialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.availableProfiles = this.profileManager.getAll();
		this.form = this.fb.group({
			defaultProfileId: [this.data.scopeModel.defaultProfileId || '']
		});
	}

	onSave(): void {
		this.dialogRef.close({
			defaultProfileId: this.form.getRawValue().defaultProfileId || null
		});
	}
}
