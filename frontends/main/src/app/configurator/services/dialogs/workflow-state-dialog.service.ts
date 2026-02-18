import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {LanguageService} from '../language.service';
import {WorkflowState} from '@core/model/workflow-state';
import {
	WorkflowStateBasicInfoDialogComponent, WorkflowStateBasicInfoDialogData
} from '../../dialogs/workflow-state/workflow-state-basic-info-dialog/workflow-state-basic-info-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStateDialogService {
	constructor(
		private dialog: MatDialog,
		private languageService: LanguageService
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
}
