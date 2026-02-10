import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {DatasetModel} from '@core/model/dataset-model';

@Injectable({
	providedIn: 'root'
})
export class DatasetModelService {
	constructor(private http: HttpClient) {}

	getDatasetModels(projectId: string): Observable<DatasetModel[]> {
		return this.http.get<DatasetModel[]>(`/api/superuser/configurator/projects/${projectId}/config/dataset-models`);
	}

	getDatasetModel(projectId: string, DatasetModelId: string): Observable<DatasetModel> {
		return this.http.get<DatasetModel>(`/api/superuser/configurator/projects/${projectId}/config/dataset-models/${DatasetModelId}`);
	}

	createDatasetModel(projectId: string, datasetModel: DatasetModel): Observable<DatasetModel> {
		return this.http.post<DatasetModel>(`/api/superuser/configurator/projects/${projectId}/config/dataset-models`, datasetModel);
	}

	updateDatasetModel(projectId: string, datasetModelId: string, datasetModel: DatasetModel): Observable<DatasetModel> {
		return this.http.put<DatasetModel>(`/api/superuser/configurator/projects/${projectId}/config/dataset-models/${datasetModelId}`, datasetModel);
	}

	deleteDatasetModel(projectId: string, datasetModelId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/dataset-models/${datasetModelId}`);
	}
}
