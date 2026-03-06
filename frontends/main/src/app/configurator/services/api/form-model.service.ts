import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {FormModel} from '@core/model/form-model';

@Injectable({
	providedIn: 'root'
})
export class FormModelService {
	constructor(private http: HttpClient) {}

	getFormModels(projectId: string): Observable<FormModel[]> {
		return this.http.get<FormModel[]>(`/api/superuser/configurator/projects/${projectId}/config/form-models`);
	}

	getFormModel(projectId: string, formModelId: string): Observable<FormModel> {
		return this.http.get<FormModel>(`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}`);
	}

	createFormModel(projectId: string, formModel: FormModel): Observable<FormModel> {
		return this.http.post<FormModel>(`/api/superuser/configurator/projects/${projectId}/config/form-models`, formModel);
	}

	updateFormModel(projectId: string, formModelId: string, formModel: FormModel): Observable<FormModel> {
		return this.http.put<FormModel>(`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}`, formModel);
	}

	deleteFormModel(projectId: string, formModelId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/form-models/${formModelId}`);
	}
}
