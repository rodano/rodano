import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {WorkflowState} from '@core/model/workflow-state';
import {
	WorkflowStateBasicInfoDialogComponent, WorkflowStateBasicInfoDialogData
} from '../../dialogs/workflow-state/workflow-state-basic-info-dialog/workflow-state-basic-info-dialog.component';
import {
	WorkflowStateAggregationDialogComponent
} from '../../dialogs/workflow-state/workflow-state-aggregation-dialog/workflow-state-aggregation-dialog.component';
import {
	WorkflowStateActionDialogComponent
} from '../../dialogs/workflow-state/workflow-state-action-dialog/workflow-state-action-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStateDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		workflowId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowStateBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowId,
				workflowState: null,
				languages
			} as WorkflowStateBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		workflowState: WorkflowState,
		projectId: string,
		workflowId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowStateBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowId,
				workflowState,
				languages
			} as WorkflowStateBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openAggregationDialog(
		workflowState: WorkflowState,
		aggregatedWorkflowStates: {id: string; name: string; code: string}[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowStateAggregationDialogComponent, {
			width: '500px',
			data: {workflowState: JSON.parse(JSON.stringify(workflowState)), aggregatedWorkflowStates}
		});
		return dialogRef.afterClosed();
	}

	openActionsDialog(
		workflowState: WorkflowState,
		availableActions: {id: string; name: string; code: string}[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowStateActionDialogComponent, {
			width: '500px',
			data: {
				workflowState: JSON.parse(JSON.stringify(workflowState)),
				availableWorkflowActions: availableActions
			}
		});
		return dialogRef.afterClosed();
	}
}
