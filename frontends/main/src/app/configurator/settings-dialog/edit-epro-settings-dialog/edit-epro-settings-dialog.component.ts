import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {Profile} from '@core/model/profile';
import {MatIconModule} from '@angular/material/icon';

export interface EproSettingsDialogData {
	eproEnabled: boolean;
	eproProfileId: string | null;
	availableProfiles: Profile[];
}

@Component({
	selector: 'app-edit-epro-settings-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatSlideToggleModule,
		MatIconModule
	],
	templateUrl: './edit-epro-settings-dialog.component.html',
	styleUrls: ['./edit-epro-settings-dialog.component.css']
})
export class EditEproSettingsDialogComponent implements OnInit {
	form: FormGroup;
	hasProfiles = false;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EditEproSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EproSettingsDialogData
	) {
		this.form = this.fb.group({
			eproEnabled: [data.eproEnabled],
			eproProfileId: [data.eproProfileId]
		});
	}

	ngOnInit(): void {
		this.hasProfiles = this.data.availableProfiles && this.data.availableProfiles.length > 0;
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
