import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ChartModel} from '@core/model/chart-model';
import {ChartRange} from '@core/model/chart-range';
import {FieldModel} from '@core/model/field-model';
import {LanguageService} from '../../../services/language.service';
import {MatSelectModule} from '@angular/material/select';
import {DatasetModel} from '@core/model/dataset-model';
import {ScopeModel} from '@core/model/scope-model';
import {MatTabsModule} from '@angular/material/tabs';
import {ProjectLanguage} from '@core/model/project-language';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface ChartStatisticsDialogData {
	chart: ChartModel;
	availableFieldModels: FieldModel[];
	availableDatasetModels: DatasetModel[];
	leafScopeModel: ScopeModel | null;
	languages: ProjectLanguage[];
}

@Component({
	selector: 'app-chart-statistics-dialog',
	standalone: true,
	templateUrl: './chart-statistics-dialog.component.html',
	styleUrls: ['../../dialog-shared.css', './chart-statistics-dialog.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatCheckboxModule, MatIconModule, MatSelectModule,
		MatTabsModule]
})
export class ChartStatisticsDialogComponent extends BaseDialogComponent<ChartStatisticsDialogData> implements OnInit {
	form: FormGroup;
	ranges: ChartRange[] = [];
	selectedLanguage: string;

	filterDatasetModelId: string | null = null;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<ChartStatisticsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartStatisticsDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.selectedLanguage = this.languageService.currentLanguage
		  || this.languageService.getDefaultLanguageCode(this.data.languages)
		  || this.data.languages[0]?.languageCode
		  || '';

		const existingFieldModelId = this.data.chart?.fieldModelId ?? null;
		const existingDatasetModelId = this.data.chart?.datasetModelId ?? this.deriveDatasetModelId(existingFieldModelId);

		this.form = this.fb.group({
			fieldModelId: [existingFieldModelId],
			datasetModelId: [existingDatasetModelId],
			withStatistics: [this.data.chart?.withStatistics ?? false]
		});

		this.form.get('fieldModelId')!.valueChanges.subscribe(fieldModelId => {
			const datasetModelId = this.deriveDatasetModelId(fieldModelId);
			this.form.patchValue({datasetModelId}, {emitEvent: false});
		});

		this.ranges = this.data.chart?.ranges
			? JSON.parse(JSON.stringify(this.data.chart.ranges))
			: [];
	}

	private deriveDatasetModelId(fieldModelId: string | null): string | null {
		if(!fieldModelId) {
			return null;
		}
		const fieldModel = this.data.availableFieldModels.find(fm => fm.fieldModelId === fieldModelId);
		return fieldModel?.datasetModelId ?? null;
	}

	onDatasetFilterChange(datasetModelId: string | null): void {
		this.filterDatasetModelId = datasetModelId;
		const currentFieldModelId = this.form.value.fieldModelId;
		if(currentFieldModelId) {
			const stillValid = this.filteredFieldModels.some(fm => fm.fieldModelId === currentFieldModelId);
			if(!stillValid) {
				this.form.patchValue({fieldModelId: null, datasetModelId: null});
			}
		}
	}

	get filteredFieldModels(): FieldModel[] {
		let models = this.data.availableFieldModels;

		if(this.data.leafScopeModel?.datasetModelIds?.length) {
			const allowedDatasetIds = new Set(this.data.leafScopeModel.datasetModelIds);
			models = models.filter(fm => allowedDatasetIds.has(fm.datasetModelId));
		}

		if(this.filterDatasetModelId) {
			models = models.filter(fm => fm.datasetModelId === this.filterDatasetModelId);
		}

		return models;
	}

	addRange(): void {
		this.ranges = [...this.ranges, {
			chartRangeId: crypto.randomUUID(),
			chartId: this.data.chart.chartId,
			id: '',
			value: '',
			label: {},
			min: undefined,
			max: undefined,
			other: false,
			sortOrder: this.ranges.length
		}];
	}

	removeRange(index: number): void {
		this.ranges = this.ranges.filter((_, i) => i !== index);
	}

	updateRangeField(index: number, field: keyof ChartRange, value: any): void {
		this.ranges = this.ranges.map((r, i) =>
			i === index ? {...r, [field]: value} : r
		);
	}

	onLanguageChange(index: number): void {
		this.selectedLanguage = this.data.languages[index]?.languageCode || this.selectedLanguage;
	}

	getRangeLabelForLanguage(range: ChartRange): string {
		return range.label?.[this.selectedLanguage] || '';
	}

	updateRangeLabelForLanguage(index: number, value: string): void {
		const label = {...(this.ranges[index].label || {}), [this.selectedLanguage]: value};
		this.updateRangeField(index, 'label', label);
	}

	getLanguageLabel(lang: ProjectLanguage): string {
		const name = this.languageService.getLanguageName(lang.languageCode);
		return lang.isDefault ? `${name} *` : name;
	}

	onSave(): void {
		const emptyIds = this.ranges.some(r => !r.id?.trim());
		if(emptyIds) {
			this.showError('All ranges must have an ID');
			return;
		}

		this.dialogRef.close({
			fieldModelId: this.form.value.fieldModelId,
			datasetModelId: this.form.value.datasetModelId,
			withStatistics: this.form.value.withStatistics,
			ranges: this.ranges.map((r, i) => ({...r, sortOrder: i}))
		});
	}

	protected readonly isNaN = isNaN;
}
