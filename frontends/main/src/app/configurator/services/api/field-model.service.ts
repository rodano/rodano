import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FieldModel} from '@core/model/field-model';

@Injectable({
	providedIn: 'root'
})
export class FieldModelService {
	constructor(private http: HttpClient) {}

	getFieldModels(projectId: string): Observable<FieldModel[]> {
		return this.http.get<FieldModel[]>(`/api/superuser/configurator/projects/${projectId}/config/field-models`);
	}

	getFieldModel(projectId: string, fieldModelId: string): Observable<FieldModel> {
		return this.http.get<FieldModel>(`/api/superuser/configurator/projects/${projectId}/config/field-models/${fieldModelId}`);
	}

	createFieldModel(projectId: string, fieldModel: FieldModel): Observable<FieldModel> {
		return this.http.post<FieldModel>(`/api/superuser/configurator/projects/${projectId}/config/field-models`, fieldModel);
	}

	updateFieldModel(projectId: string, fieldModelId: string, fieldModel: FieldModel): Observable<FieldModel> {
		return this.http.put<FieldModel>(`/api/superuser/configurator/projects/${projectId}/config/field-models/${fieldModelId}`, fieldModel);
	}

	deleteFieldModel(projectId: string, fieldModelId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/field-models/${fieldModelId}`);
	}
}
