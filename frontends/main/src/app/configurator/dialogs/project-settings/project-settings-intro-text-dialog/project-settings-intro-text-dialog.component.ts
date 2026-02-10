import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';

export interface IntroductionTextDialogData {
	introductionText?: string;
}

@Component({
	selector: 'app-edit-introduction-text-dialog',
	standalone: true,
	templateUrl: './project-settings-intro-text-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatIconModule,
		MatTabsModule
	]
})
export class ProjectSettingsIntroTextDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ProjectSettingsIntroTextDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: IntroductionTextDialogData
	) {
		this.form = this.fb.group({
			introductionText: ['']
		});
	}

	ngOnInit(): void {
		this.form.patchValue({
			introductionText: this.data.introductionText || ''
		});
	}

	onSave(): void {
		if(this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}

		this.dialogRef.close(this.form.value);
	}

	onCancel(): void {
		this.dialogRef.close();
	}
}
