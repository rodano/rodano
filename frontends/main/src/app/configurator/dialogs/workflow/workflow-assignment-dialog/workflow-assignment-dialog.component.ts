import {Workflow} from '@core/model/workflow';
import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowAction} from '@core/model/workflow-action';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface WorkflowAssignmentDialogData {
	workflow: Workflow;
	availableWorkflows: Workflow[];
	currentWorkflowStates: WorkflowState[];
	currentWorkflowActions: WorkflowAction[];
}

@Component({
	selector: 'app-workflow-assignment-dialog',
	standalone: true,
	templateUrl: './workflow-assignment-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatCheckboxModule, MatSelectModule]
})
export class WorkflowAssignmentDialogComponent extends BaseDialogComponent<WorkflowAssignmentDialogData> implements OnInit, OnDestroy {
	form: FormGroup;
	availableStates: WorkflowState[] = [];
	availableActions: WorkflowAction[] = [];

	private formSubscriptions = new Subscription();

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowAssignmentDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowAssignmentDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.initializeForm();
		this.availableStates = this.data.currentWorkflowStates;
		this.availableActions = this.data.currentWorkflowActions;
		this.subscribeToFormChanges();
	}

	ngOnDestroy(): void {
		this.formSubscriptions.unsubscribe();
	}

	get hasAggregateWorkflow(): boolean {
		return !!this.form.get('aggregatedWorkflowId')?.value;
	}

	get isMandatory(): boolean {
		return !!this.form.get('mandatory')?.value;
	}

	private initializeForm(): void {
		const wf = this.data.workflow;

		this.form = this.fb.group({
			aggregatedWorkflowId: [wf?.aggregatedWorkflowId ?? null],
			mandatory: [wf?.mandatory ?? false],
			initialStateId: [wf?.initialStateId ?? null],
			actionId: [wf?.actionId ?? null],
			unique: [wf?.unique ?? false]
		});
	}

	private subscribeToFormChanges(): void {
		this.formSubscriptions.add(
			this.form.get('aggregatedWorkflowId')!.valueChanges.subscribe((workflowId: string | null) => {
				if(workflowId) {
					this.form.patchValue({
						mandatory: false,
						initialStateId: null,
						unique: false
					}, {emitEvent: false});
				}
			})
		);
		this.formSubscriptions.add(
			this.form.get('mandatory')!.valueChanges.subscribe((mandatory: boolean) => {
				if(mandatory) {
					this.form.patchValue({actionId: null}, {emitEvent: false});
				}
			})
		);
	}

	onSave(): void {
		if(this.form.invalid) {
			this.showError();
			return;
		}

		const formValue = this.form.getRawValue();
		const hasAggregate = !!formValue.aggregatedWorkflowId;
		const mandatory = !hasAggregate && formValue.mandatory;

		this.dialogRef.close({
			aggregatedWorkflowId: formValue.aggregatedWorkflowId || null,
			mandatory: hasAggregate ? false : mandatory,
			initialStateId: hasAggregate ? null : (formValue.initialStateId || null),
			unique: hasAggregate ? false : formValue.unique,
			actionId: mandatory ? null : (formValue.actionId || null)
		});
	}
}
