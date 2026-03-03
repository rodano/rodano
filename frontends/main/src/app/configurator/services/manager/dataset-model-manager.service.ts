import {Injectable} from '@angular/core';
import {DatasetModel} from '@core/model/dataset-model';
import {DatasetModelService} from '../api/dataset-model.service';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class DatasetModelManagerService extends BaseManagerService<DatasetModel> {
	constructor(private datasetModelService: DatasetModelService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (dm: DatasetModel) => dm.datasetModelId;}
	protected getSimpleFields(): (keyof DatasetModel)[] {
		return ['id', 'multiple', 'exportable', 'master', 'exportOrder', 'family', 'expandedLabelPattern', 'collapsedLabelPattern'];
	}

	protected getTranslationFields(): (keyof DatasetModel)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof DatasetModel)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<DatasetModel[]> {
		return this.datasetModelService.getDatasetModels(projectId);
	}

	protected createEntity(projectId: string, entity: DatasetModel): Observable<DatasetModel> {
		return this.datasetModelService.createDatasetModel(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.datasetModelService.deleteDatasetModel(projectId, id);
	}
}
