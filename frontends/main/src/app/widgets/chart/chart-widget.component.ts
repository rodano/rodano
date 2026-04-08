import {ChangeDetectionStrategy, Component, ViewChild, effect, input, signal} from '@angular/core';
import {Chart} from '@core/model/chart';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {WidgetService} from '@core/services/widget.service';
import {Scope} from '@core/model/scope';
import {ScopeMini} from '@core/model/scope-mini';
import {ConfigurationService} from '@core/services/configuration.service';
import {BaseChartDirective} from 'ng2-charts';
import {ChartConfiguration, ChartData, TooltipItem} from 'chart.js';
import {ChartType} from '@core/model/chart-type';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {ChartDatasetPointObjectObject} from '@core/model/chart-dataset-point-object-object';
import 'chartjs-adapter-date-fns';
import {forkJoin} from 'rxjs';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-chart-widget',
	templateUrl: './chart-widget.component.html',
	styleUrls: ['./chart-widget.component.css'],
	imports: [BaseChartDirective]
})
export class ChartWidgetComponent {
	private static DEFAULT_OPTIONS = {
		responsive: true,
		maintainAspectRatio: true,
		animation: false,
		plugins: {
			tooltip: {
				displayColors: false
			}
		},
		pointStyle: false,
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

	readonly id = input.required<string>();
	readonly scopes = input<Scope[] | ScopeMini[]>();
	readonly criteria = input<FieldModelCriterion[]>();

	@ViewChild(BaseChartDirective) chartDirective?: BaseChartDirective;

	readonly chart = signal<Chart | undefined>(undefined);
	studyColor: string;
	readonly data = signal<ChartData<any> | undefined>(undefined);
	readonly options = signal<ChartConfiguration<any>['options'] | undefined>(undefined);

	readonly graphType = signal('');

	constructor(
		private configurationService: ConfigurationService,
		private widgetService: WidgetService
	) {
		effect(() => {
			const scopePks = this.scopes()?.map(s => s.pk) || [];
			forkJoin([
				this.configurationService.getStudy(),
				this.widgetService.getChart(this.id(), scopePks, this.criteria())
			]).subscribe(([study, chart]) => {
				this.studyColor = study.color;
				const graphType = this.getGraphType(chart.model.type);
				const data: ChartData<any> = {datasets: chart.datasets};
				//clone the default options
				const options = structuredClone(ChartWidgetComponent.DEFAULT_OPTIONS) as ChartConfiguration<any>['options'];

				//hide dataset labels when there is only one dataset
				if(data.datasets.length === 1) {
					//hide dataset labels at the top
					options.plugins.legend = {
						display: false
					};
				}

				//configure tooltip content
				options.plugins.tooltip.callbacks = {
					title: (context: TooltipItem<any>[]) => {
						const raw = context[0].raw as ChartDatasetPointObjectObject;
						const x = raw.x;
						return x instanceof Date ? ChartWidgetComponent.DATE_FORMATTER.transform(x) : undefined;
					}
				};

				//set specific options for enrollment by scope
				if(chart.model.type === ChartType.ENROLLMENT_BY_SCOPE) {
					//for enrollment by scope, bars are horizontal to leave more room for scopes
					options.indexAxis = 'y';
					//as the chart is pivoted, data and legend labels must be inverted
					data.datasets.forEach(dataset => {
						dataset.data.forEach((point: any) => {
							const x = point.x;
							point.x = point.y;
							point.y = x;
						});
					});
					options.scales.x.title.text = chart.model.legendY;
					options.scales.y.title.text = chart.model.legendX;
				}
				else {
					options.scales.x.title.text = chart.model.legendX;
					options.scales.y.title.text = chart.model.legendY;
				}

				//configure x-scale for chart based on time
				if([ChartType.ENROLLMENT, ChartType.WORKFLOW_STATUS].includes(chart.model.type)) {
					options.scales.x.type = 'time';
					options.interaction = {
						mode: 'nearest',
						axis: 'xy',
						intersect: false
					};
					options.scales.x.ticks = {
						callback: function (value: number) {
							const date = new Date(value);
							return ChartWidgetComponent.DATE_FORMATTER.transform(date);
						}
					};
				}

				//manage colors
				const colors = chart.model.colors.length ? chart.model.colors : [this.studyColor];
				data.datasets.forEach((dataset, index) => {
					const color = colors[index % colors.length];
					//parsing is done (basically transformation of date) is already done in the API service
					//dataset.parsing = false;
					dataset.backgroundColor = color;
					dataset.borderColor = color;
				});

				this.chart.set(chart);
				this.graphType.set(graphType);
				this.data.set(data);
				this.options.set(options);
			});
		});
	}

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
