import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {Workflow} from '@core/model/workflow';
import {
	WorkflowBasicInfoDialogComponent
} from '../../dialogs/workflow/workflow-basic-info-dialog/workflow-basic-info-dialog.component';
import {
	WorkflowAssignmentDialogComponent
} from '../../dialogs/workflow/workflow-assignment-dialog/workflow-assignment-dialog.component';
import {
	WorkflowMiscDialogComponent
} from '../../dialogs/workflow/workflow-misc-dialog/workflow-misc-dialog.component';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../manager/workflow-action-manager.service';
import {LanguageService} from '../language.service';

@Injectable({
	providedIn: 'root'
})
export class WorkflowDialogService {
	constructor(
		private dialog: MatDialog,
		private languageService: LanguageService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService
	) {}

	openCreateDialog(
		projectId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				workflow: null,
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		workflow: Workflow,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				workflow: JSON.parse(JSON.stringify(workflow)),
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openAssignmentDialog(workflow: Workflow, availableWorkflows: Workflow[]): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowAssignmentDialogComponent, {
			width: '500px',
			data: {
				workflow: JSON.parse(JSON.stringify(workflow)),
				availableWorkflows: availableWorkflows.filter(wf => wf.workflowId !== workflow.workflowId),
				currentWorkflowStates: this.workflowStateManager.getAllForWorkflow(workflow.workflowId),
				currentWorkflowActions: this.workflowActionManager.getAllForWorkflow(workflow.workflowId)
			}
		});
		return dialogRef.afterClosed();
	}

	openMiscDialog(
		workflow: Workflow,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowMiscDialogComponent, {
			width: '500px',
			data: {
				workflow: JSON.parse(JSON.stringify(workflow)),
				languages
			}
		});

		return dialogRef.afterClosed();
	}
}
