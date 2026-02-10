import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {Profile} from '@core/model/profile';
import {MatSelectModule} from '@angular/material/select';

export interface EproSettingsDialogData {
	eproEnabled: boolean;
	eproProfileId: string | null;
	availableProfiles: Profile[];
}

@Component({
	selector: 'app-project-settings-epro-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatSlideToggleModule,
		MatSelectModule
	],
	templateUrl: './project-settings-epro-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class ProjectSettingsEproDialogComponent {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProjectSettingsEproDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EproSettingsDialogData
	) {
		this.form = this.fb.group({
			eproEnabled: [data.eproEnabled],
			eproProfileId: [data.eproProfileId]
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
