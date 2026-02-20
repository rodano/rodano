import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {WorkflowAction} from '@core/model/workflow-action';
import {
	WorkflowActionBasicInfoDialogComponent, WorkflowActionBasicInfoDialogData
} from '../../dialogs/workflow-action/workflow-action-basic-info-dialog/workflow-action-basic-info-dialog.component';
import {
	WorkflowActionDocumentationDialogComponent
} from '../../dialogs/workflow-action/workflow-action-documentation-dialog/workflow-action-documentation-dialog.component';
import {
	WorkflowActionSignatureDialogComponent
} from '../../dialogs/workflow-action/workflow-action-signature-dialog/workflow-action-signature-dialog.component';

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

	openDocumentationDialog(
		workflowAction: WorkflowAction,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowActionDocumentationDialogComponent, {
			width: '500px',
			data: {workflowAction: JSON.parse(JSON.stringify(workflowAction)), languages}
		});
		return dialogRef.afterClosed();
	}

	openSignatureDialog(
		workflowAction: WorkflowAction,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowActionSignatureDialogComponent, {
			width: '600px',
			data: {workflowAction: JSON.parse(JSON.stringify(workflowAction)), languages}
		});
		return dialogRef.afterClosed();
	}
}
