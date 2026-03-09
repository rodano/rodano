import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {DatasetModelManagerService} from '../../../services/manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../../../services/manager/field-model-manager.service';
import {LanguageService} from '../../../services/language.service';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModel} from '@core/model/field-model';
import {LayoutType} from '@core/model/layout-type';
import {ColumnHeader} from '@core/model/column-header';
import {LayoutLine} from '@core/model/layout-line';
import {Cell} from '@core/model/cell';
import {Layout} from '@core/model/layout';
import {ProjectLanguage} from '@core/model/project-language';
import {MatTabsModule} from '@angular/material/tabs';

interface FormLayoutCreateDialogData {
	projectId: string;
	formModelId: string;
	generateFromDataset: boolean;
	layout?: Layout;
}

@Component({
	selector: 'app-form-layout-create-dialog',
	standalone: true,
	templateUrl: './form-layout-create-dialog.component.html',
	styleUrls: ['./form-layout-create-dialog.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule,
		MatCheckboxModule, MatButtonModule, MatIconModule, MatTabsModule]
})
export class FormLayoutCreateDialogComponent implements OnInit {
	form: FormGroup;
	generateFromDataset: boolean;
	isEditMode: boolean;
	readonly layoutTypes = [LayoutType.SINGLE, LayoutType.MULTIPLE];

	description: Record<string, string> = {};
	textBefore: Record<string, string> = {};
	textAfter: Record<string, string> = {};
	selectedLanguage = '';

	constructor(
		private dialogRef: MatDialogRef<FormLayoutCreateDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FormLayoutCreateDialogData,
		private fb: FormBuilder,
		public datasetModelManager: DatasetModelManagerService,
		public fieldModelManager: FieldModelManagerService,
		public languageService: LanguageService
	) {
		this.generateFromDataset = data.generateFromDataset;
		this.isEditMode = !!data.layout;
		this.selectedLanguage = languageService.getDefaultLanguageCode();

		const layout = data.layout;
		if(layout) {
			this.description = layout.description ? {...layout.description} : {};
			this.textBefore = layout.textBefore ? {...layout.textBefore} : {};
			this.textAfter = layout.textAfter ? {...layout.textAfter} : {};
		}

		this.form = this.fb.group({
			id: [
				{value: layout?.id ?? '', disabled: this.isEditMode},
				[Validators.required, Validators.pattern(/^[A-Z_][A-Z0-9_]*$/)]
			],
			type: [layout?.type ?? LayoutType.SINGLE, Validators.required],
			contribution: [layout?.contribution ?? false],
			layoutDatasetModelId: [
				layout?.datasetModel?.datasetModelId ?? '',
				layout?.type === LayoutType.MULTIPLE ? Validators.required : []
			],
			generateDatasetModelId: [
				'',
				data.generateFromDataset ? Validators.required : []
			],
			defaultSortFieldModelId: [layout?.defaultSortFieldModelId ?? ''],
			cssCode: [layout?.cssCode ?? ''],
			columnCount: [
				{value: layout?.columns?.length ?? 1, disabled: this.isEditMode},
				[Validators.required, Validators.min(1), Validators.max(6)]
			]
		});
	}

	ngOnInit(): void {
		this.form.get('type')?.valueChanges.subscribe(type => {
			const ctrl = this.form.get('layoutDatasetModelId')!;
			if(type === LayoutType.MULTIPLE) {
				ctrl.setValidators(Validators.required);
			}
			else {
				ctrl.clearValidators();
				ctrl.setValue('');
			}
			ctrl.updateValueAndValidity();
			this.form.get('defaultSortFieldModelId')?.setValue('');
		});

		this.form.get('layoutDatasetModelId')?.valueChanges.subscribe(() => {
			this.form.get('defaultSortFieldModelId')?.setValue('');
		});
	}

	get datasetModels(): DatasetModel[] {
		return this.datasetModelManager.getAll();
	}

	get previewFields(): FieldModel[] {
		const dsId = this.form.get('generateDatasetModelId')?.value;
		if(!dsId) {
			return [];
		}
		return this.fieldModelManager.getAll().filter(fm => fm.datasetModelId === dsId);
	}

	get projectLanguages(): ProjectLanguage[] {
		return this.languageService.projectLanguages;
	}

	get fieldModels(): FieldModel[] {
		return this.fieldModelManager.getAll();
	}

	get fieldsForSelectedDataset(): FieldModel[] {
		const dsId = this.form.get('layoutDatasetModelId')?.value;
		if(!dsId) {
			return [];
		}
		return this.fieldModelManager.getAll().filter(fm => fm.datasetModelId === dsId);
	}

	getText(map: Record<string, string>, langCode: string): string {
		return map[langCode] || '';
	}

	onTranslationChange(event: Event, map: Record<string, string>, langCode: string): void {
		map[langCode] = (event.target as HTMLInputElement | HTMLTextAreaElement).value;
	}

	getLanguageLabel(code: string, isDefault: boolean): string {
		const name = this.languageService.getLanguageName(code);
		return isDefault ? `${name} ☆` : name;
	}

	onCodeInput(event: Event): void {
		const input = event.target as HTMLInputElement;
		const upper = input.value.toUpperCase().replace(/[^A-Z0-9_]/g, '');
		this.form.get('id')?.setValue(upper, {emitEvent: false});
		input.value = upper;
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSubmit(): void {
		if(this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}

		const v = this.form.getRawValue();

		if(this.isEditMode) {
			const result: Partial<Layout> = {
				type: v.type,
				contribution: v.contribution,
				defaultSortFieldModelId: v.defaultSortFieldModelId || undefined,
				cssCode: v.cssCode || undefined,
				description: this.description,
				textBefore: this.textBefore,
				textAfter: this.textAfter,
				datasetModel: v.layoutDatasetModelId
					? this.datasetModelManager.getById(v.layoutDatasetModelId)
					: undefined
			};
			this.dialogRef.close(result);
			return;
		}

		const columns: ColumnHeader[] = Array.from({length: v.columnCount}, () => ({}));
		const lines: LayoutLine[] = [];

		if(this.generateFromDataset && v.generateDatasetModelId) {
			const fields = this.fieldModelManager.getAll()
				.filter(fm => fm.datasetModelId === v.generateDatasetModelId);

			for(let i = 0; i < fields.length; i += v.columnCount) {
				const chunk = fields.slice(i, i + v.columnCount);
				const cells: Cell[] = chunk.map(fm => ({
					formLayoutCellId: '',
					id: fm.id,
					datasetModelId: v.generateDatasetModelId,
					fieldModelId: fm.fieldModelId,
					visibilityCriteria: [],
					displayLabel: true,
					displayPossibleValueLabels: false,
					colspan: 1,
					hasPrintButton: false
				}));
				lines.push({formLayoutLineId: '', cells});
			}
		}

		const layout: Partial<Layout> = {
			id: v.id,
			type: v.type,
			contribution: v.contribution,
			defaultSortFieldModelId: v.defaultSortFieldModelId || undefined,
			cssCode: v.cssCode || undefined,
			description: this.description,
			textBefore: this.textBefore,
			textAfter: this.textAfter,
			datasetModel: v.layoutDatasetModelId
				? this.datasetModelManager.getById(v.layoutDatasetModelId)
				: undefined,
			columns,
			lines,
			formModelId: this.data.formModelId
		};

		this.dialogRef.close(layout);
	}
}
