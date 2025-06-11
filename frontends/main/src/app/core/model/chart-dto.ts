import {
	Category,
	ChartType,
	DataLabelFormats,
	DataLabelPositions,
	GraphType,
	UnitFormats
} from "../../widgets/chart/chartType";

export type SeriesValue = [string | number, number];

export interface Series {
	label: string;
	values: SeriesValue[];
}

export interface BackendChartData {
	series: Series[];
}

export interface ChartDTO {
	chartId: string;
	chartType: ChartType;
	title: string;
	xLabel: string;
	yLabel: string;
	chartConfig: {
		graphType: GraphType;
		unitFormat: UnitFormats;
		ignoreNA: boolean;
		showYAxisLabel: boolean;
		showXAxisLabel: boolean;
		showDataLabels: boolean;
		dataLabelPos: DataLabelPositions;
		dataLabelFormat: DataLabelFormats;
		showLegend: boolean;
		showGridlines: boolean;
		backgroundColor: string;
		headerColor: string;
		colors: string[];
	}
	requestParams: {
		scopeModelId: string | null;
		leafScopeModelId: string | null;
		datasetModelId: string | null;
		fieldModelId: string | null;
		eventModelId: string | null;
		stateIds: string[] | null;
		workflowId: string | null;
		showOtherCategory: boolean | null;
		ignoreUserRights: boolean | null;
		categories: Category[] | null;
	}
	data?: BackendChartData;
}

export function printChartDTO(chart: ChartDTO): void {
	console.log('ChartDTO Debug:');
	console.log('-----------------------------');
	console.log(`chartId: ${chart.chartId}`);
	console.log(`chartType: ${chart.chartType}`);
	console.log(`title: ${chart.title}`);
	console.log(`xLabel: ${chart.xLabel}`);
	console.log(`yLabel: ${chart.yLabel}`);
	console.log('chartConfig:', chart.chartConfig);
	console.log('requestParams:', chart.requestParams);

	if (chart.data) {
		console.log('data.series:');
		chart.data.series.forEach((s, i) => {
			console.log(`  [${i}] label: ${s.label}`);
			console.log(`      values:`, s.values);
		});
	} else {
		console.log('data: undefined');
	}
	console.log('-----------------------------');
}
