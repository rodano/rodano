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
import {MatSelectModule} from '@angular/material/select';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {WorkflowSummaryManagerService} from '../../../services/manager/workflow-summary-manager.service';
import {ScopeModel} from '@core/model/scope-model';

export interface WorkflowSummaryBasicInfoDialogData {
	projectId: string;
	workflowSummary: WorkflowSummary | null;
	languages: ProjectLanguage[];
	scopeModels: ScopeModel[];
}

interface EntityOption {
	value: string;
	label: string;
}

@Component({
	selector: 'app-workflow-summary-basic-info-dialog',
	standalone: true,
	templateUrl: './workflow-summary-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatCheckboxModule, MatSelectModule]
})
export class WorkflowSummaryBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	scopeModels: ScopeModel[] = [];
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
		dialogRef: MatDialogRef<WorkflowSummaryBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowSummaryBasicInfoDialogData,
		private workflowSummaryManager: WorkflowSummaryManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.workflowSummary;
		this.scopeModels = data.scopeModels || [];
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
			const ws = this.data.workflowSummary;
			this.languageForms.set(lang.languageCode, this.fb.group({
				title: [ws?.title?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []]
			}));
		});
	}

	initializeForm(): void {
		const ws = this.data.workflowSummary;
		this.form = this.fb.group({
			id: [ws?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowEntity: [ws?.workflowEntity || '', Validators.required],
			displayLegend: [ws?.displayLegend ?? false],
			displayColumnExport: [ws?.displayColumnExport ?? false],
			leafScopeModelId: [ws?.leafScopeModelId || null]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentWorkflowSummaryId = this.data.workflowSummary?.workflowSummaryId;
		return this.workflowSummaryManager.getAll().some(ws =>
			ws.id.toUpperCase() === code.toUpperCase() && ws.workflowSummaryId !== currentWorkflowSummaryId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A workflow summary with code "${code}" already exists`);
			return;
		}

		const title: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.title) {
				title[langCode] = v.title;
			}
		});

		this.dialogRef.close({
			...this.form.value,
			id: code,
			title
		});
	}
}
