import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {WorkflowAction} from '@core/model/workflow-action';
import {
	WorkflowActionBasicInfoDialogComponent, WorkflowActionBasicInfoDialogData
} from '../../dialogs/workflow-action/workflow-action-basic-info-dialog/workflow-action-basic-info-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class WorkflowActionDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		workflowId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowActionBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowId,
				workflowAction: null,
				languages
			} as WorkflowActionBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openEditDialog(
		projectId: string,
		workflowId: string,
		workflowAction: WorkflowAction,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowActionBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowId,
				workflowAction: JSON.parse(JSON.stringify(workflowAction)),
				languages
			} as WorkflowActionBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}
}
