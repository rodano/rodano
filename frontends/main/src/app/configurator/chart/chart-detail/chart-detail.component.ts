import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Observable, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ChartManagerService} from '../../services/manager/chart-manager.service';
import {ChartDialogService} from '../../services/dialogs/chart-dialog.service';
import {ProjectLanguage} from '@core/model/project-language';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {ChartModel} from '@core/model/chart-model';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {ChartRange} from '@core/model/chart-range';

@Component({
	selector: 'app-chart-detail',
	standalone: true,
	templateUrl: './chart-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class ChartDetailComponent implements OnInit, OnDestroy {
	@Input() chart!: ChartModel;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allCharts: ChartModel[] = [];
	@Output() chartUpdated = new EventEmitter<ChartModel>();
	@Output() chartDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public chartManager: ChartManagerService,
		public languageService: LanguageService,
		private chartDialogService: ChartDialogService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.chartDialogService.openBasicInfoDialog(
			this.projectId,
			this.chart,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const typeChanged = result.type !== this.chart.type;
				const updatedChart: ChartModel = {
					...this.chart,
					...result,
					leafScopeModelId: result.type === 'WORKFLOW_STATUS' ? null : this.chart.leafScopeModelId,
					...(typeChanged
						? {
							workflowId: null,
							scopeModelId: null,
							fieldModelId: null,
							displayExpected: false,
							withStatistics: false,
							stateFilters: [],
							ranges: []
						}
						: {})
				};
				this.chartManager.update(updatedChart);
				this.chartUpdated.emit(updatedChart);
				this.showStagedMessage();
			}
		});
	}

	onEditSettings(): void {
		this.chartDialogService.openSettingsDialog(
			this.chart,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedChart: ChartModel = {...this.chart, ...result};
				this.chartManager.update(updatedChart);
				this.chartUpdated.emit(updatedChart);
				this.showStagedMessage();
			}
		});
	}

	onEditColors(): void {
		this.chartDialogService.openColorsDialog(this.chart).subscribe((result: string[] | null) => {
			if(result) {
				const updatedChart: ChartModel = {...this.chart, colors: result};
				this.chartManager.update(updatedChart);
				this.chartUpdated.emit(updatedChart);
				this.showStagedMessage();
			}
		});
	}

	onEditTypeSettings(): void {
		let dialog$: Observable<any>;

		switch(this.chart.type) {
			case 'STATISTICS':
				dialog$ = this.chartDialogService.openStatisticsDialog(this.chart, this.projectLanguages);
				break;
			case 'ENROLLMENT':
				dialog$ = this.chartDialogService.openEnrollmentDialog(this.chart);
				break;
			case 'WORKFLOW_STATUS':
				dialog$ = this.chartDialogService.openWorkflowStatusDialog(this.chart);
				break;
			case 'ENROLLMENT_BY_SCOPE':
				dialog$ = this.chartDialogService.openEnrollmentByScopeDialog(this.chart);
				break;
			default:
				return;
		}

		dialog$.subscribe((result: any) => {
			if(result) {
				let updatedChart: ChartModel;

				switch(this.chart.type) {
					case 'STATISTICS':
						updatedChart = {
							...this.chart,
							fieldModelId: result.fieldModelId,
							datasetModelId: result.datasetModelId,
							withStatistics: result.withStatistics,
							ranges: result.ranges
						};
						break;
					case 'ENROLLMENT':
						updatedChart = {
							...this.chart,
							displayExpected: result.displayExpected,
							workflowId: result.workflowId
						};
						break;
					case 'WORKFLOW_STATUS':
						updatedChart = {
							...this.chart,
							workflowId: result.workflowId,
							stateFilters: result.stateFilters
						};
						break;
					case 'ENROLLMENT_BY_SCOPE':
						updatedChart = {
							...this.chart,
							scopeModelId: result.scopeModelId
						};
						break;
					default:
						return;
				}

				this.chartManager.update(updatedChart);
				this.chart = updatedChart;
				this.chartUpdated.emit(updatedChart);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Chart',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.chart.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.chartDeleted.emit(this.chart.chartId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.chartManager.isFieldModified(this.chart.chartId, fieldName);
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

	getIncludedStates(): any[] {
		return (this.chart.stateFilters || []).filter(sf => sf.kind === 'INCLUDED');
	}

	getExcludedStates(): any[] {
		return (this.chart.stateFilters || []).filter(sf => sf.kind === 'EXCLUDED');
	}

	getRangeLabel(range: ChartRange): string {
		return this.languageService.getTranslatedValue(range.label);
	}
}
