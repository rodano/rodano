import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {
	WorkflowSummaryBasicInfoDialogComponent,
	WorkflowSummaryBasicInfoDialogData
} from '../../dialogs/workflow-summary/workflow-summary-basic-info-dialog/workflow-summary-basic-info-dialog.component';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {
	WorkflowSummaryWorkflowDialogComponent
} from '../../dialogs/workflow-summary/workflow-summary-workflow-dialog/workflow-summary-workflow-dialog.component';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {
	WorkflowSummaryFilterDialogComponent,
	WorkflowSummaryFilterDialogData
} from '../../dialogs/workflow-summary/workflow-summary-filter-dialog/workflow-summary-filter-dialog.component';
import {EventModelManagerService} from '../manager/event-model-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {
	WorkflowSummaryColumnDialogComponent, WorkflowSummaryColumnDialogData
} from '../../dialogs/workflow-summary/workflow-summary-column-dialog/workflow-summary-column-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class WorkflowSummaryDialogService {
	constructor(
		private dialog: MatDialog,
		private scopeModelManager: ScopeModelManagerService,
		private workflowManager: WorkflowManagerService,
		private eventModelManager: EventModelManagerService,
		private workflowStateManager: WorkflowStateManagerService
	) {}

	openCreateDialog(
		projectId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowSummaryBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowSummary: null,
				languages,
				scopeModels: this.scopeModelManager.getAll()
			} as WorkflowSummaryBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		workflowSummary: WorkflowSummary,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowSummaryBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				workflowSummary,
				languages,
				scopeModels: this.scopeModelManager.getAll()
			} as WorkflowSummaryBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openWorkflowDialog(
		projectId: string,
		workflowSummary: WorkflowSummary
	): Observable<any> {
		const dialogRef = this.dialog.open(WorkflowSummaryWorkflowDialogComponent, {
			width: '500px',
			data: {
				workflowSummary: JSON.parse(JSON.stringify(workflowSummary)),
				availableWorkflows: this.workflowManager.getAll()
			}
		});
		return dialogRef.afterClosed();
	}

	openFilterDialog(projectId: string, workflowSummary: WorkflowSummary): Observable<any> {
		return this.dialog.open(WorkflowSummaryFilterDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				workflowSummary: JSON.parse(JSON.stringify(workflowSummary)),
				eventModels: this.eventModelManager.getAll()
			} as WorkflowSummaryFilterDialogData
		}).afterClosed();
	}

	openColumnsDialog(
		projectId: string,
		workflowSummary: WorkflowSummary,
		languages: ProjectLanguage[]
	): Observable<any> {
		return new Observable(observer => {
			const openDialog = () => {
				const availableStates = (workflowSummary.workflowIds ?? [])
					.flatMap(workflowId => this.workflowStateManager.getAllForWorkflow(workflowId));

				const dialogRef = this.dialog.open(WorkflowSummaryColumnDialogComponent, {
					width: '500px',
					disableClose: true,
					data: {
						columns: JSON.parse(JSON.stringify(workflowSummary.columns ?? [])),
						languages,
						availableStates
					} as WorkflowSummaryColumnDialogData
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
}
