import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, of, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ChartModel} from '@core/model/chart-model';
import {ChartManagerService} from '../../services/manager/chart-manager.service';
import {ChartDialogService} from '../../services/dialogs/chart-dialog.service';
import {ChartDetailComponent} from '../chart-detail/chart-detail.component';
import {MatTooltip} from '@angular/material/tooltip';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';

@Component({
	selector: 'app-chart-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ChartDetailComponent,
		EmptyStateComponent,
		MatTooltip
	],
	templateUrl: './chart-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ChartListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() chartsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() chartContextChanged = new EventEmitter<{
		charts: any[];
		selectedChartId: string | null;
	}>();

	selectedChart: ChartModel | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public chartManager: ChartManagerService,
		public languageService: LanguageService,
		private fieldModelManager: FieldModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private chartDialogService: ChartDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadCharts();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.charts.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('chart-')) {
				const chartId = nodeId.replace('chart-', '');
				const chart = this.charts.find(c => c.chartId === chartId);
				if(chart) {
					this.selectedChart = chart;
				}
			}
			else if(nodeId === 'charts') {
				this.selectedChart = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedChart) {
			return 0;
		}
		return 2;
	}

	get charts(): ChartModel[] {
		return this.chartManager.getAll();
	}

	get modifiedChartIds(): Set<string> {
		return this.chartManager.getModifiedIds();
	}

	get originalCharts(): ChartModel[] {
		return this.chartManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.chartManager.getModificationCount();
	}

	loadCharts(): void {
		this.loading = true;
		forkJoin({
			charts: this.chartManager.loadFull(this.projectId),
			fieldModels: this.fieldModelManager.isLoaded() ? of(null) : this.fieldModelManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded() ? of(null) : this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({charts}) => {
				if(this.selectedChart) {
					this.selectedChart = charts.find(
						c => c.chartId === this.selectedChart!.chartId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading charts:', error);
				this.snackBar.open('Failed to load charts', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectChart(chart: ChartModel): void {
		if(this.selectedChart?.chartId === chart.chartId) {
			this.clearSelection();
		}
		else {
			this.selectChart(chart);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedChart = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('charts');
	}

	private selectChart(chart: ChartModel): void {
		const previousChartId = this.selectedChart?.chartId;
		this.selectedChart = chart;

		if(previousChartId !== chart.chartId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`chart-${chart.chartId}`);
	}

	isSelected(chart: ChartModel): boolean {
		return this.selectedChart?.chartId === chart.chartId;
	}

	onCreateChart(): void {
		this.chartDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: ChartModel | null) => {
			if(result) {
				this.chartManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Chart created', 'Close', {duration: 2000});
						this.loadCharts();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating chart', error);
						this.snackBar.open('Failed to create chart', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onChartUpdated(updatedChart: ChartModel): void {
		this.selectedChart = this.chartManager.getById(updatedChart.chartId) || null;
		this.emitModificationChange();
	}

	onChartDeleted(chartId: string): void {
		const chart = this.charts.find(c => c.chartId === chartId);
		if(!chart) {
			return;
		}
		this.performDelete(chart);
	}

	private performDelete(chart: ChartModel): void {
		this.chartManager.delete(this.projectId, chart.chartId).subscribe({
			next: () => {
				this.snackBar.open('Chart deleted', 'Close', {duration: 2000});

				if(this.selectedChart?.chartId === chart.chartId) {
					this.clearSelection();
				}

				this.loadCharts();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting chart', error);
				this.snackBar.open('Failed to delete chart', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.chartsChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.chartContextChanged.emit({
			charts: [...this.charts],
			selectedChartId: this.selectedChart?.chartId || null
		});
	}

	isModified(chartId: string): boolean {
		return this.chartManager.isModified(chartId);
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.scopeModelManager.getById(id));
	}

	getFieldModelLabel(fieldModelId: string): string {
		return this.languageService.getLabelById(fieldModelId, id => this.fieldModelManager.getById(id));
	}

	getIncludedStatesCount(chart: ChartModel): number {
		return (chart.stateFilters || []).filter(sf => sf.kind === 'INCLUDED').length;
	}

	getExcludedStatesCount(chart: ChartModel): number {
		return (chart.stateFilters || []).filter(sf => sf.kind === 'EXCLUDED').length;
	}
}
