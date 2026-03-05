import {DatasetModel} from '@core/model/dataset-model';
import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface DatasetModelLabelPatternsDialogData {
	datasetModel: DatasetModel;
}

interface PatternOption {
	pattern: string;
	description: string;
}

@Component({
	selector: 'app-dataset-model-label-patterns-dialog',
	standalone: true,
	templateUrl: './dataset-model-label-patterns-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule]
})
export class DatasetModelLabelPatternsDialogComponent extends BaseDialogComponent<DatasetModelLabelPatternsDialogData> implements OnInit, AfterViewInit {
	@ViewChild('collapsedInput') collapsedInput!: ElementRef<HTMLInputElement>;
	@ViewChild('expandedInput') expandedInput!: ElementRef<HTMLInputElement>;

	form: FormGroup;
	lastFocusedField: 'collapsed' | 'expanded' = 'collapsed';

	patternOptions: PatternOption[] = [
		{pattern: '${shortname}', description: 'Short name'},
		{pattern: '${longname}', description: 'Long name'},
		{pattern: '${description}', description: 'Description'},
		{pattern: '${number}', description: 'Instance number'},
		{pattern: '${fieldModelId:FIELD_MODEL_ID}', description: 'Field value by ID'}
	];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<DatasetModelLabelPatternsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: DatasetModelLabelPatternsDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.datasetModel) {
			this.form = this.fb.group({
				collapsedLabelPattern: [this.data.datasetModel.collapsedLabelPattern || ''],
				expandedLabelPattern: [this.data.datasetModel.expandedLabelPattern || '']
			});
		}
	}

	ngAfterViewInit(): void {
		if(this.collapsedInput && this.expandedInput) {
			this.collapsedInput.nativeElement.addEventListener('focus', () => {
				this.lastFocusedField = 'collapsed';
			});
			this.expandedInput.nativeElement.addEventListener('focus', () => {
				this.lastFocusedField = 'expanded';
			});

			setTimeout(() => {
				this.collapsedInput.nativeElement.focus();
			}, 0);
		}
	}

	insertPattern(pattern: string): void {
		const controlName = this.lastFocusedField === 'collapsed'
			? 'collapsedLabelPattern'
			: 'expandedLabelPattern';
		const input = this.lastFocusedField === 'collapsed'
			? this.collapsedInput
			: this.expandedInput;
		const control = this.form.get(controlName);

		if(control && input) {
			const element = input.nativeElement;
			const currentValue = control.value || '';
			const start = element.selectionStart ?? currentValue.length;
			const end = element.selectionEnd ?? currentValue.length;

			const newValue = currentValue.substring(0, start) + pattern + currentValue.substring(end);
			control.setValue(newValue);

			setTimeout(() => {
				element.focus();
				const newPosition = start + pattern.length;
				element.setSelectionRange(newPosition, newPosition);
			}, 0);
		}
	}

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.value;
			const result: any = {};

			const normalize = (value: any) => {
				if(value === '' || value === null || value === undefined) {
					return null;
				}
				return value;
			};

			const normalizedCollapsedPattern = normalize(formValue.collapsedLabelPattern);
			const normalizedOriginalCollapsedPattern = normalize(this.data.datasetModel.collapsedLabelPattern);
			if(normalizedCollapsedPattern !== normalizedOriginalCollapsedPattern) {
				result.collapsedLabelPattern = formValue.collapsedLabelPattern;
			}

			const normalizedExpandedPattern = normalize(formValue.expandedLabelPattern);
			const normalizedOriginalExpandedPattern = normalize(this.data.datasetModel.expandedLabelPattern);
			if(normalizedExpandedPattern !== normalizedOriginalExpandedPattern) {
				result.expandedLabelPattern = formValue.expandedLabelPattern;
			}

			this.dialogRef.close(result);
		}
	}
}
