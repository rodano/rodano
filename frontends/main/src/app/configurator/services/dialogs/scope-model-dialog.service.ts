import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ScopeModel} from '@core/model/scope-model';
import {Observable} from 'rxjs';
import {
	ScopeModelBasicInfoDialogComponent
} from '../../dialogs/scope-model/scope-model-basic-info-dialog/scope-model-basic-info-dialog.component';
import {
	ScopeModelRelationshipsDialogComponent
} from '../../dialogs/scope-model/scope-model-relationships-dialog/scope-model-relationships-dialog.component';
import {
	ScopeModelDefaultSettingsDialogComponent
} from '../../dialogs/scope-model/scope-model-default-settings-dialog/scope-model-default-settings-dialog.component';
import {
	ScopeModelPatternDialogComponent
} from '../../dialogs/scope-model/scope-model-pattern-dialog/scope-model-pattern-dialog.component';
import {
	ScopeModelResourcesDialogComponent,
	WorkflowStateSelection
} from '../../dialogs/scope-model/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {WorkflowManagerService} from '../manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../manager/workflow-state-manager.service';

@Injectable({
	providedIn: 'root'
})
export class ScopeModelDialogService {
	constructor(
		private dialog: MatDialog,
		private datasetModelManager: DatasetModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
	) {}

	openCreateDialog(
		projectId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(ScopeModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				scopeModel: null,
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		scopeModel: ScopeModel,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(ScopeModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel)),
				languages
			}
		});

		return dialogRef.afterClosed();
	}

	openRelationshipsDialog(
		projectId: string,
		scopeModel: ScopeModel
	): Observable<any> {
		const dialogRef = this.dialog.open(ScopeModelRelationshipsDialogComponent, {
			width: '500px',
			data: {
				projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		return dialogRef.afterClosed();
	}

	openDefaultSettingsDialog(
		projectId: string,
		scopeModel: ScopeModel
	): Observable<any> {
		const dialogRef = this.dialog.open(ScopeModelDefaultSettingsDialogComponent, {
			width: '500px',
			data: {
				projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		return dialogRef.afterClosed();
	}

	openPatternDialog(scopeModel: ScopeModel): Observable<any> {
		const dialogRef = this.dialog.open(ScopeModelPatternDialogComponent, {
			width: '500px',
			data: {
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		projectId: string,
		scopeModel: ScopeModel,
		workflowStateSelections: WorkflowStateSelection[]
	): Observable<any> {
		const availableWorkflows = this.workflowManager.getAll().map(wf => ({
			...wf,
			states: this.workflowStateManager.getAllForWorkflow(wf.workflowId)
		}));
		const dialogRef = this.dialog.open(ScopeModelResourcesDialogComponent, {
			width: '500px',
			data: {
				scopeModel: JSON.parse(JSON.stringify(scopeModel)),
				availableForms: [],
				availableDatasets: this.datasetModelManager.getAll(),
				availableWorkflows,
				workflowStateSelections
			}
		});
		return dialogRef.afterClosed();
	}
}
