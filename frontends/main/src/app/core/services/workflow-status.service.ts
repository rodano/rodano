import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Field} from '../model/field';
import {Form} from '../model/form';
import {Scope} from '../model/scope';
import {Event} from '../model/event';
import {APIService} from './api.service';
import {WorkflowUpdate} from '../model/workflow-update';
import {PagedResultWorkflowStatus} from '../model/paged-result-workflow-status';
import {FormInfo} from '../model/form-info';
import {WorkflowStatusSearch} from '../utilities/search/workflow-status-search';
import {HttpParamsService} from './http-params.service';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {SKIP_ERROR_HANDLING_HEADER} from 'src/app/interceptors/auth.interceptor';

@Injectable({
	providedIn: 'root'
})
export class WorkflowStatusService {
	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpParamsService: HttpParamsService
	) { }

	getFormForWorkflowStatus(statusPk: number): Observable<FormInfo> {
		return this.http.get<FormInfo>(`${this.apiService.getApiUrl()}/workflows/${statusPk}/form`);
	}

	@reviveDates
	createOnField(field: Field, workflowUpdate: WorkflowUpdate): Observable<Field> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows`;

		return this.http.post<Field>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnField(field: Field, workflowStatusPk: number, workflowUpdate: WorkflowUpdate): Observable<Field> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows/${workflowStatusPk}`;

		return this.http.put<Field>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnForm(form: Form, workflowUpdate: WorkflowUpdate): Observable<Form> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;

		return this.http.post<Form>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnForm(form: Form, workflowStatusPk: number, workflowUpdate: WorkflowUpdate): Observable<Form> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows/${workflowStatusPk}`;

		return this.http.put<Form>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnForm(form: Form, workflowId: string, workflowUpdate: WorkflowUpdate): Observable<Form> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;

		return this.http.put<Form>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnEvent(event: Event, workflowUpdate: WorkflowUpdate): Observable<Event> {
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		return this.http.post<Event>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnEvent(event: Event, workflowStatusPk: number, workflowUpdate: WorkflowUpdate): Observable<Event> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows/${workflowStatusPk}`;
		return this.http.put<Event>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnEvent(event: Event, workflowId: string, workflowUpdate: WorkflowUpdate): Observable<Event> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;
		return this.http.put<Event>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnScope(scope: Scope, workflowUpdate: WorkflowUpdate): Observable<Scope> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		return this.http.post<Scope>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnScope(scope: Scope, workflowStatusPk: number, workflowUpdate: WorkflowUpdate): Observable<Scope> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows/${workflowStatusPk}`;
		return this.http.put<Scope>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnScope(scope: Scope, workflowId: string, workflowUpdate: WorkflowUpdate): Observable<Scope> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;
		return this.http.put<Scope>(url, workflowUpdate, {headers});
	}

	@reviveDates
	search(search: WorkflowStatusSearch): Observable<PagedResultWorkflowStatus> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultWorkflowStatus>(`${this.apiService.getApiUrl()}/workflows`, {params});
	}
}
