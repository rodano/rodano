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

@Component({
	selector: 'app-workflow-detail',
	standalone: true,
	templateUrl: './workflow-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
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
		private workflowActionManager: WorkflowActionManagerService,
		private languageService: LanguageService,
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
				message: `Are you sure you want to delete "${this.getTranslatedName(this.workflow.shortname)}"?`,
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

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.selectedLanguage;
		return translations[lang] || '';
	}

	getWorkflowLabel(workflowId: string): string {
		const workflow = this.allWorkflows.find(wf => wf.workflowId === workflowId);
		if(!workflow) {
			return workflowId;
		}

		const name = this.languageService.getDefaultTranslation(workflow.shortname) || workflow.id;
		return `${name} (${workflow.id})`;
	}

	getWorkflowStateLabel(workflowStateId: string | null | undefined): string {
		if(!workflowStateId) {
			return 'Not configured';
		}
		const workflowState = this.workflowStateManager.getAllForWorkflow(this.workflow.workflowId)
			.find(wfs => wfs.workflowStateId === workflowStateId);
		if(!workflowState) {
			return workflowStateId;
		}
		const name = this.languageService.getDefaultTranslation(workflowState.shortname) || workflowState.id;
		return `${name} (${workflowState.id})`;
	}

	getWorkflowActionLabel(workflowActionId: string | null | undefined): string {
		if(!workflowActionId) {
			return 'Not configured';
		}
		const workflowAction = this.workflowActionManager.getAllForWorkflow(this.workflow.workflowId)
			.find(wfa => wfa.workflowActionId === workflowActionId);
		if(!workflowAction) {
			return workflowActionId;
		}
		const name = this.languageService.getDefaultTranslation(workflowAction.shortname) || workflowAction.id;
		return `${name} (${workflowAction.id})`;
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}
}
