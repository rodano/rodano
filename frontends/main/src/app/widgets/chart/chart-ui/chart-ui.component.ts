import {Component, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BaseChartDirective} from 'ng2-charts';
import {Chart, ChartConfiguration, ChartData} from 'chart.js';
import ChartDataLabels, {Context} from 'chartjs-plugin-datalabels';
import {ChartDTO} from '@core/model/chart-dto';
import {ChartType, DataLabelFormats, UnitFormats} from '../chartType';
import {ChartDataAdapter} from '../chartDataAdapter';
import 'chartjs-adapter-luxon';

Chart.register(ChartDataLabels);

@Component({
	selector: 'app-chart-ui',
	imports: [CommonModule, BaseChartDirective],
	templateUrl: './chart-ui.component.html',
	styleUrl: './chart-ui.component.css'
})
export class ChartUiComponent implements OnInit, OnChanges {
	/**
		* Input DTO received from the parent containing all chart configuration and data.
		*/
	@Input() chart: ChartDTO;

	/**
		* Reference to the Chart.js chart instance.
		*/
	@ViewChild(BaseChartDirective) chartDirective?: BaseChartDirective;

	/**
		* Controls visibility of the chart (used to re-render the chart).
		*/
	chartVisible = true;

	data: ChartData<any> = {labels: [], datasets: []};
	options: ChartConfiguration<any>['options'] = {
		responsive: true,
		maintainAspectRatio: true,
		animation: false,
		plugins: {
			legend: {display: false}
		},
		scales: {
			x: {title: {display: true, text: ''}},
			y: {
				title: {display: true, text: ''},
				ticks: {
					stepSize: 1,
					precision: 0
				}
			}
		}
	};

	/**
		* Called once after component initialization to process chart data.
		*/
	ngOnInit() {
		if(this.chart.data) {
			this.processChartData();
		}
	}

	ngOnChanges() {
		this.processChartData();
	}

	/**
		* Transforms ChartDTO into Chart.js-compatible data and option objects.
		* Includes dynamic handling of axis types, legends, data labels, colors, etc.
		*/
	processChartData(): void {
		const config = this.chart.chartConfig;
		const isTimelineChart = [ChartType.ENROLLMENT_STATUS, ChartType.WORKFLOW_STATUS]
			.includes(this.chart.chartType);

		const graphType = this.resolvedGraphType;

		const {datasets, labels} = ChartDataAdapter.preprocessChartDatasets(this.chart.data, {
			timeline: isTimelineChart,
			percentage: config.unitFormat === UnitFormats.PERCENTAGE,
			ignoreNA: config.ignoreNA
		});

		const palette = this.chart.chartConfig.colors || ['#6a98af', '#e8712f', '#80b13c', '#b62e6c'];

		datasets.forEach((ds, i) => {
			if(graphType === 'line' || graphType === 'area') {
				ds.borderColor = palette[i % palette.length];
				ds.backgroundColor = (graphType === 'area')
					? `${palette[i % palette.length]}77`
					: 'transparent';
				ds.pointBackgroundColor = palette[i % palette.length];
				ds.pointBorderColor = palette[i % palette.length];
				ds.borderWidth = 2;
				ds.fill = (graphType === 'area');
				ds.pointRadius = 0;
			}
			else {
				//Assign a color per bar/pie slice
				ds.backgroundColor = ds.data.map((_: any, dataIdx: number) =>
					palette[dataIdx % palette.length]
				);
				delete ds.borderWidth;
				delete ds.strokeStyle;
				delete ds.pointBackgroundColor;
				delete ds.pointBorderColor;
				delete ds.fill;
			}
		});

		this.data = {
			labels,
			datasets
		};

		this.options.scales = this.configureScales(isTimelineChart);

		this.options.plugins = this.configureDataLabels();

		this.options.layout = this.configureLayout();

		if(this.chart.chartConfig.graphType === 'horizontalBar') {
			this.options.indexAxis = 'y';
		}
		else {
			this.options.indexAxis = 'x';
		}

		this.chartDirective?.update();
	}

	public updateChartManually() {
		this.processChartData();
	}

	get resolvedGraphType(): string {
		if(!this.chart?.chartConfig?.graphType) {
			return 'bar';
		}

		if(this.chart.chartConfig.graphType === 'area') {
			return 'line';
		}

		if(this.chart.chartConfig.graphType === 'horizontalBar') {
			return 'bar';
		}

		return this.chart.chartConfig.graphType;
	}

	private configureScales(isTimelineChart: boolean): ChartConfiguration<any>['options']['scales'] {
		const config = this.chart.chartConfig;
		const isHorizontal = config.graphType === 'horizontalBar';

		if(this.resolvedGraphType === 'pie' || this.resolvedGraphType === 'doughnut') {
			return undefined;
		}

		if(isTimelineChart) {
			return {
				x: {
					type: 'time',
					title: {display: !!this.chart.xLabel, text: this.chart.xLabel},
					adapters: {
						date: {
							locale: 'en'
						}
					},
					grid: {display: config.showGridlines},
					ticks: {display: config.showXAxisLabel}
				},
				y: {
					title: {display: !!this.chart.yLabel, text: this.chart.yLabel},
					ticks: {stepSize: 1, precision: 0, display: config.showYAxisLabel},
					grid: {display: config.showGridlines}
				}
			};
		}

		return {
			[isHorizontal ? 'y' : 'x']: {
				type: 'category',
				title: {display: !!this.chart.xLabel, text: this.chart.xLabel},
				grid: {display: config.showGridlines},
				ticks: {display: config.showXAxisLabel}
			},
			[isHorizontal ? 'x' : 'y']: {
				title: {display: !!this.chart.yLabel, text: this.chart.yLabel},
				ticks: {stepSize: 1, precision: 0, display: config.showYAxisLabel},
				grid: {display: config.showGridlines}
			}
		};
	}

	private configureDataLabels(): ChartConfiguration<any>['options']['plugins'] {
		const config = this.chart.chartConfig;

		const formatter = (value: any, context: Context): string => {
			const x = context.chart.data.labels?.[context.dataIndex];
			const y = context.dataset.data[context.dataIndex];

			switch(config.dataLabelFormat) {
				case DataLabelFormats.ONLY_X:
					return `${x}`;
				case DataLabelFormats.ONLY_Y:
					return `${y}${config.unitFormat === UnitFormats.PERCENTAGE ? '%' : ''}`;
				case DataLabelFormats.X_AND_Y:
				default:
					return `${x}, ${y}${config.unitFormat === UnitFormats.PERCENTAGE ? '%' : ''}`;
			}
		};

		return {
			legend: {
				display: config.showLegend,
				labels: {
					usePointStyle: true,
					pointStyle: 'rect'
				}
			},
			tooltip: {
				displayColors: false,
				callbacks: {
					label: (context: {label: string; formattedValue: string}) => {
						const chartType = this.chart.chartType;

						if(chartType === ChartType.WORKFLOW_STATUS) {
							return this.chart.title;
						}

						if(chartType === ChartType.ENROLLMENT_BY_SCOPE) {
							return `${context.label}`;
						}

						//Default
						const label = context.label ?? '';
						const values = context.formattedValue ?? '';
						return `${label}: ${values}`;
					}
				}
			},
			datalabels: config.showDataLabels
				? {
					backgroundColor: 'rgba(255, 255, 255, 0.7)',
					borderRadius: 4,
					padding: 4,
					clip: false,
					anchor: config.dataLabelPos || 'end',
					align: config.dataLabelPos || 'end',
					formatter,
					color: '#000'
				}
				: {display: false}
		};
	}

	private configureLayout(): ChartConfiguration<any>['options']['layout'] {
		const config = this.chart.chartConfig;
		return {
			padding: {
				top: (config.dataLabelPos === 'end' && !config.showLegend) ? 25 : 0,
				bottom: (config.graphType === 'doughnut' || config.graphType === 'pie') ? 20 : 0,
				right: 20
			}
		};
	}
}
