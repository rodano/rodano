import {WorkflowState} from '@core/model/workflow-state';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {WorkflowAction} from '@core/model/workflow-action';

export interface WorkflowStateActionDialogData {
	workflowState: WorkflowState;
	availableWorkflowActions: {id: string; name: string; code: string}[];
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
	availableWorkflowActions: {id: string; name: string; code: string}[] = [];
	selectedWorkflowActions: {id: string; name: string; code: string}[] = [];

	constructor(
		private dialogRef: MatDialogRef<WorkflowStateActionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: WorkflowStateActionDialogData
	) {}

	ngOnInit(): void {
		const selectedIds = (this.data.workflowState.possibleActions || []).map((wfa: WorkflowAction) => wfa.workflowActionId);
		this.selectedWorkflowActions = this.data.availableWorkflowActions.filter(wfa => selectedIds.includes(wfa.id));
		this.availableWorkflowActions = this.data.availableWorkflowActions.filter(wfa => !selectedIds.includes(wfa.id));
	}

	onAdd(workflowAction: {id: string; name: string; code: string}): void {
		this.availableWorkflowActions = this.availableWorkflowActions.filter(wfa => wfa.id !== workflowAction.id);
		this.selectedWorkflowActions = [...this.selectedWorkflowActions, workflowAction];
	}

	onRemove(workflowAction: {id: string; name: string; code: string}): void {
		this.selectedWorkflowActions = this.selectedWorkflowActions.filter(wfa => wfa.id !== workflowAction.id);
		this.availableWorkflowActions = [...this.availableWorkflowActions, workflowAction];
	}

	onSave(): void {
		const originalIds = (this.data.workflowState.possibleActions || [])
			.map((a: WorkflowAction) => a.workflowActionId)
			.sort();
		const currentIds = this.selectedWorkflowActions.map(wfa => wfa.id).sort();

		if(JSON.stringify(originalIds) === JSON.stringify(currentIds)) {
			this.dialogRef.close(null);
			return;
		}

		this.dialogRef.close({possibleActionIds: this.selectedWorkflowActions.map(wfa => wfa.id)});
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
