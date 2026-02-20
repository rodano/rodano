import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';

export interface EventModelLabelPatternDialogData {
	eventModel: EventModel;
}

interface PatternOption {
	pattern: string;
	description: string;
}

@Component({
	selector: 'app-event-model-label-pattern-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		ReactiveFormsModule
	],
	templateUrl: './event-model-label-pattern-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class EventModelLabelPatternDialogComponent implements OnInit {
	form: FormGroup;

	patternOptions: PatternOption[] = [
		{pattern: '${id}', description: 'Event model ID'},
		{pattern: '${shortname}', description: 'Event short name'},
		{pattern: '${longname}', description: 'Event long name'},
		{pattern: '${description}', description: 'Event description'},
		{pattern: '${number}', description: 'Event number'},
		{pattern: '${date}', description: 'Date'},
		{pattern: '${expectedDate}', description: 'Expected date'},
		{pattern: '${actualDate}', description: 'Actual date'},
		{pattern: '${endDate}', description: 'End date'},
		{pattern: '${time}', description: 'Time'},
		{pattern: '${durationInDays}', description: 'Duration in days'},
		{pattern: '${durationInMonths}', description: 'Duration in months'},
		{pattern: '${durationInYears}', description: 'Duration in years'},
		{pattern: '${durationAuto}', description: 'Duration (auto)'},
		{pattern: '${rawGroupNumber}', description: 'Raw group number'},
		{pattern: '${groupNumber}', description: 'Group number'},
		{pattern: '${interval}', description: 'Interval'},
		{pattern: '${intervalUnit}', description: 'Interval unit'},
		{pattern: '${intervalText}', description: 'Interval text'},
		{pattern: '${deadline}', description: 'Deadline'},
		{pattern: '${deadlineUnit}', description: 'Deadline unit'},
		{pattern: '${datasetModelCode:DATASET_MODEL_CODE-fieldModelCode:FIELD_MODEL_CODE}', description: 'Dataset field value'}
	];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventModelLabelPatternDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelLabelPatternDialogData
	) {
		this.form = this.fb.group({
			labelPattern: [''],
			icon: ['']
		});
	}

	ngOnInit(): void {
		if(this.data.eventModel) {
			this.form.patchValue({
				labelPattern: this.data.eventModel.labelPattern || '',
				icon: this.data.eventModel.icon || ''
			});
		}
	}

	insertPattern(pattern: string): void {
		const labelPatternControl = this.form.get('labelPattern');
		const currentValue = labelPatternControl?.value || '';
		labelPatternControl?.setValue(currentValue + pattern);
	}

	get iconPreview(): string {
		return this.form.get('icon')?.value?.trim() || '';
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

			const normalizedLabelPattern = normalize(formValue.labelPattern);
			const normalizedOriginalLabelPattern = normalize(this.data.eventModel.labelPattern);
			if(normalizedLabelPattern !== normalizedOriginalLabelPattern) {
				result.labelPattern = formValue.labelPattern;
			}

			const normalizedIcon = normalize(formValue.icon);
			const normalizedOriginalIcon = normalize(this.data.eventModel.icon);
			if(normalizedIcon !== normalizedOriginalIcon) {
				result.icon = formValue.icon;
			}

			this.dialogRef.close(result);
		}
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
