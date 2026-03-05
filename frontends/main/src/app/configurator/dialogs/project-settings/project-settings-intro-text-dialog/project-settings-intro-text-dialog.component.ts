import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {WysiwygEditorComponent} from '../../../shared/wysiwyg-editor/wysiwyg-editor.component';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface IntroductionTextDialogData {
	introductionText?: string;
}

@Component({
	selector: 'app-edit-introduction-text-dialog',
	standalone: true,
	templateUrl: './project-settings-intro-text-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, WysiwygEditorComponent]
})
export class ProjectSettingsIntroTextDialogComponent extends BaseDialogComponent<IntroductionTextDialogData> implements OnInit {
	introductionTextControl = new FormControl('');

	constructor(
		dialogRef: MatDialogRef<ProjectSettingsIntroTextDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: IntroductionTextDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.introductionTextControl.setValue(this.data.introductionText || '');
	}

	onSave(): void {
		this.dialogRef.close({
			introductionText: this.introductionTextControl.value || null
		});
	}
}
