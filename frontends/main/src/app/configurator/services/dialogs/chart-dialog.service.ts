import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {ChartModel} from '@core/model/chart-model';
import {
	ChartBasicInfoDialogComponent,
	ChartBasicInfoDialogData
} from '../../dialogs/chart/chart-basic-info-dialog/chart-basic-info-dialog.component';
import {
	ChartSettingsDialogComponent, ChartSettingsDialogData
} from '../../dialogs/chart/chart-settings-dialog/chart-settings-dialog.component';
import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {
	ChartStatisticsDialogComponent, ChartStatisticsDialogData
} from '../../dialogs/chart/chart-statistics-dialog/chart-statistics-dialog.component';
import {
	ChartWorkflowStatusDialogComponent,
	ChartWorkflowStatusDialogData
} from '../../dialogs/chart/chart-workflow-status-dialog/chart-workflow-status-dialog.component';
import {
	ChartEnrollmentDialogComponent,
	ChartEnrollmentDialogData
} from '../../dialogs/chart/chart-enrollment-dialog/chart-enrollment-dialog.component';
import {
	ChartEnrollmentByScopeDialogComponent,
	ChartEnrollmentByScopeDialogData
} from '../../dialogs/chart/chart-enrollment-by-scope-dialog/chart-enrollment-by-scope-dialog.component';
import {FieldModelManagerService} from '../manager/field-model-manager.service';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {ChartColorsDialogComponent, ChartColorsDialogData} from '../../dialogs/chart/chart-colors-dialog/chart-colors-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class ChartDialogService {
	constructor(
		private scopeModelManager: ScopeModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(projectId: string, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ChartBasicInfoDialogComponent> = this.dialog.open(
			ChartBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, chart: null, languages} as ChartBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(projectId: string, chart: ChartModel, languages: ProjectLanguage[]): Observable<any> {
		const dialogRef: MatDialogRef<ChartBasicInfoDialogComponent> = this.dialog.open(
			ChartBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {projectId, chart: JSON.parse(JSON.stringify(chart)), languages} as ChartBasicInfoDialogData
			}
		);
		return dialogRef.afterClosed();
	}

	openSettingsDialog(chart: ChartModel, languages: ProjectLanguage[]): Observable<any> {
		return this.dialog.open(ChartSettingsDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				chart: JSON.parse(JSON.stringify(chart)),
				languages,
				availableScopeModels: this.scopeModelManager.getAll()
			} as ChartSettingsDialogData
		}).afterClosed();
	}

	openColorsDialog(chart: ChartModel): Observable<any> {
		return this.dialog.open(ChartColorsDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {chart: JSON.parse(JSON.stringify(chart))} as ChartColorsDialogData
		}).afterClosed();
	}

	openEnrollmentByScopeDialog(chart: ChartModel): Observable<any> {
		return this.dialog.open(ChartEnrollmentByScopeDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				chart: JSON.parse(JSON.stringify(chart)),
				availableScopeModels: this.scopeModelManager.getAll()
			} as ChartEnrollmentByScopeDialogData
		}).afterClosed();
	}

	openEnrollmentDialog(chart: ChartModel): Observable<any> {
		return this.dialog.open(ChartEnrollmentDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				chart: JSON.parse(JSON.stringify(chart)),
				availableWorkflows: this.workflowManager.getAll()
			} as ChartEnrollmentDialogData
		}).afterClosed();
	}

	openWorkflowStatusDialog(chart: ChartModel): Observable<any> {
		return this.dialog.open(ChartWorkflowStatusDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				chart: JSON.parse(JSON.stringify(chart)),
				availableWorkflows: this.workflowManager.getAll(),
				availableWorkflowStates: this.workflowStateManager.getAll()
			} as ChartWorkflowStatusDialogData
		}).afterClosed();
	}

	openStatisticsDialog(chart: ChartModel, languages: ProjectLanguage[]): Observable<any> {
		const leafScopeModel = chart.leafScopeModelId
			? this.scopeModelManager.getById(chart.leafScopeModelId)
			: null;

		return this.dialog.open(ChartStatisticsDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				chart: JSON.parse(JSON.stringify(chart)),
				availableFieldModels: this.fieldModelManager.getAll(),
				availableDatasetModels: this.datasetModelManager.getAll(),
				leafScopeModel: leafScopeModel ?? null,
				languages
			} as ChartStatisticsDialogData
		}).afterClosed();
	}
}
