import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {LanguageService} from '../language.service';
import {Workflow} from '@core/model/workflow';
import {
	WorkflowBasicInfoDialogComponent
} from '../../dialogs/workflow/workflow-basic-info-dialog/workflow-basic-info-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class WorkflowDialogService {
	constructor(
		private dialog: MatDialog,
		private languageService: LanguageService
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
}
