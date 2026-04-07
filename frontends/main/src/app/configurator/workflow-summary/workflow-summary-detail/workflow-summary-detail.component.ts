import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {WorkflowSummaryManagerService} from '../../services/manager/workflow-summary-manager.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {WorkflowSummaryDialogService} from '../../services/dialogs/workflow-summary-dialog.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {UsedByComponent} from '../../shared/used-by/used-by.component';
import {ConfiguratorNavigationService} from '../../services/configurator-navigation.service';

@Component({
	selector: 'app-workflow-summary-detail',
	standalone: true,
	templateUrl: './workflow-summary-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class WorkflowSummaryDetailComponent extends BaseManagerDetailComponent<WorkflowSummary, WorkflowSummaryManagerService> {
	@Input() override entity!: WorkflowSummary;
	@Input() override allEntities: WorkflowSummary[] = [];
	@Output() workflowSummaryUpdated = this.entityUpdated;
	@Output() workflowSummaryDeleted = this.entityDeleted;

	@Input() set workflowSummary(ww: WorkflowSummary) {this.entity = ww;}
	get workflowSummary(): WorkflowSummary {return this.entity;}

	@Input() set allWorkflowSummaries(ww: WorkflowSummary[]) {this.allEntities = ww;}

	constructor(
		workflowSummaryManager: WorkflowSummaryManagerService,
		languageService: LanguageService,
		public navigationService: ConfiguratorNavigationService,
		private scopeModelManager: ScopeModelManagerService,
		private workflowManager: WorkflowManagerService,
		private eventModelManager: EventModelManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowSummaryDialogService: WorkflowSummaryDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(workflowSummaryManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.workflowSummaryId;}

	onEditBasicInfo(): void {
		this.workflowSummaryDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditWorkflows(): void {
		this.workflowSummaryDialogService.openWorkflowDialog(
			this.projectId,
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditFilter(): void {
		this.workflowSummaryDialogService.openFilterDialog(
			this.projectId, this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditColumns(): void {
		this.workflowSummaryDialogService.openColumnsDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, columns: result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow Summary',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.title)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.workflowSummaryId);
			}
		});
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.scopeModelManager.getById(id));
	}

	getEventModelLabel(eventModelId: string): string {
		return this.languageService.getLabelById(eventModelId, id => this.eventModelManager.getById(id));
	}

	getWorkflowStateLabel(stateId: string): string {
		return this.languageService.getLabelById(stateId, id => this.workflowStateManager.getById(id));
	}
}
