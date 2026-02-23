import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSelectModule} from '@angular/material/select';
import {ProjectLanguage} from '@core/model/project-language';
import {MatSnackBar} from '@angular/material/snack-bar';
import {WorkflowAction} from '@core/model/workflow-action';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

export interface WorkflowActionBasicInfoDialogData {
	projectId: string;
	workflowId: string;
	workflowAction: WorkflowAction | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-action-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-action-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatCheckboxModule,
		MatTabsModule,
		MatSelectModule
	]
})
export class WorkflowActionBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowActionBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowActionBasicInfoDialogData,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.workflowAction;
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			const wfa = this.data.workflowAction;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [wfa?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [wfa?.longname?.[lang.languageCode] || ''],
				description: [wfa?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const wfa = this.data.workflowAction;
		this.form = this.fb.group({
			id: [wfa?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowActionId: [wfa?.workflowActionId || null],
			icon: [wfa?.icon || null]
		});
	}

	get iconPreview(): string {
		return this.form.get('icon')?.value?.trim() || '';
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			shortname,
			longname,
			description,
			...this.form.value
		});
	}
}
