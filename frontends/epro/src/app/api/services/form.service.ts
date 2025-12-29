import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Form } from '../model/form-dto';
import { Layout } from '../model/layout-dto';
import { APIService } from './api.service';
import { Dataset } from '../model/dataset-dto';
import { WorkflowStatus } from '../model/workflow-status-dto';

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

	getFormData(scopePk: number, eventPk: number, formId: string): Observable<Dataset[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<Dataset[]>(`${url}/forms/${formId}/data`);
	}

	getLayoutGroups(scopePk: number, eventPk: number, formId: string): Observable<Layout[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<Layout[]>(`${url}/forms/${formId}/layouts`);
	}

	getContainedWorkflowStatus(scopePk: number, eventPk: number, formPk: number) {
		return this.http.get<WorkflowStatus[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms/${formPk}/contained-workflows`);
	}
}
