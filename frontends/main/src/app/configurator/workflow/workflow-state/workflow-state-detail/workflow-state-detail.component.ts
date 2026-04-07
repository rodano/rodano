import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowState} from '@core/model/workflow-state';
import {Workflow} from '@core/model/workflow';
import {WorkflowStateDialogService} from '../../../services/dialogs/workflow-state-dialog.service';
import {WorkflowStateManagerService} from '../../../services/manager/workflow-state-manager.service';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';
import {BaseDraftDetailComponent} from '../../../shared/base-draft-detail.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SettingItemComponent} from '../../../shared/setting-item/setting-item.component';
import {UsedByComponent} from '../../../shared/used-by/used-by.component';
import {ConfiguratorNavigationService} from '../../../services/configurator-navigation.service';

@Component({
	selector: 'app-workflow-state-detail',
	standalone: true,
	templateUrl: './workflow-state-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class WorkflowStateDetailComponent extends BaseDraftDetailComponent<WorkflowState> {
	@Input() workflowStateId: string | null = null;
	@Input() workflowStates: WorkflowState[] = [];
	@Input() originalWorkflowStates: WorkflowState[] = [];
	@Input() workflow: Workflow | null = null;

	@Output() workflowStateUpdated = this.entityUpdated;
	@Output() workflowStateDeleted = this.entityDeleted;

	constructor(
		languageService: LanguageService,
		public navigationService: ConfiguratorNavigationService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowStateManager: WorkflowStateManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	get draftWorkflowState(): WorkflowState | null {return this.draftEntity;}

	protected entityIdInputName(): string {return 'workflowStateId';}
	protected entitiesInputName(): string {return 'workflowStates';}
	protected getEntityId(): string {return this.workflowStateId ?? '';}
	protected getEntities(): WorkflowState[] {return this.workflowStates;}
	protected getOriginals(): WorkflowState[] {return this.originalWorkflowStates;}
	protected findInArray(arr: WorkflowState[], id: string): WorkflowState | undefined {
		return arr.find(ws => ws.workflowStateId === id);
	}

	get hasAggregation(): boolean {
		return !!this.workflow?.aggregatedWorkflowId;
	}

	onEditBasicInfo(): void {
		if(!this.draftWorkflowState || !this.workflow) {
			return;
		}
		this.workflowStateDialogService.openBasicInfoDialog(
			this.draftEntity!,
			this.projectId,
			this.workflow.workflowId,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditAggregation(): void {
		if(!this.draftWorkflowState || !this.workflow?.aggregatedWorkflowId) {
			return;
		}
		this.workflowStateDialogService.openAggregationDialog(
			this.draftEntity!,
			this.workflow.aggregatedWorkflowId
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditPossibleActions(): void {
		if(!this.draftWorkflowState || !this.workflow) {
			return;
		}
		this.workflowStateDialogService.openActionsDialog(
			this.draftEntity!,
			this.workflow.workflowId
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onDelete(): void {
		if(!this.draftWorkflowState) {
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow State',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEntity!.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.workflowStateId);
			}
		});
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}
}
