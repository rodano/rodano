import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {
	WorkflowWidgetBasicInfoDialogComponent,
	WorkflowWidgetBasicInfoDialogData
} from '../../dialogs/workflow-widget/workflow-widget-basic-info-dialog/workflow-widget-basic-info-dialog.component';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {
	WorkflowWidgetWorkflowDialogComponent, WorkflowWidgetWorkflowDialogData
} from '../../dialogs/workflow-widget/workflow-widget-workflow-dialog/workflow-widget-workflow-dialog.component';
import {
	WorkflowWidgetColumnDialogComponent,
	WorkflowWidgetColumnDialogData
} from '../../dialogs/workflow-widget/workflow-widget-column-dialog/workflow-widget-column-dialog.component';
import {WorkflowWidgetColumnConfig} from '@core/model/workflow-widget-column-config';

@Injectable({
	providedIn: 'root'
})
export class WorkflowWidgetDialogService {
	constructor(
		private dialog: MatDialog,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<WorkflowWidgetBasicInfoDialogComponent> = this.dialog.open(
			WorkflowWidgetBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, workflowWidget: null, languages} as WorkflowWidgetBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, workflowWidget: WorkflowWidgetConfig, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<WorkflowWidgetBasicInfoDialogComponent> = this.dialog.open(
			WorkflowWidgetBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, workflowWidget: JSON.parse(JSON.stringify(workflowWidget)), languages} as WorkflowWidgetBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openWorkflowDialog(workflowWidget: WorkflowWidgetConfig, projectId: string): Observable<any> {
		return new Observable(observer => {
			const openDialog = () => {
				const availableWorkflows = this.workflowManager.getAll().map(wf => ({
					...wf,
					states: this.workflowStateManager.getAllForWorkflow(wf.workflowId)
				}));

				const dialogRef = this.dialog.open(WorkflowWidgetWorkflowDialogComponent, {
					width: '500px',
					disableClose: true,
					data: {workflowWidget, availableWorkflows} as WorkflowWidgetWorkflowDialogData
				});
				dialogRef.afterClosed().subscribe(result => {
					observer.next(result);
					observer.complete();
				});
			};

			if(this.workflowStateManager.isLoaded()) {
				openDialog();
			}
			else {
				this.workflowStateManager.load(projectId).subscribe(() => openDialog());
			}
		});
	}

	openColumnsDialog(columns: WorkflowWidgetColumnConfig[], languages: ProjectLanguage[], workflowEntity: string): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowWidgetColumnDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				columns: JSON.parse(JSON.stringify(columns)),
				languages,
				workflowEntity
			} as WorkflowWidgetColumnDialogData
		});
		return dialogRef.afterClosed();
	}
}
