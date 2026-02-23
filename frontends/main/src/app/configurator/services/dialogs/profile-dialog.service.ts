import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {
	ProfileBasicInfoDialogComponent,
	ProfileBasicInfoDialogData
} from '../../dialogs/profile/profile-basic-info-dialog/profile-basic-info-dialog.component';
import {Profile} from '@core/model/profile';
import {WorkflowManagerService} from '../manager/workflow-manager.service';

@Injectable({
	providedIn: 'root'
})
export class ProfileDialogService {
	constructor(
		private dialog: MatDialog,
		private workflowManager: WorkflowManagerService
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const availableWorkflows = this.workflowManager.getAll();
		const dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent> = this.dialog.open(
			ProfileBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, profile: null, languages, availableWorkflows} as ProfileBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, profile: Profile, languages: ProjectLanguage[]): Observable<any> {
		const availableWorkflows = this.workflowManager.getAll();
		const dialogRef: MatDialogRef<ProfileBasicInfoDialogComponent> = this.dialog.open(
			ProfileBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, profile: JSON.parse(JSON.stringify(profile)), languages, availableWorkflows} as ProfileBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}
}
