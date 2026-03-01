import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {FieldModelManagerService} from '../manager/field-model-manager.service';
import {Report} from '@core/model/report';
import {
	ReportBasicInfoDialogComponent,
	ReportBasicInfoDialogData
} from '../../dialogs/report/report-basic-info-dialog/report-basic-info-dialog.component';
import {
	ReportResourcesDialogComponent
} from '../../dialogs/report/report-resources-dialog/report-resources-dialog.component';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';

@Injectable({
	providedIn: 'root'
})
export class ReportDialogService {
	constructor(
		private workflowManager: WorkflowManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ReportBasicInfoDialogComponent> = this.dialog.open(
			ReportBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					report: null,
					languages,
					workflows: this.workflowManager.getAll(),
					datasetModels: this.datasetModelManager.getAll()
				} as ReportBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, report: Report, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ReportBasicInfoDialogComponent> = this.dialog.open(
			ReportBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					report: JSON.parse(JSON.stringify(report)),
					languages,
					workflows: this.workflowManager.getAll(),
					datasetModels: this.datasetModelManager.getAll()
				} as ReportBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		projectId: string,
		report: Report
	): Observable<any> {
		const dialogRef = this.dialog.open(ReportResourcesDialogComponent, {
			width: '500px',
			data: {
				report: JSON.parse(JSON.stringify(report)),
				availableFieldModels: this.fieldModelManager.getAll()
			}
		});
		return dialogRef.afterClosed();
	}
}
