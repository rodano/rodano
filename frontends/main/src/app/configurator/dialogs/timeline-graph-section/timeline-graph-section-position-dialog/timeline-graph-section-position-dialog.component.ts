import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {BaseDialogComponent} from '../../base-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';

export interface TimelineGraphSectionPositionDialogData {
	section: TimelineGraphSection;
}

@Component({
	selector: 'app-timeline-graph-section-position-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-position-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule]
})
export class TimelineGraphSectionPositionDialogComponent extends BaseDialogComponent<TimelineGraphSectionPositionDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<TimelineGraphSectionPositionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionPositionDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const tgs = this.data.section;
		this.form = this.fb.group({
			positionStart: [tgs.positionStart ?? null],
			positionStop: [tgs.positionStop ?? null]
		});
	}

	onSave(): void {
		const tgs = this.data.section;
		const fv = this.form.value;
		const result: any = {};

		const normalize = (v: any) => (v === '' || v === undefined) ? null : v;

		if(normalize(fv.positionStart) !== normalize(tgs.positionStart)) {
			result.positionStart = normalize(fv.positionStart);
		}
		if(normalize(fv.positionStop) !== normalize(tgs.positionStop)) {
			result.positionStop = normalize(fv.positionStop);
		}

		this.dialogRef.close(result);
	}
}
