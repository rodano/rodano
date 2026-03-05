import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ChartManagerService} from '../../../services/manager/chart-manager.service';
import {MatSelectModule} from '@angular/material/select';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {ChartModel} from '@core/model/chart-model';

export interface ChartBasicInfoDialogData {
	projectId: string;
	chart: ChartModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-chart-basic-info-dialog',
	standalone: true,
	templateUrl: './chart-basic-info-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatSelectModule]
})
export class ChartBasicInfoDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;
	isEditMode: boolean;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ChartBasicInfoDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartBasicInfoDialogData,
		private chartManager: ChartManagerService,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
		this.isEditMode = !!data.chart;
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
			const c = this.data.chart;
			this.languageForms.set(lang.languageCode, this.fb.group({
				shortname: [c?.shortname?.[lang.languageCode] || '', lang.isDefault ? Validators.required : []],
				longname: [c?.longname?.[lang.languageCode] || ''],
				description: [c?.description?.[lang.languageCode] || '']
			}));
		});
	}

	readonly chartTypes = [
		{value: 'ENROLLMENT_BY_SCOPE', label: 'Enrollment by Scope'},
		{value: 'ENROLLMENT', label: 'Enrollment'},
		{value: 'STATISTICS', label: 'Statistics'},
		{value: 'WORKFLOW_STATUS', label: 'Workflow Status'}
	];

	initializeForm(): void {
		const c = this.data.chart;
		this.form = this.fb.group({
			id: [c?.id || '', [Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]],
			type: [c?.type || null, Validators.required]
		});
	}

	isCodeDuplicate(code: string): boolean {
		const currentChartId = this.data.chart?.chartId;
		return this.chartManager.getAll().some(c =>
			c.id.toUpperCase() === code.toUpperCase() && c.chartId !== currentChartId
		);
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const code = this.form.getRawValue().id.toUpperCase();
		if(this.isCodeDuplicate(code)) {
			this.showError(`A chart with code "${code}" already exists`);
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
