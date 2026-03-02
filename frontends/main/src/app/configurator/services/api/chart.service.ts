import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ChartModel} from '@core/model/chart-model';

@Injectable({
	providedIn: 'root'
})
export class ChartService {
	constructor(private http: HttpClient) {}

	getCharts(projectId: string): Observable<ChartModel[]> {
		return this.http.get<ChartModel[]>(`/api/superuser/configurator/projects/${projectId}/config/charts`, {
			params: {view: 'summary'}
		});
	}

	getChartsFull(projectId: string): Observable<ChartModel[]> {
		return this.http.get<ChartModel[]>(`/api/superuser/configurator/projects/${projectId}/config/charts`, {
			params: {view: 'full'}
		});
	}

	getChart(projectId: string, chartId: string): Observable<ChartModel> {
		return this.http.get<ChartModel>(`/api/superuser/configurator/projects/${projectId}/config/charts/${chartId}`);
	}

	createChart(projectId: string, chart: ChartModel): Observable<ChartModel> {
		return this.http.post<ChartModel>(`/api/superuser/configurator/projects/${projectId}/config/charts`, chart);
	}

	updateChart(projectId: string, chartId: string, chart: ChartModel): Observable<ChartModel> {
		return this.http.put<ChartModel>(`/api/superuser/configurator/projects/${projectId}/config/charts/${chartId}`, chart);
	}

	deleteChart(projectId: string, chartId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/charts/${chartId}`);
	}
}
