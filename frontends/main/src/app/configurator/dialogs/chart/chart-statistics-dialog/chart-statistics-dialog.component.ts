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
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatCheckboxModule,
		MatIconModule,
		MatSelectModule,
		MatTabsModule
	]
})
export class ChartStatisticsDialogComponent implements OnInit {
	form: FormGroup;
	ranges: ChartRange[] = [];
	selectedLanguage: string;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		private dialogRef: MatDialogRef<ChartStatisticsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ChartStatisticsDialogData,
		private snackBar: MatSnackBar
	) {}

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

	get filteredFieldModels(): FieldModel[] {
		if(!this.data.leafScopeModel?.datasetModelIds?.length) {
			return this.data.availableFieldModels;
		}

		const allowedDatasetIds = new Set(this.data.leafScopeModel.datasetModelIds);
		return this.data.availableFieldModels.filter(fm =>
			allowedDatasetIds.has(fm.datasetModelId)
		);
	}

	onSave(): void {
		const emptyIds = this.ranges.some(r => !r.id?.trim());
		if(emptyIds) {
			this.snackBar.open('All ranges must have an ID', 'Close', {duration: 3000});
			return;
		}

		this.dialogRef.close({
			fieldModelId: this.form.value.fieldModelId,
			datasetModelId: this.form.value.datasetModelId,
			withStatistics: this.form.value.withStatistics,
			ranges: this.ranges.map((r, i) => ({...r, sortOrder: i}))
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	protected readonly isNaN = isNaN;
}
