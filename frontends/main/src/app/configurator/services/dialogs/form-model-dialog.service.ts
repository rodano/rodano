import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {FormModel} from '@core/model/form-model';
import {Observable} from 'rxjs';
import {
	FormModelBasicInfoDialogComponent
} from '../../dialogs/form-model/form-model-basic-info-dialog/form-model-basic-info-dialog.component';
import {
	FormModelResourcesDialogComponent
} from '../../dialogs/form-model/form-model-resources-dialog/form-model-resources-dialog.component';
import {WorkflowManagerService} from '../manager/workflow-manager.service';

@Injectable({
	providedIn: 'root'
})
export class FormModelDialogService {
	constructor(
		private dialog: MatDialog,
		private workflowManager: WorkflowManagerService
	) {}

	openCreateDialog(
		projectId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FormModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				formModel: null,
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		formModel: FormModel,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(FormModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				formModel: JSON.parse(JSON.stringify(formModel)),
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		projectId: string,
		formModel: FormModel
	): Observable<any> {
		const availableWorkflows = this.workflowManager.getAll();
		const dialogRef = this.dialog.open(FormModelResourcesDialogComponent, {
			width: '500px',
			data: {
				formModel: JSON.parse(JSON.stringify(formModel)),
				availableWorkflows
			}
		});

		return dialogRef.afterClosed();
	}
}
