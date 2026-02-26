import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {Workflow} from '@core/model/workflow';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowDialogService} from '../../services/dialogs/workflow-dialog.service';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-workflow-detail',
	standalone: true,
	templateUrl: './workflow-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DangerZoneComponent
	]
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {
	@Input() workflow!: Workflow;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allWorkflows: Workflow[] = [];
	@Output() workflowUpdated = new EventEmitter<Workflow>();
	@Output() workflowDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();
	@Output() switchToWorkflowStates = new EventEmitter<void>();
	@Output() switchToWorkflowActions = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	constructor(
		public workflowManager: WorkflowManagerService,
		public workflowStateManager: WorkflowStateManagerService,
		public languageService: LanguageService,
		private workflowActionManager: WorkflowActionManagerService,
		private workflowDialogService: WorkflowDialogService,
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
		this.workflowDialogService.openBasicInfoDialog(
			this.projectId,
			this.workflow,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedWorkflow: Workflow = {...this.workflow, ...result};
				this.workflowManager.update(updatedWorkflow);
				this.workflowUpdated.emit(updatedWorkflow);
				this.showStagedMessage();
			}
		});
	}

	onEditAssignment(): void {
		this.workflowDialogService.openAssignmentDialog(
			this.workflow,
			this.allWorkflows
		).subscribe((result: any) => {
			if(result) {
				const updatedWorkflow: Workflow = {...this.workflow, ...result};
				this.workflowManager.update(updatedWorkflow);
				this.workflowUpdated.emit(updatedWorkflow);
				this.showStagedMessage();
			}
		});
	}

	onEditMisc(): void {
		this.workflowDialogService.openMiscDialog(
			this.workflow,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedWorkflow: Workflow = {...this.workflow, ...result};
				this.workflowManager.update(updatedWorkflow);
				this.workflowUpdated.emit(updatedWorkflow);
				this.showStagedMessage();
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedName(this.workflow.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.workflowDeleted.emit(this.workflow.workflowId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	onSwitchToWorkflowStates(): void {
		this.switchToWorkflowStates.emit();
	}

	onSwitchToWorkflowActions(): void {
		this.switchToWorkflowActions.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.workflowManager.isFieldModified(this.workflow.workflowId, fieldName);
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}

	getWorkflowActionLabel(workflowActionId: string): string {
		return this.languageService.getLabelById(workflowActionId, id => this.workflowActionManager.getById(id));
	}
}
