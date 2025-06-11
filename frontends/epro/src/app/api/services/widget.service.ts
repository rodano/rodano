import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Widget } from '../model/configuration/widget';
import { WidgetChart } from '../model/widget-chart';
import { WidgetWorkflowStatus } from '../model/widget-workflow-status';
import { APIService } from './api.service';
import { PagedResult } from '../model/paged-result';

@Injectable({
	providedIn: 'root'
})
export class WidgetService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getWorkflowWidget(widgetId: string): Observable<Widget> {
		return this.http.get<Widget>(`${this.apiService.getApiUrl()}/widget/workflow/${widgetId}`);
	}

	getWorkflowWidgetData(
		widgetId: string,
		scopePks: number[],
		search: string,
		order: string,
		descendant: boolean,
		offset: number,
		limit: number
	): Observable<PagedResult<WidgetWorkflowStatus>> {
		return this.http.get<PagedResult<WidgetWorkflowStatus>>(
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

	getHighchartWidgetData(chartId: string, scopePks, criteria): Observable<WidgetChart> {
		return this.http.post<WidgetChart>(`${this.apiService.getApiUrl()}/widget/highchart/${chartId}`, criteria, {params: {scopePks}});
	}

}
