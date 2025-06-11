import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {SidebarComponent, SidebarItem} from './sidebar/sidebar.component';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {ChartDTO} from '@core/model/chart-dto';
import {ChartType, DataLabelFormats, DataLabelPositions, GraphType, UnitFormats} from '../widgets/chart/chartType';
import {filter} from 'rxjs';
import {ChartService} from '@core/services/chart.service';
import {DialogComponent} from './ui-components/dialog/dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
	selector: 'app-configuration',
	imports: [
		SidebarComponent,
		RouterOutlet
	],
	templateUrl: './configuration.component.html',
	styleUrl: './configuration.component.css'
})
export class ConfigurationComponent implements OnInit {
	heading = 'Configuration';
	sidebarItems: SidebarItem[] = [
		{icon: 'bar_chart', link: 'chart', text: 'Charts', supportsChildren: true}

		/**If we decide to add other configuration entities in the future, they could be added here like this:
		{icon: 'category', link: 'placeholder', text: 'Scope Models'},
		{icon: 'dataset', link: 'placeholder', text: 'Dataset Models'},
		{icon: 'check_circle', link: 'placeholder', text: 'Validators'},
		{icon: 'assignment', link: 'placeholder', text: 'Form Models'}
			**/
	];

	selectedLink: string | null = null;

	constructor(
		private chartService: ChartService,
		private router: Router,
		private dialog: MatDialog
	) {
	}

	destroyRef = inject(DestroyRef);

	ngOnInit() {
		this.loadCharts();

		this.router.events
			.pipe(
				filter(e => e instanceof NavigationEnd),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(() => {
				this.loadCharts(); //reload sidebar items every time URL changes
			});
	}

	loadCharts(deletedChartId?: string) {
		this.chartService.listCharts().subscribe({
			next: (charts: ChartDTO[]) => {
				//Populate the sidebar
				this.sidebarItems[0].children = charts.map(chart => ({
					icon: 'bar_chart',
					link: `chart/${chart.chartId}`,
					text: chart.title
				}));

				//Auto-navigate if a chart was just deleted
				if(deletedChartId) {
					//Try to go to the next available chart
					const nextChart = charts.find(chart => chart.chartId !== deletedChartId);
					if(nextChart) {
						this.router.navigate(['/configuration', 'chart', nextChart.chartId]);
					}
					else {
						//No charts left → go to overview
						this.router.navigate(['/configuration', 'chart']);
					}
				}
			},
			error: err => {
				console.error(err);
				this.dialog.open(DialogComponent, {
					data: {
						title: 'Error listing charts',
						message: err.error.message
					},
					width: '400px',
					disableClose: true
				});
			}
		});
	}

	handleCreateNewClicked(context: SidebarItem) {
		switch(context.text.toLowerCase()) {
			case 'charts':
				this.createLocalChart();
				this.loadCharts();
				break;
			default:
				console.warn('Unhandled create-new click context:', context);
		}
	}

	createLocalChart() {
		const tempId = `chart-${Date.now()}`;

		const newChart: ChartDTO = {
			chartId: tempId,
			chartType: ChartType.ENROLLMENT_BY_SCOPE,
			title: 'New Chart',
			xLabel: '',
			yLabel: '',
			chartConfig: {
				graphType: GraphType.BAR,
				unitFormat: UnitFormats.ABSOLUTE,
				ignoreNA: false,
				showYAxisLabel: true,
				showXAxisLabel: true,
				showDataLabels: true,
				dataLabelPos: DataLabelPositions.END,
				dataLabelFormat: DataLabelFormats.ONLY_Y,
				showLegend: false,
				showGridlines: false,
				backgroundColor: '#ffffff',
				headerColor: '#000000',
				colors: ['#F0BB78', '#F5ECD5', '#A4B465']
			},
			requestParams: {
				scopeModelId: null,
				leafScopeModelId: null,
				datasetModelId: null,
				fieldModelId: null,
				eventModelId: null,
				stateIds: null,
				workflowId: null,
				showOtherCategory: true,
				ignoreUserRights: false,
				categories: []
			},
			data: {
				series: []
			}
		};

		this.chartService.setTemporaryChart(newChart);

		this.selectedLink = `configuration/chart/${newChart.chartId}`;

		console.log('Selected link after chart creation:', this.selectedLink);

		this.router.navigate(
			['configuration', 'chart', newChart.chartId],
			{queryParams: {new: true}}
		);
	}

	onActivate(component: any) {
		if('chartDeleted' in component) {
			component.chartDeleted.subscribe((deletedId: string) => {
				this.loadCharts(deletedId);
			});
		}
	}
}
