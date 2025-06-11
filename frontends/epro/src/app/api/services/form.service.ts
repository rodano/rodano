import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Form } from '../model/form';
import { LayoutDTO } from '../model/configuration/layout/layout-dto';
import { APIService } from './api.service';
import { DatasetDTO } from '../model/dataset-dto';
import { WorkflowStatusDTO } from '../model/workflow-status-dto';

@Injectable({
	providedIn: 'root'
})
export class FormService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getForScope(scopePk: number): Observable<Form[]> {
		return this.http.get<Form[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms`);
	}

	getForEvent(scopePk: number, eventPk: number): Observable<Form[]> {
		return this.http.get<Form[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms`);
	}

	getFormForScope(scopePk: number, formId: string): Observable<Form> {
		return this.http.get<Form>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms/${formId}`);
	}

	getFormForEvent(scopePk: number, eventPk: number, formId: string): Observable<Form> {
		return this.http.get<Form>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms/${formId}`);
	}

	getFormData(scopePk: number, eventPk: number, formId: string): Observable<DatasetDTO[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<DatasetDTO[]>(`${url}/forms/${formId}/data`);
	}

	getLayoutGroups(scopePk: number, eventPk: number, formId: string): Observable<LayoutDTO[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<LayoutDTO[]>(`${url}/forms/${formId}/layouts`);
	}

	getContainedWorkflowStatus(scopePk: number, eventPk: number, formPk: number) {
		return this.http.get<WorkflowStatusDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms/${formPk}/contained-workflows`);
	}
}
