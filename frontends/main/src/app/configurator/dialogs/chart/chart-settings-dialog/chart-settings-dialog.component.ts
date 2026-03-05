import {ProjectLanguage} from '@core/model/project-language';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatTabsModule} from '@angular/material/tabs';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../../services/language.service';
import {BaseInfoDialogComponent} from '../../base-info-dialog.component';
import {ChartModel} from '@core/model/chart-model';
import {ScopeModel} from '@core/model/scope-model';

export interface ChartSettingsDialogData {
	chart: ChartModel;
	languages: ProjectLanguage[];
	availableScopeModels: ScopeModel[];
}

@Component({
	selector: 'app-chart-settings-dialog',
	standalone: true,
	templateUrl: './chart-settings-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatTabsModule, MatCheckboxModule, MatSelectModule]
})
export class ChartSettingsDialogComponent extends BaseInfoDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		fb: FormBuilder,
		languageService: LanguageService,
		dialogRef: MatDialogRef<ChartSettingsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartSettingsDialogData,
		snackBar: MatSnackBar
	) {
		super(fb, languageService, dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.loadProjectLanguages(this.data.languages);
		this.initializeForm();
	}

	get showLeafScopeModel(): boolean {
		return this.data.chart?.type !== 'WORKFLOW_STATUS';
	}

	initializeLanguageForms(): void {
		this.availableLanguages.forEach(lang => {
			if(!lang.languageCode) {
				return;
			}
			const c = this.data.chart;
			this.languageForms.set(lang.languageCode, this.fb.group({
				title: [c?.title?.[lang.languageCode] || ''],
				legendX: [c?.legendX?.[lang.languageCode] || ''],
				legendY: [c?.legendY?.[lang.languageCode] || '']
			}));
		});
	}

	initializeForm(): void {
		const c = this.data.chart;
		this.form = this.fb.group({
			overrideUserRights: [c?.overrideUserRights ?? false],
			leafScopeModelId: [c?.leafScopeModelId ?? null]
		});
	}

	onSave(): void {
		if(this.form.invalid || !this.areLanguageFormsValid()) {
			this.showError();
			return;
		}

		const title: Record<string, string> = {};
		const legendX: Record<string, string> = {};
		const legendY: Record<string, string> = {};

		this.languageForms.forEach((langForm, langCode) => {
			const v = langForm.value;
			if(v.title) {
				title[langCode] = v.title;
			}
			if(v.legendX) {
				legendX[langCode] = v.legendX;
			}
			if(v.legendY) {
				legendY[langCode] = v.legendY;
			}
		});

		this.dialogRef.close({
			title: Object.keys(title).length > 0 ? title : this.data.chart.title,
			legendX: Object.keys(legendX).length > 0 ? legendX : this.data.chart.legendX,
			legendY: Object.keys(legendY).length > 0 ? legendY : this.data.chart.legendY,
			overrideUserRights: this.form.value.overrideUserRights,
			leafScopeModelId: this.showLeafScopeModel ? this.form.value.leafScopeModelId : null
		});
	}
}
