import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {LanguageService} from '../../../services/language.service';

export interface EventModelSchedulingDialogData {
	eventModel: EventModel;
	allEventModels: EventModel[];
}

@Component({
	selector: 'app-event-model-scheduling-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		ReactiveFormsModule
	],
	templateUrl: './event-model-scheduling-dialog.component.html',
	styleUrls: ['../shared-scope-model-dialog-styles.css']
})
export class EventModelSchedulingDialogComponent implements OnInit {
	form: FormGroup;

	timeUnits = [
		{value: 'SECONDS', label: 'Seconds'},
		{value: 'MINUTES', label: 'Minutes'},
		{value: 'HOURS', label: 'Hours'},
		{value: 'DAYS', label: 'Days'},
		{value: 'WEEKS', label: 'Weeks'},
		{value: 'MONTHS', label: 'Months'},
		{value: 'YEARS', label: 'Years'}
	];

	aggregationFunctions = [
		{value: 'MIN', label: 'Minimum'},
		{value: 'MAX', label: 'Maximum'}
	];

	availableEventModels: EventModel[] = [];
	selectedEventModels: EventModel[] = [];

	constructor(
		private languageService: LanguageService,
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<EventModelSchedulingDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelSchedulingDialogData
	) {
		this.form = this.fb.group({
			deadline: [null, [Validators.min(0)]],
			deadlineUnit: [''],
			deadlineAggregationFunction: [''],
			interval: [null, [Validators.min(0)]],
			intervalUnit: ['']
		});
	}

	ngOnInit(): void {
		if(this.data.eventModel) {
			this.form.patchValue({
				deadline: this.data.eventModel.deadline,
				deadlineUnit: this.data.eventModel.deadlineUnit || '',
				deadlineAggregationFunction: this.data.eventModel.deadlineAggregationFunction || '',
				interval: this.data.eventModel.interval,
				intervalUnit: this.data.eventModel.intervalUnit || ''
			});

			const selectedIds = this.data.eventModel.deadlineReferenceEventModelIds || [];

			const otherEventModels = this.data.allEventModels.filter(
				em => em.eventModelId !== this.data.eventModel.eventModelId
			);

			this.selectedEventModels = otherEventModels.filter(em =>
				selectedIds.includes(em.eventModelId)
			);

			this.availableEventModels = otherEventModels.filter(em =>
				!selectedIds.includes(em.eventModelId)
			);
		}
	}

	onAddEventModel(eventModel: EventModel): void {
		this.availableEventModels = this.availableEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.selectedEventModels = [...this.selectedEventModels, eventModel];
	}

	onRemoveEventModel(eventModel: EventModel): void {
		this.selectedEventModels = this.selectedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.availableEventModels = [...this.availableEventModels, eventModel];
	}

	getEventModelLabel(eventModel: EventModel): string {
		const shortname = this.languageService.getDefaultTranslation(eventModel.shortname) || eventModel.id;
		return `${shortname} (${eventModel.id})`;
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

			const normalizedDeadline = normalize(formValue.deadline);
			const normalizedOriginalDeadline = normalize(this.data.eventModel.deadline);
			if(normalizedDeadline !== normalizedOriginalDeadline) {
				result.deadline = formValue.deadline;
			}

			const normalizedDeadlineUnit = normalize(formValue.deadlineUnit);
			const normalizedOriginalDeadlineUnit = normalize(this.data.eventModel.deadlineUnit);
			if(normalizedDeadlineUnit !== normalizedOriginalDeadlineUnit) {
				result.deadlineUnit = formValue.deadlineUnit;
			}

			const normalizedDeadlineAggr = normalize(formValue.deadlineAggregationFunction);
			const normalizedOriginalDeadlineAggr = normalize(this.data.eventModel.deadlineAggregationFunction);
			if(normalizedDeadlineAggr !== normalizedOriginalDeadlineAggr) {
				result.deadlineAggregationFunction = formValue.deadlineAggregationFunction;
			}

			const normalizedInterval = normalize(formValue.interval);
			const normalizedOriginalInterval = normalize(this.data.eventModel.interval);
			if(normalizedInterval !== normalizedOriginalInterval) {
				result.interval = formValue.interval;
			}

			const normalizedIntervalUnit = normalize(formValue.intervalUnit);
			const normalizedOriginalIntervalUnit = normalize(this.data.eventModel.intervalUnit);
			if(normalizedIntervalUnit !== normalizedOriginalIntervalUnit) {
				result.intervalUnit = formValue.intervalUnit;
			}

			const originalIds = this.data.eventModel.deadlineReferenceEventModelIds || [];
			const currentIds = this.selectedEventModels.map(em => em.eventModelId);

			if(JSON.stringify(originalIds.sort()) !== JSON.stringify(currentIds.sort())) {
				result.deadlineReferenceEventModelIds = currentIds;
			}

			this.dialogRef.close(result);
		}
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
