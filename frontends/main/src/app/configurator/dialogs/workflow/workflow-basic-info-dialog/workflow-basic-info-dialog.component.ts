import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ProjectLanguage} from '@core/model/project-language';
import {Workflow} from '@core/model/workflow';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';

interface WorkflowBasicInfoDialogData {
	projectId: string;
	workflow: Workflow | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-workflow-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatCheckboxModule,
		MatSelectModule,
		MatTabsModule
	]
})
export class WorkflowBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowBasicInfoDialogData,
		private workflowManager: WorkflowManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.workflow;
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
			const wf = this.data.workflow;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [wf?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [wf?.longname?.[lang.languageCode] || ''],
				description: [wf?.description?.[lang.languageCode] || ''],
				message: [wf?.message?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const wf = this.data.workflow;
		this.form = this.fb.group({
			id: [wf?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			order: [wf?.order || null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentWorkflowId = this.data.workflow?.workflowId;
		return this.workflowManager.getAll().some(wf =>
			wf.id.toUpperCase() === code.toUpperCase() && wf.workflowId !== currentWorkflowId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A workflow with code "${code}" already exists`, 'Close', {duration: 3000});
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description
		});
	}
}
