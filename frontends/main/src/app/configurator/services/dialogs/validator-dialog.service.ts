import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {Validator} from '@core/model/validator';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {
	ValidatorBasicInfoDialogComponent, ValidatorBasicInfoDialogData
} from '../../dialogs/validator/validator-basic-info-dialog/validator-basic-info-dialog.component';
import {
	ValidatorWorkflowDialogComponent, ValidatorWorkflowDialogData
} from '../../dialogs/validator/validator-workflow-dialog/validator-workflow-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ValidatorDialogService {
	constructor(
		private dialog: MatDialog,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent> = this.dialog.open(
			ValidatorBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, validator: null, languages} as ValidatorBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, validator: Validator, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ValidatorBasicInfoDialogComponent> = this.dialog.open(
			ValidatorBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, validator: JSON.parse(JSON.stringify(validator)), languages} as ValidatorBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openWorkflowDialog(validator: Validator, projectId: string): Observable<any> {
		return new Observable(observer => {
			const openDialog = () => {
				const availableWorkflows = this.workflowManager.getAll().map(wf => ({
					...wf,
					states: this.workflowStateManager.getAllForWorkflow(wf.workflowId)
				}));

				const dialogRef = this.dialog.open(ValidatorWorkflowDialogComponent, {
					width: '500px',
					disableClose: true,
					data: {validator, availableWorkflows} as ValidatorWorkflowDialogData
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
