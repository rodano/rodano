import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {Report} from '@core/model/report';
import {ReportManagerService} from '../../../services/manager/report-manager.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {LanguageService} from '../../../services/language.service';
import { Workflow } from '@core/model/workflow';
import { DatasetModel } from '@core/model/dataset-model';

export interface ReportBasicInfoDialogData {
	projectId: string;
	report: Report | null;
	languages: ProjectLanguage[];
	workflows: Workflow[];
	datasetModels: DatasetModel[];
}

@Component({
	selector: 'app-report-basic-info-dialog',
	standalone: true,
	templateUrl: './report-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatTabsModule,
		MatSelectModule
	]
})
export class ReportBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	workflows: Workflow[] = [];
	datasetModels: DatasetModel[] = [];
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ReportBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ReportBasicInfoDialogData,
		private reportManager: ReportManagerService,
		private snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef);
		this.isEditMode = !!data.report;
		this.workflows = data.workflows || [];
		this.datasetModels = data.datasetModels || [];
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
			const r = this.data.report;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [r?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [r?.longname?.[lang.languageCode] || ''],
				description: [r?.description?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const r = this.data.report;
		this.form = this.fb.group({
			id: [r?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			workflowId: [r?.workflowId || '', Validators.required],
			datasetModelId: [r?.datasetModelId || '', Validators.required]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentReportId = this.data.report?.reportId;
		return this.reportManager.getAll().some(r =>
			r.id.toUpperCase() === code.toUpperCase() && r.reportId !== currentReportId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.snackBar.open(`A report with code "${code}" already exists`, 'Close', {duration: 3000});
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
