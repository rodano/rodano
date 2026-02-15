import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {catchError, forkJoin, Observable, of} from 'rxjs';
import {
	EventModelBasicInfoDialogComponent, EventModelBasicInfoDialogData
} from '../../dialogs/event-model/event-model-basic-info-dialog/event-model-basic-info-dialog.component';
import {EventModel} from '@core/model/event-model';
import {
	EventModelSchedulingDialogComponent
} from '../../dialogs/event-model/event-model-scheduling-dialog/event-model-scheduling-dialog.component';
import {
	EventModelLabelPatternDialogComponent
} from '../../dialogs/event-model/event-model-label-pattern-dialog/event-model-label-pattern-dialog.component';
import {
	EventModelResourcesDialogComponent
} from '../../dialogs/event-model/event-model-resources-dialog/event-model-resources-dialog.component';
import {
	EventModelRelationshipsDialogComponent
} from '../../dialogs/event-model/event-model-relationships-dialog/event-model-relationships-dialog.component';
import {switchMap, map} from 'rxjs/operators';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {LanguageService} from '../language.service';

@Injectable({
	providedIn: 'root'
})
export class EventModelDialogService {
	constructor(
		private dialog: MatDialog,
		private datasetModelManager: DatasetModelManagerService,
		private languageService: LanguageService
	) {}

	openCreateDialog(
		projectId: string,
		scopeModelId: string,
		languages: any[],
		eventGroups: {id: string; name: string; code: string}[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventModelBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				scopeModelId,
				eventModel: null,
				languages,
				eventGroups
			} as EventModelBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		eventModel: EventModel,
		projectId: string,
		scopeModelId: string,
		languages: any[],
		eventGroups: {id: string; name: string; code: string}[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventModelBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				scopeModelId,
				eventModel,
				languages,
				eventGroups
			} as EventModelBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openSchedulingDialog(
		eventModel: EventModel,
		allEventModels: EventModel[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventModelSchedulingDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				eventModel,
				allEventModels
			}
		});

		return dialogRef.afterClosed();
	}

	openRelationshipsDialog(
		eventModel: EventModel,
		availableEventModels: EventModel[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventModelRelationshipsDialogComponent, {
			width: '500px',
			maxHeight: '90vh',
			disableClose: true,
			data: {
				eventModel: JSON.parse(JSON.stringify(eventModel)),
				availableEventModels
			}
		});

		return dialogRef.afterClosed();
	}

	openLabelPatternDialog(eventModel: EventModel): Observable<any> {
		const dialogRef = this.dialog.open(EventModelLabelPatternDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {eventModel}
		});

		return dialogRef.afterClosed();
	}

	openResourcesDialog(
		projectId: string,
		eventModel: EventModel
	): Observable<any> {
		return this.loadResourcesForDialog(projectId).pipe(
			switchMap(resources => {
				const dialogRef = this.dialog.open(EventModelResourcesDialogComponent, {
					width: '500px',
					disableClose: true,
					data: {
						eventModel,
						availableFormModels: resources.forms,
						availableDatasetModels: resources.datasets,
						availableWorkflows: resources.workflows
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
