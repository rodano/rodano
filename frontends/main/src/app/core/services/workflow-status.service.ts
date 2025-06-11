import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FieldDTO} from '../model/field-dto';
import {FormDTO} from '../model/form-dto';
import {ScopeDTO} from '../model/scope-dto';
import {EventDTO} from '../model/event-dto';
import {APIService} from './api.service';
import {WorkflowUpdateDTO} from '../model/workflow-update-dto';
import {PagedResultWorkflowStatusDTO} from '../model/paged-result-workflow-status-dto';
import {FormInfoDTO} from '../model/form-info-dto';
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

	getFormForWorkflowStatus(statusPk: number): Observable<FormInfoDTO> {
		return this.http.get<FormInfoDTO>(`${this.apiService.getApiUrl()}/workflows/${statusPk}/form`);
	}

	@reviveDates
	createOnField(field: FieldDTO, workflowUpdate: WorkflowUpdateDTO): Observable<FieldDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows`;

		return this.http.post<FieldDTO>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnField(field: FieldDTO, workflowStatusPk: number, workflowUpdate: WorkflowUpdateDTO): Observable<FieldDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${field.scopePk}`;
		if(field.eventPk) {
			url = `${url}/events/${field.eventPk}`;
		}
		url = `${url}/datasets/${field.datasetPk}/fields/${field.pk}/workflows/${workflowStatusPk}`;

		return this.http.put<FieldDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnForm(form: FormDTO, workflowUpdate: WorkflowUpdateDTO): Observable<FormDTO> {
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows`;

		return this.http.post<FormDTO>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnForm(form: FormDTO, workflowStatusPk: number, workflowUpdate: WorkflowUpdateDTO): Observable<FormDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows/${workflowStatusPk}`;

		return this.http.put<FormDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnForm(form: FormDTO, workflowId: string, workflowUpdate: WorkflowUpdateDTO): Observable<FormDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		let url = `${this.apiService.getApiUrl()}/scopes/${form.scopePk}`;
		if(form.eventPk) {
			url = `${url}/events/${form.eventPk}`;
		}
		url = `${url}/forms/${form.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;

		return this.http.put<FormDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnEvent(event: EventDTO, workflowUpdate: WorkflowUpdateDTO): Observable<EventDTO> {
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows`;
		return this.http.post<EventDTO>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnEvent(event: EventDTO, workflowStatusPk: number, workflowUpdate: WorkflowUpdateDTO): Observable<EventDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows/${workflowStatusPk}`;
		return this.http.put<EventDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnEvent(event: EventDTO, workflowId: string, workflowUpdate: WorkflowUpdateDTO): Observable<EventDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${event.scopePk}/events/${event.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;
		return this.http.put<EventDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	createOnScope(scope: ScopeDTO, workflowUpdate: WorkflowUpdateDTO): Observable<ScopeDTO> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows`;
		return this.http.post<ScopeDTO>(url, workflowUpdate);
	}

	@reviveDates
	executeActionOnScope(scope: ScopeDTO, workflowStatusPk: number, workflowUpdate: WorkflowUpdateDTO): Observable<ScopeDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows/${workflowStatusPk}`;
		return this.http.put<ScopeDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	executeAggregateActionOnScope(scope: ScopeDTO, workflowId: string, workflowUpdate: WorkflowUpdateDTO): Observable<ScopeDTO> {
		//for actions that require a signature, skip handling of "Unauthorized" errors
		const headers = new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, '401');
		const url = `${this.apiService.getApiUrl()}/scopes/${scope.pk}/workflows/${workflowId}/${workflowUpdate.actionId}`;
		return this.http.put<ScopeDTO>(url, workflowUpdate, {headers});
	}

	@reviveDates
	search(search: WorkflowStatusSearch): Observable<PagedResultWorkflowStatusDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultWorkflowStatusDTO>(`${this.apiService.getApiUrl()}/workflows`, {params});
	}
}
