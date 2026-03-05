import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {Validator} from '@core/model/validator';
import {FormsModule} from '@angular/forms';
import {Workflow} from '@core/model/workflow';
import {WorkflowState} from '@core/model/workflow-state';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface ValidatorWorkflowDialogData {
	validator: Validator;
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-validator-workflow-dialog',
	standalone: true,
	templateUrl: './validator-workflow-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, FormsModule]
})
export class ValidatorWorkflowDialogComponent extends BaseDialogComponent<ValidatorWorkflowDialogData> implements OnInit {
	selectedWorkflowId: string | null = null;
	selectedInvalidStateId: string | null = null;
	selectedValidStateId: string | null = null;
	availableStates: WorkflowState[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<ValidatorWorkflowDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ValidatorWorkflowDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.selectedWorkflowId = this.data.validator.workflowId || null;
		this.selectedInvalidStateId = this.data.validator.invalidStateId || null;
		this.selectedValidStateId = this.data.validator.validStateId || null;

		if(this.selectedWorkflowId) {
			const workflow = this.data.availableWorkflows.find(wf => wf.workflowId === this.selectedWorkflowId);
			this.availableStates = workflow ? workflow.states : [];
		}
	}

	onWorkflowSelected(workflowId: string): void {
		this.selectedWorkflowId = workflowId;
		this.selectedInvalidStateId = null;
		this.selectedValidStateId = null;
		const workflow = this.data.availableWorkflows.find(wf => wf.workflowId === workflowId);
		this.availableStates = workflow ? [...workflow.states] : [];
	}

	onSave(): void {
		this.dialogRef.close({
			workflowId: this.selectedWorkflowId || undefined,
			invalidStateId: this.selectedInvalidStateId || undefined,
			validStateId: this.selectedValidStateId || undefined
		});
	}
}
