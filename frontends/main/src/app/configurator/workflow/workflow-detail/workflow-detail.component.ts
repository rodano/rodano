import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {Workflow} from '@core/model/workflow';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowDialogService} from '../../services/dialogs/workflow-dialog.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {Rule} from '@core/model/rule';
import {RuleListComponent} from '../../rules/rule-list/rule-list.component';

@Component({
	selector: 'app-workflow-detail',
	standalone: true,
	templateUrl: './workflow-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent, RuleListComponent]
})
export class WorkflowDetailComponent extends BaseManagerDetailComponent<Workflow, WorkflowManagerService> {
	@Input() override entity!: Workflow;
	@Input() override allEntities: Workflow[] = [];

	@Input() set workflow(v: Workflow) {this.entity = v;}
	get workflow(): Workflow {return this.entity;}

	@Input() set allWorkflows(v: Workflow[]) {this.allEntities = v;}

	@Input() initialTab: 'general' | 'rules' = 'general';

	@Output() workflowUpdated = this.entityUpdated;
	@Output() workflowDeleted = this.entityDeleted;

	@Output() switchToWorkflowStates = new EventEmitter<void>();
	@Output() switchToWorkflowActions = new EventEmitter<void>();
	@Output() switchToRuleEditor = new EventEmitter<Rule>();

	readonly ruleTypes = [{type: null, label: 'Rules'}];
	readonly ruleDomains = ['SCOPE', 'EVENT', 'DATASET', 'FIELD', 'FORM', 'WORKFLOW'];

	constructor(
		workflowManager: WorkflowManagerService,
		public workflowStateManager: WorkflowStateManagerService,
		public workflowActionManager: WorkflowActionManagerService,
		languageService: LanguageService,
		private workflowDialogService: WorkflowDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(workflowManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.workflowId;}

	onEditBasicInfo(): void {
		this.workflowDialogService.openBasicInfoDialog(
			this.projectId, this.entity, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditAssignment(): void {
		this.workflowDialogService.openAssignmentDialog(
			this.entity, this.allEntities
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditMisc(): void {
		this.workflowDialogService.openMiscDialog(
			this.entity, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedName(this.entity.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.workflowId);
			}
		});
	}

	onSwitchToWorkflowStates(): void {this.switchToWorkflowStates.emit();}
	onSwitchToWorkflowActions(): void {this.switchToWorkflowActions.emit();}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.manager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}

	getWorkflowActionLabel(workflowActionId: string): string {
		return this.languageService.getLabelById(workflowActionId, id => this.workflowActionManager.getById(id));
	}
}
