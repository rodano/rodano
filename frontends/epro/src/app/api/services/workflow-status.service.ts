import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WorkflowAction } from '../model/workflow-action-dto';
import { Field } from '../model/field-dto';
import { Form } from '../model/form-dto';
import { FormInfo } from '../model/form-info-dto';
import { Scope } from '../model/scope-dto';
import { Event } from '../model/event-dto';
import { APIService } from './api.service';
import { WorkflowUpdate } from '../model/workflow-update-dto';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStatusService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getFormForWorkflowStatus(statusPk: number): Observable<FormInfo> {
		return this.http.get<FormInfo>(`${this.apiService.getApiUrl()}/workflows/${statusPk}/form`);
	}

	initWorkflowForField(field: Field, actionParams: WorkflowUpdate): Observable<Field> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows`;

		return this.http.post<Field>(url, actionParams);
	}

	executeActionForField(workflowStatusPk: number, field: Field, actionParams: WorkflowUpdate): Observable<Field> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows/${workflowStatusPk}`;

		return this.http.put<Field>(url, actionParams);
	}

	initWorkflowForForm(form: Form, actionParams: WorkflowUpdate): Observable<Form> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;

		return this.http.post<Form>(url, actionParams);
	}

	executeActionForForm(form: Form, action: WorkflowAction, actionParams: WorkflowUpdate, workflowStatusPk: number | undefined): Observable<Form> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;
		if(workflowStatusPk) {
			url = `${url}/${workflowStatusPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.workflowActionId}`;
		}

		return this.http.put<Form>(url, actionParams);
	}

	initWorkflowForEvent(event: Event, actionParams: WorkflowUpdate): Observable<Event> {
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		return this.http.post<Event>(url, actionParams);
	}

	executeActionForEvent(event: Event, action: WorkflowAction, actionParams: WorkflowUpdate, workflowStatusPk: number | undefined): Observable<Event> {
		let url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		if(workflowStatusPk) {
			url = `${url}/${workflowStatusPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.workflowActionId}`;
		}

		return this.http.put<Event>(url, actionParams);
	}

	initWorkflowForScope(scope: Scope, actionParams: WorkflowUpdate): Observable<Scope> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		return this.http.post<Scope>(url, actionParams);
	}

	executeActionForScope(scope: Scope, action: WorkflowAction, actionParams: WorkflowUpdate, workflowStatusPk: number | undefined): Observable<Scope> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		if(workflowStatusPk) {
			url = `${url}/${workflowStatusPk}`;
		}
		else {
			url = `${url}/${action.workflowId}/${action.workflowActionId}`;
		}

		return this.http.put<Scope>(url, actionParams);
	}
}
