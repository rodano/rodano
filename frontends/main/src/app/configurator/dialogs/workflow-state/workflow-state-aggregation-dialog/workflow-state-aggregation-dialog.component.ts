import {Component, Inject, OnInit} from '@angular/core';
import {WorkflowState} from '@core/model/workflow-state';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';

export interface WorkflowStateAggregationDialogData {
	workflowState: WorkflowState;
	aggregatedWorkflowStates: {id: string; name: string; code: string}[];
}

@Component({
	selector: 'app-workflow-state-aggregation-dialog',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatButtonModule,
		MatSelectModule
	],
	templateUrl: './workflow-state-aggregation-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class WorkflowStateAggregationDialogComponent implements OnInit {
	form: FormGroup;

	matcherOptions = [
		{value: null, label: 'None'},
		{value: 'ALL', label: 'All'},
		{value: 'ONE', label: 'One'},
		{value: 'DEFAULT', label: 'Default'}
	];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<WorkflowStateAggregationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowStateAggregationDialogData
	) {}

	ngOnInit(): void {
		this.form = this.fb.group({
			aggregateStateId: [this.data.workflowState.aggregateStateId || null],
			aggregateStateMatcher: [this.data.workflowState.aggregateStateMatcher || null]
		});
	}

	onSave(): void {
		const formValue = this.form.value;
		const result: any = {};

		const normalize = (v: any) => v === '' ? null : v;

		if(normalize(formValue.aggregateStateId) !== normalize(this.data.workflowState.aggregateStateId)) {
			result.aggregateStateId = formValue.aggregateStateId || null;
		}
		if(normalize(formValue.aggregateStateMatcher) !== normalize(this.data.workflowState.aggregateStateMatcher)) {
			result.aggregateStateMatcher = formValue.aggregateStateMatcher || null;
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
