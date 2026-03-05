import {Component, Inject, OnInit} from '@angular/core';
import {WorkflowState} from '@core/model/workflow-state';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowStateAggregationDialogData {
	workflowState: WorkflowState;
	aggregatedWorkflowStates: WorkflowState[];
}

@Component({
	selector: 'app-workflow-state-aggregation-dialog',
	standalone: true,
	templateUrl: './workflow-state-aggregation-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatSelectModule]
})
export class WorkflowStateAggregationDialogComponent extends BaseDialogComponent<WorkflowStateAggregationDialogData> implements OnInit {
	form: FormGroup;

	matcherOptions = [
		{value: null, label: 'None'},
		{value: 'ALL', label: 'All'},
		{value: 'ONE', label: 'One'},
		{value: 'DEFAULT', label: 'Default'}
	];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowStateAggregationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowStateAggregationDialogData
	) {
		super(dialogRef, data);
	}

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
}
