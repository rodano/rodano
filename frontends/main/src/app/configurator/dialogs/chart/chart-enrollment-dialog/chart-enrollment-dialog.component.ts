import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {ChartModel} from '@core/model/chart-model';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';
import {MatSelectModule} from '@angular/material/select';

export interface ChartEnrollmentDialogData {
	chart: ChartModel;
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-chart-enrollment-dialog',
	standalone: true,
	templateUrl: './chart-enrollment-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatCheckboxModule, MatSelectModule]
})
export class ChartEnrollmentDialogComponent implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		private dialogRef: MatDialogRef<ChartEnrollmentDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ChartEnrollmentDialogData
	) {}

	ngOnInit(): void {
		this.form = this.fb.group({
			displayExpected: [this.data.chart?.displayExpected ?? false],
			workflowId: [this.data.chart?.workflowId ?? null]
		});
	}

	onSave(): void {
		this.dialogRef.close({
			displayExpected: this.form.value.displayExpected,
			workflowId: this.form.value.workflowId
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}
}
