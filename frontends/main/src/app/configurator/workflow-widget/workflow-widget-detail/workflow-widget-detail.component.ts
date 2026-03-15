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
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {WorkflowWidgetManagerService} from '../../services/manager/workflow-widget-manager.service';
import {WorkflowWidgetDialogService} from '../../services/dialogs/workflow-widget-dialog.service';

interface WorkflowStateGroup {
	workflowId: string;
	workflowName: string;
	states: {id: string; name: string}[];
}

@Component({
	selector: 'app-workflow-widget-detail',
	standalone: true,
	templateUrl: './workflow-widget-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class WorkflowWidgetDetailComponent extends BaseManagerDetailComponent<WorkflowWidgetConfig, WorkflowWidgetManagerService> {
	@Input() override entity!: WorkflowWidgetConfig;
	@Input() override allEntities: WorkflowWidgetConfig[] = [];
	@Output() workflowWidgetUpdated = this.entityUpdated;
	@Output() workflowWidgetDeleted = this.entityDeleted;

	@Input() set workflowWidget(ww: WorkflowWidgetConfig) {this.entity = ww;}
	get workflowWidget(): WorkflowWidgetConfig {return this.entity;}

	@Input() set allWorkflowWidgets(ww: WorkflowWidgetConfig[]) {this.allEntities = ww;}

	readonly columnTypeLabels: Record<string, string> = {
		WORKFLOW_LABEL: 'Workflow label',
		WORKFLOW_TRIGGER_MESSAGE: 'Workflow trigger message',
		STATUS_LABEL: 'Status label',
		STATUS_DATE: 'Status date',
		PARENT_SCOPE_CODE: 'Parent scope code',
		SCOPE_CODE: 'Scope code',
		EVENT_LABEL: 'Event label',
		EVENT_DATE: 'Event date',
		FORM_LABEL: 'Form label',
		FORM_DATE: 'Form date',
		FIELD_LABEL: 'Field label',
		FIELD_DATE: 'Field date'
	};

	constructor(
		workflowWidgetManager: WorkflowWidgetManagerService,
		languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowWidgetDialogService: WorkflowWidgetDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(workflowWidgetManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.workflowWidgetId;}

	onEditBasicInfo(): void {
		this.workflowWidgetDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditWorkflow(): void {
		this.workflowWidgetDialogService.openWorkflowDialog(
			this.entity, this.projectId
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	getColumnTypeLabel(type: string): string {
		return this.columnTypeLabels[type] ?? type;
	}

	onEditColumns(): void {
		if(!this.entity) {
			return;
		}
		this.workflowWidgetDialogService.openColumnsDialog(
			this.entity.columns ?? [],
			this.projectLanguages,
			this.entity.workflowEntity
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
				title: 'Delete Workflow Widget',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.workflowWidgetId);
			}
		});
	}

	getWorkflowStateSelections(): WorkflowStateGroup[] {
		const grouped = new Map<string, {id: string; name: string}[]>();
		(this.entity.workflowStateIds || []).forEach(stateId => {
			const state = this.workflowStateManager.getById(stateId);
			if(!state) {
				return;
			}
			if(!grouped.has(state.workflowId)) {
				grouped.set(state.workflowId, []);
			}
			const name = `${this.languageService.getDefaultTranslation(state.shortname) || state.id} (${state.id})`;
			grouped.get(state.workflowId)!.push({id: stateId, name});
		});
		const result: WorkflowStateGroup[] = [];
		grouped.forEach((states, workflowId) => result.push({workflowId, workflowName: this.getWorkflowLabel(workflowId), states}));
		return result;
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
