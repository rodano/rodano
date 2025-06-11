import { Injectable } from '@angular/core';
import { BehaviorSubject, forkJoin, identity, Observable, of } from 'rxjs';
import { filter, mergeMap, switchMap, tap } from 'rxjs/operators';
import { DatasetDTO } from '../api/model/dataset-dto';
import { FieldDTO } from '../api/model/field-dto';
import { DatasetService } from '../api/services/dataset.service';
import { DatasetUpdateDTO } from '../api/model/dataset-update-dto';
import { FieldUpdateDTO } from '../api/model/field-update-dto';

@Injectable({
	providedIn: 'root'
})
export class DatasetStateService {

	private readonly _currentDatasets = new BehaviorSubject<DatasetDTO[]>([]);

	readonly currentDatasets$: Observable<DatasetDTO[]> = this._currentDatasets.asObservable();

	private get currentDatasets(): DatasetDTO[] {
		return this._currentDatasets.getValue();
	}

	constructor(
		private datasetService: DatasetService
	) { }

	private setCurrentDatasets(newDatasets: DatasetDTO[]): void {
		newDatasets.sort((a, b) => a.pk - b.pk);
		this._currentDatasets.next([...newDatasets]);
	}

	public pullDatasets(scopePk: number, eventPks?: number[]): Observable<DatasetDTO[]> {
		let newDatasets$: Observable<DatasetDTO[]>;
		if(eventPks) {
			if(eventPks.length === 0) {
				return of([]);
			} else {
				newDatasets$ = forkJoin(
					eventPks.map(eventPk => this.datasetService.getDatasetsForEvent(scopePk, eventPk))
				).pipe(
					mergeMap(identity)
				);
			}
		} else {
			newDatasets$ = this.datasetService.getDatasetsForScope(scopePk);
		}

		return newDatasets$.pipe(
			tap(newDatasets => {
				const newDatasetPks = newDatasets.map(d => d.pk);
				const notUpdatedDatasets = this.currentDatasets.filter(d => !newDatasetPks.includes(d.pk));

				this.setCurrentDatasets([...notUpdatedDatasets, ...newDatasets]);
			})
		);
	}

	public saveDataset(dataset: DatasetDTO): Observable<DatasetDTO> {
		const datasetUpdate = this.convertToDatasetUpdate(dataset);

		return this.datasetService.save(
			datasetUpdate,
			dataset.scopePk,
			dataset.eventPk
		).pipe(
			tap(updatedDataset => {
				const filteredDatasets = this.currentDatasets.filter(d => d.pk !== updatedDataset.pk);
				this.setCurrentDatasets([...filteredDatasets, updatedDataset]);
			})
		);
	}

	public saveField(dataset: DatasetDTO, field: FieldDTO): Observable<DatasetDTO> {
		const datasetToSend = {...dataset, fields: [field]};
		return this.saveDataset(datasetToSend);
	}

	public getDatasetsForEvent$(eventPk: number): Observable<DatasetDTO[]> {
		return this.currentDatasets$.pipe(
			switchMap(datasets => of(datasets.filter(d => d.eventPk === eventPk)))
		);
	}

	public getDatasetForEvent$(eventPk: number, datasetPk: number): Observable<DatasetDTO> {
		return this.getDatasetsForEvent$(eventPk).pipe(
			mergeMap(identity),
			filter(d => d.pk === datasetPk)
		);
	}

	public getDataset$(datasetPk: number): Observable<DatasetDTO> {
		return this.currentDatasets$.pipe(
			mergeMap(identity),
			filter(d => d.pk === datasetPk)
		);
	}

	public getDatasetsForEvent(eventPk: number): DatasetDTO[] {
		return this.currentDatasets.filter(d => d.eventPk === eventPk);
	}

	public getProgression(dataset: DatasetDTO): number {
		return this.getCompletedFields(dataset).length / dataset.fields.length;
	}

	public getCompletedFields(dataset: DatasetDTO): FieldDTO[] {
		return dataset.fields.filter(field => field.value !== undefined && field.value !== null);
	}

	public isStarted(dataset: DatasetDTO): boolean {
		return this.getProgression(dataset) > 0;
	}

	public isCompleted(dataset: DatasetDTO): boolean {
		return this.getProgression(dataset) === 1;
	}

	private convertToDatasetUpdate(dataset: DatasetDTO): DatasetUpdateDTO {
		const fieldUpdates = dataset.fields.map(field => {
			return {
				datasetPk: field.datasetPk,
				datasetId: field.datasetId,
				pk: field.pk,
				modelId: field.modelId,
				value: field.value,
				filePk: field.filePk
			} as FieldUpdateDTO;
		});

		return {
			pk: dataset.pk,
			fields: fieldUpdates
		} as DatasetUpdateDTO;
	}
}
