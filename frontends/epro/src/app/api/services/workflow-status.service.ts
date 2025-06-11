import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WorkflowAction } from '../model/configuration/workflow-action';
import { FieldDTO } from '../model/field-dto';
import { Form } from '../model/form';
import { FormValueDTO } from '../model/form-value-dto';
import { ScopeDTO } from '../model/scope-dto';
import { EventDTO } from '../model/event-dto';
import { APIService } from './api.service';
import { WorkflowActionResponse } from '../model/workflow-action-response';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStatusService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getFormForWorkflowStatus(statusPk: number): Observable<FormValueDTO> {
		return this.http.get<FormValueDTO>(`${this.apiService.getApiUrl()}/workflows/${statusPk}/form`);
	}

	initWorkflowForField(field: FieldDTO, actionParams: WorkflowActionResponse): Observable<FieldDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.fieldPk}/workflows`;

		return this.http.post<FieldDTO>(url, actionParams);
	}

	executeActionForField(workflowStatusPk: number, field: FieldDTO, actionParams: WorkflowActionResponse): Observable<FieldDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.fieldPk}/workflows/${workflowStatusPk}`;

		return this.http.put<FieldDTO>(url, actionParams);
	}

	initWorkflowForForm(form: Form, actionParams: WorkflowActionResponse): Observable<Form> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;

		return this.http.post<Form>(url, actionParams);
	}

	executeActionForForm(form: Form, action: WorkflowAction, actionParams: WorkflowActionResponse): Observable<Form> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;
		if(action.wsPk) {
			url = `${url}/${action.wsPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.id}`;
		}

		return this.http.put<Form>(url, actionParams);
	}

	initWorkflowForEvent(event: EventDTO, actionParams: WorkflowActionResponse): Observable<EventDTO> {
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		return this.http.post<EventDTO>(url, actionParams);
	}

	executeActionForEvent(event: EventDTO, action: WorkflowAction, actionParams: WorkflowActionResponse): Observable<EventDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		if(action.wsPk) {
			url = `${url}/${action.wsPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.id}`;
		}

		return this.http.put<EventDTO>(url, actionParams);
	}

	initWorkflowForScope(scope: ScopeDTO, actionParams: WorkflowActionResponse): Observable<ScopeDTO> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		return this.http.post<ScopeDTO>(url, actionParams);
	}

	executeActionForScope(scope: ScopeDTO, action: WorkflowAction, actionParams: WorkflowActionResponse): Observable<ScopeDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		if(action.wsPk) {
			url = `${url}/${action.wsPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.id}`;
		}

		return this.http.put<ScopeDTO>(url, actionParams);
	}
}
