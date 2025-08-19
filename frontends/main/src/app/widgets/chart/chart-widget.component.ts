import {Component, Input, OnChanges, ViewChild} from '@angular/core';
import {ChartDTO} from '@core/model/chart-dto';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {WidgetService} from '@core/services/widget.service';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {BaseChartDirective} from 'ng2-charts';
import {ChartConfiguration, ChartData, TooltipItem} from 'chart.js';
import {ChartType} from '@core/model/chart-type';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {ChartDatasetPointObjectObject} from '@core/model/chart-dataset-point-object-object';
import 'chartjs-adapter-date-fns';
import {forkJoin} from 'rxjs';

@Component({
	selector: 'app-chart-widget',
	templateUrl: './chart-widget.component.html',
	styleUrls: ['./chart-widget.component.css'],
	imports: [BaseChartDirective]
})
export class ChartWidgetComponent implements OnChanges {
	private static DEFAULT_OPTIONS = {
		responsive: true,
		maintainAspectRatio: true,
		animation: false,
		plugins: {
			tooltip: {
				displayColors: false
			}
		},
		scales: {
			x: {
				//display the x-axis title
				title: {
					display: true
				}
			},
			y: {
				//display the y-axis title
				title: {
					display: true
				},
				ticks: {
					precision: 0
				},
				beginAtZero: true
			}
		}
	};

	private static DATE_FORMATTER = new DateUTCPipe();

	@Input() id: string;
	@Input() scopes?: ScopeDTO[] | ScopeMiniDTO[];
	@Input() criteria?: FieldModelCriterion[];

	@ViewChild(BaseChartDirective) chartDirective?: BaseChartDirective;

	chart: ChartDTO;
	studyColor: string;
	data: ChartData<any>;
	options: ChartConfiguration<any>['options'];

	graphType: string;

	constructor(
		private configurationService: ConfigurationService,
		private widgetService: WidgetService
	) {}

	ngOnChanges(): void {
		const scopePks = this.scopes?.map(s => s.pk) || [];
		forkJoin([
			this.configurationService.getStudy(),
			this.widgetService.getChart(this.id, scopePks, this.criteria)
		]).subscribe(([study, chart]) => {
			this.studyColor = study.color;
			this.chart = chart;
			this.graphType = this.getGraphType(this.chart.model.type);
			this.data = {datasets: this.chart.datasets};
			//clone the default options
			this.options = structuredClone(ChartWidgetComponent.DEFAULT_OPTIONS);

			//hide dataset labels when there is only one dataset
			if(this.data.datasets.length === 1) {
				//hide dataset labels at the top
				this.options.plugins.legend = {
					display: false
				};
			}

			//configure tooltip content
			this.options.plugins.tooltip.callbacks = {
				title: (context: TooltipItem<any>[]) => {
					const raw = context[0].raw as ChartDatasetPointObjectObject;
					const x = raw.x;
					return x instanceof Date ? ChartWidgetComponent.DATE_FORMATTER.transform(x) : undefined;
				}
			};

			//set specific options for enrollment by scope
			if(this.chart.model.type === ChartType.ENROLLMENT_BY_SCOPE) {
				//for enrollment by scope, bars are horizontal to leave more room for scopes
				this.options.indexAxis = 'y';
				//as the chart is pivoted, data and legend labels must be inverted
				this.data.datasets.forEach(dataset => {
					dataset.data.forEach((point: any) => {
						const x = point.x;
						point.x = point.y;
						point.y = x;
					});
				});
				this.options.scales.x.title.text = this.chart.model.legendY;
				this.options.scales.y.title.text = this.chart.model.legendX;
			}
			else {
				this.options.scales.x.title.text = this.chart.model.legendX;
				this.options.scales.y.title.text = this.chart.model.legendY;
			}

			//configure x-scale for chart based on time
			if([ChartType.ENROLLMENT, ChartType.WORKFLOW_STATUS].includes(chart.model.type)) {
				this.options.scales.x.type = 'time';
				this.options.scales.x.ticks = {
					callback: function (value: number) {
						const date = new Date(value);
						return ChartWidgetComponent.DATE_FORMATTER.transform(date);
					}
				};
			}

			//manage colors
			const colors = this.chart.model.colors.length ? this.chart.model.colors : [this.studyColor];
			this.data.datasets.forEach((dataset, index) => {
				const color = colors[index % colors.length];
				//parsing is done (basically transformation of date) is already done in the API service
				//dataset.parsing = false;
				dataset.backgroundColor = color;
				dataset.borderColor = color;
			});
		});
	}

	//transform statistics data into percent data series
	/*const datasets = data.series.map(series => {
		const valuesMap = new Map(series.values.map(([x, y]) => [x === '' ? 'N/A' : x, y]));
		let dataPoints = allX.map(x => valuesMap.get(x) ?? 0);

		//Percentage logic (optional)
		if(options.percentage) {
			const total = dataPoints.reduce((sum, val) => sum + val, 0);
			dataPoints = dataPoints.map(val =>
				total ? +(100 * val / total).toFixed(2) : 0
			);
		}

		return {
			label: series.label,
			data: dataPoints
		};
	});*/

	getGraphType(type: ChartType): string {
		switch(type) {
			case ChartType.ENROLLMENT_BY_SCOPE:
			case ChartType.STATISTICS:
				return 'bar';
			case ChartType.ENROLLMENT:
			case ChartType.WORKFLOW_STATUS:
				return 'line';
			default:
				throw new Error(`Unknown chart type: ${type}`);
		}
	}
}
