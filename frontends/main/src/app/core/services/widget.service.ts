import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {WorkflowWidgetDTO} from '../model/workflow-widget-dto';
import {PagedResultWorkflowStatusInfo} from '../model/paged-result-workflow-status-info';
import {SummaryDTO} from '../model/summary-dto';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {HttpParamsService} from './http-params.service';
import {WorkflowWidgetSearch} from '../utilities/search/workflow-widget-search';
import {OverdueWidgetSearch} from '../utilities/search/overdue-widget-search';
import {PagedResultOverdueDTO} from '../model/paged-result-overdue-dto';
import {FieldModelCriterion} from '../model/field-model-criterion';

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

	getWorkflowWidget(widgetId: string): Observable<WorkflowWidgetDTO> {
		return this.http.get<WorkflowWidgetDTO>(`${this.serviceUrl}/workflow/${widgetId}`);
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

	getScopeOverdueWidget(widgetId: string): Observable<WorkflowWidgetDTO> {
		return this.http.get<WorkflowWidgetDTO>(`${this.serviceUrl}/overdue/${widgetId}`);
	}

	@reviveDates
	getScopeOverdue(widgetId: string, search: OverdueWidgetSearch): Observable<PagedResultOverdueDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultOverdueDTO>(`${this.serviceUrl}/overdue/${widgetId}`, {params});
	}

	getScopeOverdueExportUrl(widgetId: string, scopePks?: number[]): string {
		let url = `${this.serviceUrl}/overdue/${widgetId}/export`;
		if(scopePks) {
			url = `${url}?scopePks=${scopePks}`;
		}
		return url;
	}

	//TODO returns StatisticDTO as soon as it is included in the OpenAPI spec
	getHighchartWidgetData(chartId: string, scopePks?: number[], criteria?: FieldModelCriterion[]): Observable<any> {
		let params = new HttpParams();
		if(scopePks) {
			params = params.set('scopePks', scopePks.toString());
		}
		if(criteria) {
			params = params.set('fieldModelCriteria', JSON.stringify(criteria));
		}
		return this.http.get<any>(`${this.serviceUrl}/highchart/${chartId}`, {params});
	}

	getGeneralInfo(): Observable<{title: string; value: string}[]> {
		return this.http.get<{title: string; value: string}[]>(`${this.serviceUrl}/dashboard/general-information`);
	}

	getWorkflowSummary(workflowSummaryId: string, scopePk: number): Observable<SummaryDTO> {
		let params = new HttpParams();
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return this.http.get<SummaryDTO>(`${this.serviceUrl}/workflow-summary/${workflowSummaryId}`, {params});
	}

	getWorkflowSummaryExportUrl(workflowSummaryId: string, scopePk?: number): string {
		return `${this.serviceUrl}/workflow-summary/${workflowSummaryId}/export?scopePk=${scopePk}`;
	}

	getWorkflowSummaryExportHistoricalUrl(workflowSummaryId: string, scopePk?: number): string {
		return `${this.serviceUrl}/workflow-summary/${workflowSummaryId}/export/history?scopePk=${scopePk}`;
	}

	getLockSummary(scopePk: number): Observable<SummaryDTO> {
		let params = new HttpParams();
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return this.http.get<SummaryDTO>(`${this.serviceUrl}/lock-summary`, {params});
	}

	getScopesLockSummaryExportUrl(scopePk: number): string {
		return `${this.serviceUrl}/lock-summary/export/scopes?scopePk=${scopePk}`;
	}

	getEventsLockSummaryExportUrl(scopePk: number): string {
		return `${this.serviceUrl}/lock-summary/export/events?scopePk=${scopePk}`;
	}
}
