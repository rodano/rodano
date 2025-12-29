import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WorkflowWidget } from '../model/workflow-widget-dto';
import { Chart } from '../model/chart-dto';
import { APIService } from './api.service';
import { PagedResultWorkflowStatus } from '../model/paged-result-workflow-status-dto';

@Injectable({
	providedIn: 'root'
})
export class WidgetService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getWorkflowWidget(widgetId: string): Observable<WorkflowWidget> {
		return this.http.get<WorkflowWidget>(`${this.apiService.getApiUrl()}/widget/workflow/${widgetId}`);
	}

	getWorkflowWidgetData(
		widgetId: string,
		scopePks: number[],
		search: string,
		order: string,
		descendant: boolean,
		offset: number,
		limit: number
	): Observable<PagedResultWorkflowStatus> {
		return this.http.get<PagedResultWorkflowStatus>(
			`${this.apiService.getApiUrl()}/widget/workflow/${widgetId}/data`,
			{
				params: {
					scopePks: scopePks.map(pk => pk.toString()),
					search,
					order,
					descendant: descendant.toString(),
					offset: offset.toString(),
					limit: limit.toString()
				}
			}
		);
	}

	getWorkflowWidgetExportUrl(widgetId: string, scopePks: number[]): string {
		return `${this.apiService.getApiUrl()}/widget/workflow/${widgetId}/export?scopePks=${scopePks}`;
	}

	getChart(chartId: string, scopePks: number[], criteria: any): Observable<Chart> {
		return this.http.post<Chart>(`${this.apiService.getApiUrl()}/widget/chart/${chartId}`, criteria, {params: {scopePks}});
	}

}
