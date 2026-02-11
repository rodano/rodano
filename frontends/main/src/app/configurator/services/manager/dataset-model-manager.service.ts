import {Injectable} from '@angular/core';
import {DatasetModel} from '@core/model/dataset-model';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {DatasetModelService} from '../api/dataset-model.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
	providedIn: 'root'
})
export class DatasetModelManagerService {
	private tracker: EntityModificationTracker<DatasetModel>;

	constructor(private datasetModelService: DatasetModelService) {
		this.tracker = new EntityModificationTracker<DatasetModel>(
			dm => dm.datasetModelId,
			['id', 'multiple', 'exportable', 'master', 'exportOrder', 'family', 'expandedLabelPattern', 'collapsedLabelPattern'],
			['shortname', 'longname', 'description'],
			[]
		);
	}

	load(projectId: string): Observable<DatasetModel[]> {
		return this.datasetModelService.getDatasetModels(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				return models;
			})
		);
	}

	create(projectId: string, datasetModel: DatasetModel): Observable<DatasetModel> {
		return this.datasetModelService.createDatasetModel(projectId, datasetModel).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(datasetModel: DatasetModel): void {
		this.tracker.updateEntity(datasetModel);
	}

	delete(projectId: string, datasetModelId: string): Observable<void> {
		return this.datasetModelService.deleteDatasetModel(projectId, datasetModelId).pipe(
			map(() => {
				this.tracker.removeEntity(datasetModelId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): DatasetModel[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): DatasetModel[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): DatasetModel | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	isFieldModified(id: string, field: string): boolean {
		return this.tracker.isFieldModified(id, field);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, datasetModelId: string, datasetModel: DatasetModel): Observable<DatasetModel> {
		return this.datasetModelService.updateDatasetModel(projectId, datasetModelId, datasetModel);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
