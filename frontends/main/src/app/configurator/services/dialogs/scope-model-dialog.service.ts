import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ScopeModel} from '@core/model/scope-model';
import {catchError, forkJoin, Observable, of} from 'rxjs';
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
	ScopeModelResourcesDialogComponent, WorkflowStateSelection
} from '../../dialogs/scope-model/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {switchMap, map} from 'rxjs/operators';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {LanguageService} from '../language.service';

@Injectable({
	providedIn: 'root'
})
export class ScopeModelDialogService {
	constructor(
		private dialog: MatDialog,
		private datasetModelManager: DatasetModelManagerService,
		private languageService: LanguageService
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
		return this.loadResourcesForDialog(projectId).pipe(
			switchMap(resources => {
				const dialogRef = this.dialog.open(ScopeModelResourcesDialogComponent, {
					width: '500px',
					disableClose: true,
					data: {
						scopeModel,
						availableForms: resources.forms,
						availableDatasets: resources.datasets,
						availableWorkflows: resources.workflows,
						workflowStateSelections
					}
				});

				return dialogRef.afterClosed();
			})
		);
	}

	private loadResourcesForDialog(projectId: string): Observable<{
		datasets: {id: string; name: string}[];
		forms: {id: string; name: string}[];
		workflows: {id: string; name: string; states: {id: string; name: string}[]}[];
	}> {
		return forkJoin({
			datasets: this.datasetModelManager.load(projectId).pipe(
				map(datasetModels => datasetModels.map(dm => ({
					id: dm.datasetModelId,
					name: this.languageService.getDefaultTranslation(dm.shortname) || dm.id
				}))),
				catchError(error => {
					console.error('Error loading dataset models:', error);
					return of([]);
				})
			),
			//TODO: Add form models when ready
			forms: of([]),
			//TODO: Add workflows when ready
			workflows: of([])
		});
	}
}
