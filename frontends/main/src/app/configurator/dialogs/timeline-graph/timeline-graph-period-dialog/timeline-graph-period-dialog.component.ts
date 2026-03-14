import {TimelineGraph} from '@core/model/timeline-graph';
import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {BaseDialogComponent} from '../../base-dialog.component';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {LanguageService} from '../../../services/language.service';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

export interface TimelineGraphPeriodDialogData {
	timelineGraph: TimelineGraph;
	eventModels: EventModel[];
}

@Component({
	selector: 'app-timeline-graph-period-dialog',
	standalone: true,
	templateUrl: './timeline-graph-period-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, ReactiveFormsModule, MatSelectModule, MatCheckboxModule, MatButtonModule, MatIcon]
})
export class TimelineGraphPeriodDialogComponent extends BaseDialogComponent<TimelineGraphPeriodDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<TimelineGraphPeriodDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphPeriodDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.timelineGraph) {
			this.form = this.fb.group({
				studyStartEventModelId: [this.data.timelineGraph.studyStartEventModelId || null],
				studyEndEventModelId: [this.data.timelineGraph.studyEndEventModelId || null],
				studyPeriodIsDefault: [this.data.timelineGraph.studyPeriodIsDefault ?? false]
			});
		}
	}

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.value;
			const result: any = {};

			const normalize = (value: any) => (value === '' || value === undefined) ? null : value;

			const normalizedStart = normalize(formValue.studyStartEventModelId);
			const originalStart = normalize(this.data.timelineGraph.studyStartEventModelId);
			if(normalizedStart !== originalStart) {
				result.studyStartEventModelId = normalizedStart;
			}

			const normalizedEnd = normalize(formValue.studyEndEventModelId);
			const originalEnd = normalize(this.data.timelineGraph.studyEndEventModelId);
			if(normalizedEnd !== originalEnd) {
				result.studyEndEventModelId = normalizedEnd;
			}

			const normalizedDefault = formValue.studyPeriodIsDefault;
			const originalDefault = this.data.timelineGraph.studyPeriodIsDefault ?? false;
			if(normalizedDefault !== originalDefault) {
				result.studyPeriodIsDefault = normalizedDefault;
			}

			this.dialogRef.close(result);
		}
	}
}
