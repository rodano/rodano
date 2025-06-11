import {Injectable} from '@angular/core';
import {ChartDTO} from '../model/chart-dto';
import {charts} from '../../widgets/chart/mockData/chart-mock-data';
import {RequestParams} from '../../widgets/chart/chartType';

@Injectable({
	providedIn: 'root'
})

/**
	* For development purposes only. Used temporarily to work with actual data while developing
	* the frontend independently
	*/
export class ChartMockService {
	private temporaryCharts = new Map<string, ChartDTO>();

	/**Get a single chart by ID */
	getChart(chartId: string): ChartDTO | undefined {
		const found = charts.find(chart => chart.chartId === chartId);
		if(found) {
			return found;
		}
		return this.temporaryCharts.get(chartId);
	}

	/**Get chart data dynamically*/
	getData(requestParams: RequestParams): any {
		if(requestParams.scopeModelId === 'COUNTRY' && requestParams.leafScopeModelId === 'PATIENT') {
			return [
				['Switzerland', 145],
				['Germany', 42],
				['France', 98],
				['Italy', 24]
			];
		}
		return [];
	}

	/**Get a list of all charts*/
	listCharts(): ChartDTO[] {
		return [...charts, ...Array.from(this.temporaryCharts.values())];
	}

	/**Create a new chart*/
	createChart(chart: ChartDTO): void {
		charts.push(chart);
		this.temporaryCharts.delete(chart.chartId);
	}

	/**Update an existing chart*/
	updateChart(chartId: string, chart: Partial<ChartDTO>): void {
		const index = charts.findIndex(c => c.chartId === chartId);
		if(index !== -1) {
			charts[index] = {...charts[index], ...chart};
			return;
		}

		const tempChart = this.temporaryCharts.get(chartId);
		if(tempChart) {
			this.temporaryCharts.set(chartId, {...tempChart, ...chart});
		}
	}

	/**Delete a chart*/
	deleteChart(chartId: string): void {
		const index = charts.findIndex(chart => chart.chartId === chartId);
		if(index !== -1) {
			charts.splice(index, 1);
		}
	}

	setTemporaryChart(chart: ChartDTO) {
		this.temporaryCharts.set(chart.chartId, chart);
	}

	clearTemporaryChart() {
		this.temporaryCharts.clear();
	}
}
