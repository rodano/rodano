import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin, of} from 'rxjs';
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
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';

@Component({
	selector: 'app-chart-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, ChartDetailComponent, EmptyStateComponent, MatTooltip, ListHeaderComponent, ModifiedDirective],
	templateUrl: './chart-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ChartListComponent
	extends BaseListComponent<ChartModel>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() chartsChanged = new EventEmitter<boolean>();
	@Output() chartContextChanged = new EventEmitter<{
		charts: any[];
		selectedChartId: string | null;
	}>();

	constructor(
		public chartManager: ChartManagerService,
		public override languageService: LanguageService,
		private fieldModelManager: FieldModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private chartDialogService: ChartDialogService,
		snackBar: MatSnackBar
	) {
		super(chartManager, languageService, snackBar);
	}

	getEntityId(c: ChartModel): string {return c.chartId;}
	getNodePrefix(): string {return 'chart';}
	getListNodeName(): string {return 'charts';}

	get charts(): ChartModel[] {return this.chartManager.getAll();}
	get selectedChart(): ChartModel | null {return this.selected as ChartModel | null;}
	get modifiedChartIds(): Set<string> {return this.chartManager.getModifiedIds();}
	get originalCharts(): ChartModel[] {return this.chartManager.getOriginals();}

	loadCharts(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			charts: this.chartManager.loadFull(this.projectId),
			fieldModels: this.fieldModelManager.isLoaded()
				? of(null)
				: this.fieldModelManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({charts}) => this.afterLoad(charts),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'charts')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.chartsChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.chartContextChanged.emit({
			charts: [...this.charts],
			selectedChartId: this.selected?.chartId || null
		});
	}

	onCreate(): void {
		this.chartDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: ChartModel | null) => {
				if(result) {
					this.chartManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Chart'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create chart', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: ChartModel): void {
		this.chartManager.update(updated);
		this.selected = this.chartManager.getById(updated.chartId) || null;
		this.emitModificationChange();
	}

	onDeleted(chartId: string): void {
		const chart = this.charts.find(c => c.chartId === chartId);
		if(!chart) {
			return;
		}
		this.chartManager.delete(this.projectId, chartId).subscribe({
			next: () => this.afterDelete(chart, 'Chart'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete chart', 'Close', {duration: 3000});
			}
		});
	}

	onSelectChart(c: ChartModel): void {this.onSelect(c);}
	onCreateChart(): void {this.onCreate();}
	onChartUpdated(c: ChartModel): void {this.onUpdated(c);}
	onChartDeleted(id: string): void {this.onDeleted(id);}

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
