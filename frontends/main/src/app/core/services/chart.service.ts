import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {APIService} from './api.service';
import {BackendChartData, ChartDTO} from '../model/chart-dto';
import {ChartType, RequestParams} from '../../widgets/chart/chartType';
import {SKIP_ERROR_HANDLING_HEADER} from '../../interceptors/auth.interceptor';
import {map} from 'rxjs/operators';
import {FieldModelCriterion} from '@core/model/field-model-criterion';

@Injectable({
	providedIn: 'root'
})

export class ChartService {
	private readonly serviceUrl: string;
	private temporaryCharts = new Map<string, ChartDTO>();

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/charts`;
	}

	/**Get a single chart by ID */
	getChart(chartId: string): Observable<ChartDTO> {
		console.log('Fetching chart with ID', chartId);

		const tempChart = this.temporaryCharts.get(chartId);
		if(tempChart) {
			console.log('Returning temporary chart for ID', chartId);
			return of(tempChart);
		}

		//Otherwise, fetch from the server
		return this.http.get<ChartDTO>(`${this.serviceUrl}/${chartId}`, {
			headers: new HttpHeaders().set(SKIP_ERROR_HANDLING_HEADER, 'true')
		});
	}

	/**Get chart data dynamically*/
	getData(chartType: ChartType, requestParams: RequestParams): Observable<BackendChartData> {
		//Build query params from chartType + requestParams
		let params = new HttpParams()
			.set('chartType', chartType)
			.set('scopeModelId', requestParams.scopeModelId ?? '')
			.set('leafScopeModelId', requestParams.leafScopeModelId ?? '')
			.set('datasetModelId', requestParams.datasetModelId ?? '')
			.set('fieldModelId', requestParams.fieldModelId ?? '')
			.set('showOtherCategory', requestParams.showOtherCategory ?? '');

		if(requestParams.stateIds) {
			requestParams.stateIds.forEach(stateId => {
				params = params.append('stateIds', stateId);
			});
		}

		if(requestParams.eventModelId) {
			params = params.set('eventModelId', requestParams.eventModelId);
		}

		//workflowId:
		if(requestParams.workflowId) {
			params = params.set('workflowId', requestParams.workflowId);
		}

		if(requestParams.categories && requestParams.categories.length > 0) {
			params = params.set('categories', JSON.stringify(requestParams.categories));
		}

		return this.http.get<BackendChartData>(`${this.serviceUrl}/data`, {params});
	}

	/**
		* Retrieves benchmark data based on the provided request parameters and selected root scope IDs.
		*
		* @param chartType
		* @param {RequestParams} requestParams - An object containing request properties such as scopeModelId, leafScopeModelId, datasetModelId, fieldModelId, and a flag to showOtherCategory.
		* @param {string[]} selectedRootScopeIds - An array of strings representing the selected root scope IDs to be included in the query.
		* @param criteria
		* @return {Observable<BackendChartData>} An observable that emits the benchmark data retrieved from the server.
		*/
	getBenchmarkData(
		chartType: ChartType,
		requestParams: RequestParams,
		selectedRootScopeIds: string[],
		criteria: FieldModelCriterion[] = []
	): Observable<BackendChartData> {
		//Build query params from request parameters
		let params = new HttpParams()
			.set('chartType', chartType)
			.set('scopeModelId', requestParams.scopeModelId ?? '')
			.set('leafScopeModelId', requestParams.leafScopeModelId ?? '')
			.set('datasetModelId', requestParams.datasetModelId ?? '')
			.set('fieldModelId', requestParams.fieldModelId ?? '')
			.set('showOtherCategory', requestParams.showOtherCategory ?? '');

		if(requestParams.eventModelId) {
			params = params.set('eventModelId', requestParams.eventModelId);
		}

		//Append categories
		if(requestParams.categories && requestParams.categories.length > 0) {
			params = params.set('categories', JSON.stringify(requestParams.categories));
		}

		//Append the selected root scope IDs
		selectedRootScopeIds.forEach(id => {
			params = params.append('selectedRootScopeIds', id);
		});

		//Append criteria if present
		criteria.forEach((c, index) => {
			params = params
				.append(`criteria[${index}].datasetModelId`, c.datasetModelId ?? '')
				.append(`criteria[${index}].fieldModelId`, c.fieldModelId ?? '')
				.append(`criteria[${index}].operator`, c.operator ?? '')
				.append(`criteria[${index}].value`, c.value ?? '');
		});

		return this.http.get<BackendChartData>(`${this.serviceUrl}/benchmark-data`, {params});
	}

	/**Get a list of all charts*/
	listCharts(): Observable<ChartDTO[]> {
		return this.http.get<ChartDTO[]>(`${this.serviceUrl}`).pipe(
			map(charts => [
				...charts,
				...Array.from(this.temporaryCharts.values())
			])
		);
	}

	/**Create a new chart*/
	createChart(chart: ChartDTO): Observable<ChartDTO> {
		return this.http.post<ChartDTO>(this.serviceUrl, chart);
	}

	/**Update an existing chart*/
	updateChart(chart: ChartDTO): Observable<ChartDTO> {
		return this.http.patch<ChartDTO>(`${this.serviceUrl}/${chart.chartId}`, chart);
	}

	/**Delete a chart*/
	deleteChart(chartId: string): Observable<void> {
		return this.http.delete<void>(`${this.serviceUrl}/${chartId}`);
	}

	/**
		* When the user starts the creation process of a new chart object it is set until it is properly saved
		* @param chart
		*/
	setTemporaryChart(chart: ChartDTO) {
		this.temporaryCharts.set(chart.chartId, chart);
	}

	/**
		* Function which is called when the user decides to discard his newly created chart
		*/
	clearTemporaryChart() {
		this.temporaryCharts.clear();
	}
}
