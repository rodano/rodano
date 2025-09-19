import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {WorkflowWidget} from '../model/workflow-widget';
import {PagedResultWorkflowStatusInfo} from '../model/paged-result-workflow-status-info';
import {Summary} from '../model/summary';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {HttpParamsService} from './http-params.service';
import {WorkflowWidgetSearch} from '../utilities/search/workflow-widget-search';
import {OverdueWidgetSearch} from '../utilities/search/overdue-widget-search';
import {PagedResultOverdue} from '../model/paged-result-overdue';
import {FieldModelCriterion} from '../model/field-model-criterion';
import {Chart} from '@core/model/chart';

@Injectable({
	providedIn: 'root'
})
export class WidgetService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private httpParamsService: HttpParamsService,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/widget`;
	}

	getWorkflowWidget(widgetId: string): Observable<WorkflowWidget> {
		return this.http.get<WorkflowWidget>(`${this.serviceUrl}/workflow/${widgetId}`);
	}

	@reviveDates
	getWorkflowWidgetData(widgetId: string, search: WorkflowWidgetSearch): Observable<PagedResultWorkflowStatusInfo> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultWorkflowStatusInfo>(`${this.serviceUrl}/workflow/${widgetId}/data`, {params});
	}

	getWorkflowWidgetExportUrl(widgetId: string, scopePks?: number[]): string {
		let url = `${this.serviceUrl}/workflow/${widgetId}/export`;
		if(scopePks) {
			url = `${url}?scopePks=${scopePks}`;
		}
		return url;
	}

	getScopeOverdueWidget(widgetId: string): Observable<WorkflowWidget> {
		return this.http.get<WorkflowWidget>(`${this.serviceUrl}/overdue/${widgetId}`);
	}

	@reviveDates
	getScopeOverdue(widgetId: string, search: OverdueWidgetSearch): Observable<PagedResultOverdue> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultOverdue>(`${this.serviceUrl}/overdue/${widgetId}`, {params});
	}

	getScopeOverdueExportUrl(widgetId: string, scopePks?: number[]): string {
		let url = `${this.serviceUrl}/overdue/${widgetId}/export`;
		if(scopePks) {
			url = `${url}?scopePks=${scopePks}`;
		}
		return url;
	}

	@reviveDates
	getChart(chartId: string, scopePks?: number[], criteria?: FieldModelCriterion[]): Observable<Chart> {
		let params = new HttpParams();
		if(scopePks) {
			params = params.set('scopePks', scopePks.toString());
		}
		if(criteria) {
			params = params.set('criteria', JSON.stringify(criteria));
		}
		return this.http.get<Chart>(`${this.serviceUrl}/chart/${chartId}`, {params});
	}

	getGeneralInfo(): Observable<{title: string; value: string}[]> {
		return this.http.get<{title: string; value: string}[]>(`${this.serviceUrl}/dashboard/general-information`);
	}

	getWorkflowSummary(workflowSummaryId: string, scopePk: number): Observable<Summary> {
		let params = new HttpParams();
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return this.http.get<Summary>(`${this.serviceUrl}/workflow-summary/${workflowSummaryId}`, {params});
	}

	getWorkflowSummaryExportUrl(workflowSummaryId: string, scopePk?: number): string {
		return `${this.serviceUrl}/workflow-summary/${workflowSummaryId}/export?scopePk=${scopePk}`;
	}

	getWorkflowSummaryExportHistoricalUrl(workflowSummaryId: string, scopePk?: number): string {
		return `${this.serviceUrl}/workflow-summary/${workflowSummaryId}/export/history?scopePk=${scopePk}`;
	}

	getLockSummary(scopePk: number): Observable<Summary> {
		let params = new HttpParams();
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return this.http.get<Summary>(`${this.serviceUrl}/lock-summary`, {params});
	}

	getScopesLockSummaryExportUrl(scopePk: number): string {
		return `${this.serviceUrl}/lock-summary/export/scopes?scopePk=${scopePk}`;
	}

	getEventsLockSummaryExportUrl(scopePk: number): string {
		return `${this.serviceUrl}/lock-summary/export/events?scopePk=${scopePk}`;
	}
}
