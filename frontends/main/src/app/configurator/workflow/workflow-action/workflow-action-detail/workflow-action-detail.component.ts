import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowAction} from '@core/model/workflow-action';
import {Workflow} from '@core/model/workflow';
import {WorkflowActionDialogService} from '../../../services/dialogs/workflow-action-dialog.service';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';
import {BaseDraftDetailComponent} from '../../../shared/base-draft-detail.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SettingItemComponent} from '../../../shared/setting-item/setting-item.component';
import {RuleListComponent} from '../../../rules/rule-list/rule-list.component';
import {Rule} from '@core/model/rule';

@Component({
	selector: 'app-workflow-action-detail',
	standalone: true,
	templateUrl: './workflow-action-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent, RuleListComponent]
})
export class WorkflowActionDetailComponent extends BaseDraftDetailComponent<WorkflowAction> {
	@Input() workflowActionId = '';
	@Input() workflowActions: WorkflowAction[] = [];
	@Input() originalWorkflowActions: WorkflowAction[] = [];
	@Input() workflow: Workflow | null = null;
	@Input() initialTab: 'general' | 'rules' = 'general';

	@Output() workflowActionUpdated = this.entityUpdated;
	@Output() workflowActionDeleted = this.entityDeleted;
	@Output() switchToRuleEditor = new EventEmitter<Rule>();

	readonly ruleTypes = [{type: null, label: 'Rules'}];

	constructor(
		languageService: LanguageService,
		private workflowActionDialogService: WorkflowActionDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	get draftWorkflowAction(): WorkflowAction | null {return this.draftEntity;}

	protected entityIdInputName(): string {return 'workflowActionId';}
	protected entitiesInputName(): string {return 'workflowActions';}
	protected getEntityId(): string {return this.workflowActionId;}
	protected getEntities(): WorkflowAction[] {return this.workflowActions;}
	protected getOriginals(): WorkflowAction[] {return this.originalWorkflowActions;}
	protected findInArray(arr: WorkflowAction[], id: string): WorkflowAction | undefined {
		return arr.find(wa => wa.workflowActionId === id);
	}

	onEditBasicInfo(): void {
		if(!this.draftWorkflowAction || !this.workflow) {
			return;
		}
		this.workflowActionDialogService.openEditDialog(
			this.projectId,
			this.workflow.workflowId,
			this.draftEntity!,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditDocumentation(): void {
		if(!this.draftWorkflowAction) {
			return;
		}
		this.workflowActionDialogService.openDocumentationDialog(
			this.draftEntity!, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditSignature(): void {
		if(!this.draftWorkflowAction) {
			return;
		}
		this.workflowActionDialogService.openSignatureDialog(
			this.draftEntity!, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onDelete(): void {
		if(!this.draftWorkflowAction) {
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow Action',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEntity!.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.workflowActionId);
			}
		});
	}
}
