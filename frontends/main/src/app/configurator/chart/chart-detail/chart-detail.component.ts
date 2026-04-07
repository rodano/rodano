import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Observable} from 'rxjs';
import {ChartModel} from '@core/model/chart-model';
import {ChartRange} from '@core/model/chart-range';
import {ChartManagerService} from '../../services/manager/chart-manager.service';
import {ChartDialogService} from '../../services/dialogs/chart-dialog.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {UsedByComponent} from '../../shared/used-by/used-by.component';
import {ConfiguratorNavigationService} from '../../services/configurator-navigation.service';

@Component({
	selector: 'app-chart-detail',
	standalone: true,
	templateUrl: './chart-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class ChartDetailComponent
	extends BaseManagerDetailComponent<ChartModel, ChartManagerService> {
	@Input() override entity!: ChartModel;
	@Input() override allEntities: ChartModel[] = [];
	@Output() chartUpdated = this.entityUpdated;
	@Output() chartDeleted = this.entityDeleted;

	@Input() set chart(v: ChartModel) {this.entity = v;}
	get chart(): ChartModel {return this.entity;}

	@Input() set allCharts(v: ChartModel[]) {this.allEntities = v;}

	constructor(
		chartManager: ChartManagerService,
		languageService: LanguageService,
		public navigationService: ConfiguratorNavigationService,
		private chartDialogService: ChartDialogService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(chartManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.chartId;}

	onEditBasicInfo(): void {
		this.chartDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const typeChanged = result.type !== this.entity.type;
				this.applyUpdate({
					...this.entity,
					...result,
					leafScopeModelId: result.type === 'WORKFLOW_STATUS' ? null : this.entity.leafScopeModelId,
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
				});
			}
		});
	}

	onEditSettings(): void {
		this.chartDialogService.openSettingsDialog(
			this.entity, this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditColors(): void {
		this.chartDialogService.openColorsDialog(this.entity).subscribe((result: string[] | null) => {
			if(result) {
				this.applyUpdate({...this.entity, colors: result});
			}
		});
	}

	onEditTypeSettings(): void {
		let dialog$: Observable<any>;

		switch(this.entity.type) {
			case 'STATISTICS':
				dialog$ = this.chartDialogService.openStatisticsDialog(this.entity, this.projectLanguages);
				break;
			case 'ENROLLMENT':
				dialog$ = this.chartDialogService.openEnrollmentDialog(this.entity);
				break;
			case 'WORKFLOW_STATUS':
				dialog$ = this.chartDialogService.openWorkflowStatusDialog(this.entity);
				break;
			case 'ENROLLMENT_BY_SCOPE':
				dialog$ = this.chartDialogService.openEnrollmentByScopeDialog(this.entity);
				break;
			default:
				return;
		}

		dialog$.subscribe((result: any) => {
			if(!result) {
				return;
			}

			switch(this.entity.type) {
				case 'STATISTICS':
					this.applyUpdate({...this.entity, fieldModelId: result.fieldModelId, datasetModelId: result.datasetModelId, withStatistics: result.withStatistics, ranges: result.ranges});
					break;
				case 'ENROLLMENT':
					this.applyUpdate({...this.entity, displayExpected: result.displayExpected, workflowId: result.workflowId});
					break;
				case 'WORKFLOW_STATUS':
					this.applyUpdate({...this.entity, workflowId: result.workflowId, stateFilters: result.stateFilters});
					break;
				case 'ENROLLMENT_BY_SCOPE':
					this.applyUpdate({...this.entity, scopeModelId: result.scopeModelId});
					break;
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Chart',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.chartId);
			}
		});
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
		return (this.entity.stateFilters || []).filter(sf => sf.kind === 'INCLUDED');
	}

	getExcludedStates(): any[] {
		return (this.entity.stateFilters || []).filter(sf => sf.kind === 'EXCLUDED');
	}

	getRangeLabel(range: ChartRange): string {
		return this.languageService.getTranslatedValue(range.label);
	}
}
