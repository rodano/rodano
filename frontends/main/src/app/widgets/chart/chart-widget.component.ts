import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ChartUiComponent} from './chart-ui/chart-ui.component';
import {ChartDTO} from '@core/model/chart-dto';
import {ChartService} from '@core/services/chart.service';
import {catchError, of} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {DialogComponent} from '../../configuration/ui-components/dialog/dialog.component';
import {FieldModelCriterion} from '@core/model/field-model-criterion';

@Component({
	selector: 'app-chart-widget',
	standalone: true,
	imports: [CommonModule, ChartUiComponent],
	templateUrl: './chart-widget.component.html',
	styleUrls: ['./chart-widget.component.css']
})
export class ChartWidgetComponent implements OnInit {
	/**
		* Represents a unique chart identifier. As of now determined inside the menu section inside
		* the legacy configuration
		*/
	@Input() id: string;

	/**pointer to chart inside preview to manually trigger re-draw if needed*/
	@ViewChild(ChartUiComponent) chartUiComponent: ChartUiComponent;

	chart: ChartDTO | undefined; //store API response
	errorMessage: string | undefined;

	constructor(
		private chartService: ChartService,
		private dialog: MatDialog
	) {}

	ngOnInit() {
		this.loadChartData();
	}

	loadChartData(): void {
		this.chartService.getChart(this.id).pipe(
			catchError(err => {
				if(err.status === 404) {
					this.errorMessage = `Chart with ID "${this.id}" was not found.`;
				}
				else {
					this.errorMessage = `An error occurred while loading the chart with ID "${this.id}".`;
				}
				return of(null);
			})
		).subscribe(chart => {
			if(chart) {
				this.chart = chart;
			}
		});
	}

	public updateChartBasedOnSelectedRootScopesAndCriteria(selectedRootScopes: string[], criteria: FieldModelCriterion[]): void {
		if(!this.chart || !this.chart.requestParams) {
			this.errorMessage = 'Chart data not yet available.';
			return;
		}

		if(!selectedRootScopes || selectedRootScopes.length === 0) {
			//No specific root scopes selected: Fetch data normally
			this.chartService.getData(this.chart.chartType, this.chart.requestParams).subscribe({
				next: data => {
					if(this.chart) {
						this.chart.data = data;
					}
				},
				error: err => {
					console.error('Error fetching data:', err);
					this.dialog.open(DialogComponent, {
						data: {
							title: 'Error loading benchmark data',
							message: err?.error?.message ?? 'An unknown error occurred.'
						},
						width: '400px',
						disableClose: true
					});
				}
			});
		}
		else {
			//Specific root scopes selected + criteria: Fetch updated benchmarked data
			this.chartService.getBenchmarkData(
				this.chart.chartType,
				this.chart.requestParams,
				selectedRootScopes,
				criteria
			).subscribe({
				next: data => {
					if(this.chart) {
						this.chart.data = data;
					}

					if(this.chartUiComponent) {
						this.chartUiComponent.updateChartManually();
					}
				},
				error: err => {
					console.error('Error fetching benchmark data:', err);
					this.dialog.open(DialogComponent, {
						data: {
							title: 'Error loading benchmark data',
							message: err?.error?.message ?? 'An unknown error occurred.'
						},
						width: '400px',
						disableClose: true
					});
				}
			});
		}
	}
}
