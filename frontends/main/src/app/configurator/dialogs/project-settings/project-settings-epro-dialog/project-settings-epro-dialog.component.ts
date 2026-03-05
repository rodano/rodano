import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {Profile} from '@core/model/profile';
import {MatSelectModule} from '@angular/material/select';
import {ProfileManagerService} from '../../../services/manager/profile-manager.service';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface EproSettingsDialogData {
	eproEnabled: boolean;
	eproProfileId: string | null;
	availableProfiles: Profile[];
}

@Component({
	selector: 'app-project-settings-epro-dialog',
	standalone: true,
	templateUrl: './project-settings-epro-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatSlideToggleModule, MatSelectModule]
})
export class ProjectSettingsEproDialogComponent extends BaseDialogComponent<EproSettingsDialogData> {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private profileManager: ProfileManagerService,
		private languageService: LanguageService,
		dialogRef: MatDialogRef<ProjectSettingsEproDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: EproSettingsDialogData
	) {
		super(dialogRef, data);
		this.form = this.fb.group({
			eproEnabled: [data.eproEnabled],
			eproProfileId: [data.eproProfileId]
		});
	}

	onSave(): void {
		if(this.form.valid) {
			this.dialogRef.close(this.form.value);
		}
	}

	getProfileLabel(profileId: string): string {
		return this.languageService.getLabelById(profileId, id => this.profileManager.getById(id));
	}
}
