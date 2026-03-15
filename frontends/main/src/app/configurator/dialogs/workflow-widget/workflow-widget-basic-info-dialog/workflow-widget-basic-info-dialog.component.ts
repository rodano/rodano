import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {WorkflowWidgetManagerService} from '../../../services/manager/workflow-widget-manager.service';
import {MatSelectModule} from '@angular/material/select';

export interface WorkflowWidgetBasicInfoDialogData {
	projectId: string;
	workflowWidget: WorkflowWidgetConfig | null;
	languages: ProjectLanguage[];
}

interface EntityOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-workflow-widget-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-widget-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatCheckboxModule, MatSelectModule]
})
export class WorkflowWidgetBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	entityOptions: EntityOption[] = [
		{value: 'SCOPE', label: 'Scope'},
		{value: 'EVENT', label: 'Event'},
		{value: 'FORM', label: 'Form'},
		{value: 'FIELD', label: 'Field'}
	];

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowWidgetBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowWidgetBasicInfoDialogData,
		private workflowWidgetManager: WorkflowWidgetManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.workflowWidget;
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
			const ww = this.data.workflowWidget;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [ww?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [ww?.longname?.[lang.languageCode] || ''],
				description: [ww?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const ww = this.data.workflowWidget;
		this.form = this.fb.group({
			id: [ww?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowEntity: [ww?.workflowEntity || '', Validators.required],
			filterExpectedEvents: [ww?.filterExpectedEvents ?? false]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentWorkflowWidgetId = this.data.workflowWidget?.workflowWidgetId;
		return this.workflowWidgetManager.getAll().some(ww =>
			ww.id.toUpperCase() === code.toUpperCase() && ww.workflowWidgetId !== currentWorkflowWidgetId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A workflow widget with code "${code}" already exists`);
			return;
		}

		const {shortname, longname, description} = this.collectTranslations();
		const message: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.message) {
				message[langCode] = v.message;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			shortname,
			longname,
			description
		});
	}
}
