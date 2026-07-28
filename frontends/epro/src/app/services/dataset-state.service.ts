import {Injectable} from '@angular/core';
import {BehaviorSubject, forkJoin, identity, Observable, of} from 'rxjs';
import {filter, mergeMap, switchMap, tap} from 'rxjs/operators';
import {Dataset} from '@core/model/dataset';
import {Field} from '@core/model/field';
import {DatasetService} from '@core/services/dataset.service';
import {DatasetUpdate} from '@core/model/dataset-update';
import {FieldUpdate} from '@core/model/field-update';

@Injectable({
	providedIn: 'root'
})
export class DatasetStateService {
	private readonly _currentDatasets = new BehaviorSubject<Dataset[]>([]);

	readonly currentDatasets$: Observable<Dataset[]> = this._currentDatasets.asObservable();

	private get currentDatasets(): Dataset[] {
		return this._currentDatasets.getValue();
	}

	constructor(
		private datasetService: DatasetService
	) { }

	private setCurrentDatasets(newDatasets: Dataset[]): void {
		newDatasets.sort((a, b) => a.pk - b.pk);
		this._currentDatasets.next([...newDatasets]);
	}

	public pullDatasets(scopePk: number, eventPks?: number[]): Observable<Dataset[]> {
		let newDatasets$: Observable<Dataset[]>;
		if(eventPks) {
			if(eventPks.length === 0) {
				return of([]);
			}
			else {
				newDatasets$ = forkJoin(
					eventPks.map(eventPk => this.datasetService.searchOnEvent(scopePk, eventPk))
				).pipe(
					mergeMap(identity)
				);
			}
		}
		else {
			newDatasets$ = this.datasetService.searchOnScope(scopePk);
		}

		return newDatasets$.pipe(
			tap(newDatasets => {
				const newDatasetPks = newDatasets.map(d => d.pk);
				const notUpdatedDatasets = this.currentDatasets.filter(d => !newDatasetPks.includes(d.pk));

				this.setCurrentDatasets([...notUpdatedDatasets, ...newDatasets]);
			})
		);
	}

	public saveDataset(dataset: Dataset): Observable<Dataset> {
		const datasetUpdate = this.convertToDatasetUpdate(dataset);

		const saveCommand = dataset.eventPk ? this.datasetService.saveForEvent(dataset.scopePk, dataset.eventPk, datasetUpdate) : this.datasetService.saveForScope(dataset.scopePk, datasetUpdate);

		return saveCommand.pipe(
			tap(updatedDataset => {
				const filteredDatasets = this.currentDatasets.filter(d => d.pk !== updatedDataset.pk);
				this.setCurrentDatasets([...filteredDatasets, updatedDataset]);
			})
		);
	}

	public saveField(dataset: Dataset, field: Field): Observable<Dataset> {
		const datasetToSend = {...dataset, fields: [field]};
		return this.saveDataset(datasetToSend);
	}

	public getDatasetsForEvent$(eventPk: number): Observable<Dataset[]> {
		return this.currentDatasets$.pipe(
			switchMap(datasets => of(datasets.filter(d => d.eventPk === eventPk)))
		);
	}

	public getDatasetForEvent$(eventPk: number, datasetPk: number): Observable<Dataset> {
		return this.getDatasetsForEvent$(eventPk).pipe(
			mergeMap(identity),
			filter(d => d.pk === datasetPk)
		);
	}

	public getDataset$(datasetPk: number): Observable<Dataset> {
		return this.currentDatasets$.pipe(
			mergeMap(identity),
			filter(d => d.pk === datasetPk)
		);
	}

	public getDatasetsForEvent(eventPk: number): Dataset[] {
		return this.currentDatasets.filter(d => d.eventPk === eventPk);
	}

	public getProgression(dataset: Dataset): number {
		return this.getCompletedFields(dataset).length / dataset.fields.length;
	}

	public getCompletedFields(dataset: Dataset): Field[] {
		return dataset.fields.filter(field => field.value !== undefined && field.value !== null);
	}

	public isStarted(dataset: Dataset): boolean {
		return this.getProgression(dataset) > 0;
	}

	public isCompleted(dataset: Dataset): boolean {
		return this.getProgression(dataset) === 1;
	}

	private convertToDatasetUpdate(dataset: Dataset): DatasetUpdate {
		const fieldUpdates = dataset.fields.map(field => {
			return {
				pk: field.pk,
				modelId: field.modelId,
				value: field.value,
				filePk: field.filePk
			} as FieldUpdate;
		});

		return {
			pk: dataset.pk,
			fields: fieldUpdates
		} as DatasetUpdate;
	}
}
