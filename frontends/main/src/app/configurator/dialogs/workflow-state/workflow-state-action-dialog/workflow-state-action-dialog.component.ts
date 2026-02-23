import {WorkflowState} from '@core/model/workflow-state';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {WorkflowAction} from '@core/model/workflow-action';
import {LanguageService} from '../../../services/language.service';

export interface WorkflowStateActionDialogData {
	workflowState: WorkflowState;
	availableWorkflowActions: WorkflowAction[];
}

@Component({
	selector: 'app-workflow-state-action-dialog',
	standalone: true,
	templateUrl: './workflow-state-action-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatSelectModule
	]
})
export class WorkflowStateActionDialogComponent implements OnInit {
	availableWorkflowActions: WorkflowAction[] = [];
	selectedWorkflowActions: WorkflowAction[] = [];

	constructor(
		public languageService: LanguageService,
		private dialogRef: MatDialogRef<WorkflowStateActionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowStateActionDialogData
	) {}

	ngOnInit(): void {
		const selectedIds = (this.data.workflowState.possibleActions || []).map(wfa => wfa.workflowActionId);
		this.selectedWorkflowActions = this.data.availableWorkflowActions.filter(wfa => selectedIds.includes(wfa.workflowActionId));
		this.availableWorkflowActions = this.data.availableWorkflowActions.filter(wfa => !selectedIds.includes(wfa.workflowActionId));
	}

	onAdd(workflowAction: WorkflowAction): void {
		this.availableWorkflowActions = this.availableWorkflowActions.filter(wfa => wfa.workflowActionId !== workflowAction.workflowActionId);
		this.selectedWorkflowActions = [...this.selectedWorkflowActions, workflowAction];
	}

	onRemove(workflowAction: WorkflowAction): void {
		this.selectedWorkflowActions = this.selectedWorkflowActions.filter(wfa => wfa.workflowActionId !== workflowAction.workflowActionId);
		this.availableWorkflowActions = [...this.availableWorkflowActions, workflowAction];
	}

	onSave(): void {
		const originalIds = (this.data.workflowState.possibleActions || [])
			.map((a: WorkflowAction) => a.workflowActionId)
			.sort();
		const currentIds = this.selectedWorkflowActions.map(wfa => wfa.workflowActionId).sort();

		if(JSON.stringify(originalIds) === JSON.stringify(currentIds)) {
			this.dialogRef.close(null);
			return;
		}

		this.dialogRef.close({possibleActionIds: this.selectedWorkflowActions.map(wfa => wfa.workflowActionId)});
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
