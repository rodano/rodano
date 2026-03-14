import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {BaseDialogComponent} from '../../base-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';

export interface TimelineGraphSectionScaleDialogData {
	section: TimelineGraphSection;
}

@Component({
	selector: 'app-timeline-graph-section-scale-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-scale-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule]
})
export class TimelineGraphSectionScaleDialogComponent extends BaseDialogComponent<TimelineGraphSectionScaleDialogData> implements OnInit {
	form: FormGroup;

	scalePositionOptions = [
		{value: 'LEFT', label: 'Left'},
		{value: 'RIGHT', label: 'Right'}
	];

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<TimelineGraphSectionScaleDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionScaleDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const tgs = this.data.section;
		this.form = this.fb.group({
			scaleMin: [tgs.scaleMin ?? null],
			scaleMax: [tgs.scaleMax ?? null],
			scaleDecimal: [tgs.scaleDecimal ?? null],
			scaleMarkInterval: [tgs.scaleMarkInterval ?? null],
			scaleLabelInterval: [tgs.scaleLabelInterval ?? null],
			scalePosition: [tgs.scalePosition || null]
		});
	}

	onSave(): void {
		const tgs = this.data.section;
		const fv = this.form.value;
		const result: any = {};

		const normalize = (v: any) => (v === '' || v === undefined) ? null : v;

		if(normalize(fv.scaleMin) !== normalize(tgs.scaleMin)) {
			result.scaleMin = normalize(fv.scaleMin);
		}
		if(normalize(fv.scaleMax) !== normalize(tgs.scaleMax)) {
			result.scaleMax = normalize(fv.scaleMax);
		}
		if(normalize(fv.scaleDecimal) !== normalize(tgs.scaleDecimal)) {
			result.scaleDecimal = normalize(fv.scaleDecimal);
		}
		if(normalize(fv.scaleMarkInterval) !== normalize(tgs.scaleMarkInterval)) {
			result.scaleMarkInterval = normalize(fv.scaleMarkInterval);
		}
		if(normalize(fv.scaleLabelInterval) !== normalize(tgs.scaleLabelInterval)) {
			result.scaleLabelInterval = normalize(fv.scaleLabelInterval);
		}
		if(normalize(fv.scalePosition) !== normalize(tgs.scalePosition)) {
			result.scalePosition = normalize(fv.scalePosition);
		}

		this.dialogRef.close(result);
	}
}
