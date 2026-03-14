import {TimelineGraph} from '@core/model/timeline-graph';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {BaseDialogComponent} from '../../base-dialog.component';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {LanguageService} from '../../../services/language.service';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

export interface TimelineGraphDesignDialogData {
	timelineGraph: TimelineGraph;
}

@Component({
	selector: 'app-timeline-graph-design-dialog',
	standalone: true,
	templateUrl: './timeline-graph-design-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, ReactiveFormsModule, MatCheckboxModule, MatButtonModule, MatIcon]
})
export class TimelineGraphDesignDialogComponent extends BaseDialogComponent<TimelineGraphDesignDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<TimelineGraphDesignDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphDesignDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		if(this.data.timelineGraph) {
			this.form = this.fb.group({
				width: [this.data.timelineGraph.width || null],
				height: [this.data.timelineGraph.height || null],
				legendWidth: [this.data.timelineGraph.legendWidth || null],
				scrollerHeight: [this.data.timelineGraph.scrollerHeight || null],
				showScroller: [this.data.timelineGraph.showScroller ?? false]
			});
		}
	}

	onSave(): void {
		if(this.form.valid) {
			const formValue = this.form.value;
			const result: any = {};

			const normalize = (value: any) => (value === '' || value === undefined) ? null : value;

			const normalizedWidth = normalize(formValue.width);
			const originalWidth = normalize(this.data.timelineGraph.width);
			if(normalizedWidth !== originalWidth) {
				result.width = normalizedWidth;
			}

			const normalizedHeight = normalize(formValue.height);
			const originalHeight = normalize(this.data.timelineGraph.height);
			if(normalizedHeight !== originalHeight) {
				result.height = normalizedHeight;
			}

			const normalizedLegendWidth = normalize(formValue.legendWidth);
			const originalLegendWidth = normalize(this.data.timelineGraph.legendWidth);
			if(normalizedLegendWidth !== originalLegendWidth) {
				result.legendWidth = normalizedLegendWidth;
			}

			const normalizedScrollerHeight = normalize(formValue.scrollerHeight);
			const originalScrollerHeight = normalize(this.data.timelineGraph.scrollerHeight);
			if(normalizedScrollerHeight !== originalScrollerHeight) {
				result.scrollerHeight = normalizedScrollerHeight;
			}

			const normalizedScroller = formValue.showScroller;
			const originalScroller = this.data.timelineGraph.showScroller ?? false;
			if(normalizedScroller !== originalScroller) {
				result.showScroller = normalizedScroller;
			}

			this.dialogRef.close(result);
		}
	}
}
