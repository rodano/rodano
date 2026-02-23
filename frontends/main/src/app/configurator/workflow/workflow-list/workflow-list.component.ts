import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {forkJoin, Subscription} from 'rxjs';
import {Workflow} from '@core/model/workflow';
import {WorkflowState} from '@core/model/workflow-state';
import {WorkflowAction} from '@core/model/workflow-action';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';
import {WorkflowDialogService} from '../../services/dialogs/workflow-dialog.service';
import {WorkflowStateDialogService} from '../../services/dialogs/workflow-state-dialog.service';
import {WorkflowActionDialogService} from '../../services/dialogs/workflow-action-dialog.service';
import {WorkflowDetailComponent} from '../workflow-detail/workflow-detail.component';
import {WorkflowStateDetailComponent} from '../workflow-state/workflow-state-detail/workflow-state-detail.component';
import {
	WorkflowActionDetailComponent
} from '../workflow-action/workflow-action-detail/workflow-action-detail.component';
import {ProjectLanguage} from '@core/model/project-language';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';

type ViewMode = 'workflow-list' | 'workflow-detail' | 'state-list' | 'state-detail' | 'action-list' | 'action-detail';

@Component({
	selector: 'app-workflow-list',
	standalone: true,
	templateUrl: './workflow-list.component.html',
	styleUrls: ['../../shared/list-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		WorkflowDetailComponent,
		WorkflowStateDetailComponent,
		WorkflowActionDetailComponent,
		MatTooltipModule
	]
})
export class WorkflowListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() workflowsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() workflowContextChanged = new EventEmitter<{
		workflows: any[];
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}>();

	selectedWorkflow: Workflow | null = null;
	selectedWorkflowStateId: string | null = null;
	selectedWorkflowActionId: string | null = null;
	viewMode: ViewMode = 'workflow-list';
	loading = true;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	private currentWorkflowStates: WorkflowState[] = [];
	private currentWorkflowActions: WorkflowAction[] = [];

	constructor(
		public workflowManager: WorkflowManagerService,
		public workflowStateManager: WorkflowStateManagerService,
		public workflowActionManager: WorkflowActionManagerService,
		public languageService: LanguageService,
		private workflowDialogService: WorkflowDialogService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowActionDialogService: WorkflowActionDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadWorkflows();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode'] && this.workflows.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('workflow-')) {
				const workflowId = nodeId?.replace('workflow-', '');
				const workflow = this.workflows.find(wf => wf.workflowId === workflowId);
				if(workflow) {
					this.selectedWorkflow = workflow;
					this.updateFilteredWorkflowStates();
					this.updateFilteredWorkflowActions();
				}
			}
			else if(nodeId === 'workflows') {
				this.selectedWorkflow = null;
				this.currentWorkflowStates = [];
				this.currentWorkflowActions = [];
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedWorkflow) {
			return 0;
		}

		if(this.viewMode === 'workflow-detail' || this.viewMode === 'state-list' || this.viewMode === 'action-list') {
			return 2;
		}

		if(this.viewMode === 'state-detail' || this.viewMode === 'action-detail') {
			return 3;
		}

		return 0;
	}

	get workflows(): Workflow[] {
		return this.workflowManager.getAll();
	}

	get workflowStates(): WorkflowState[] {
		return this.currentWorkflowStates;
	}

	get workflowActions(): WorkflowAction[] {
		return this.currentWorkflowActions;
	}

	get modifiedWorkflowIds(): Set<string> {
		return this.workflowManager.getModifiedIds();
	}

	get originalWorkflows(): Workflow[] {
		return this.workflowManager.getOriginals();
	}

	get modifiedWorkflowStates(): Set<string> {
		return this.workflowStateManager.getModifiedIds();
	}

	get modifiedWorkflowActions(): Set<string> {
		return this.workflowActionManager.getModifiedIds();
	}

	get originalWorkflowStates(): WorkflowState[] {
		return this.workflowStateManager.getOriginals();
	}

	get originalWorkflowActions(): WorkflowAction[] {
		return this.workflowActionManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.workflowManager.getModificationCount() + this.workflowStateManager.getModificationCount() + this.workflowActionManager.getModificationCount();
	}

	loadWorkflows(): void {
		this.loading = true;

		forkJoin({
			workflows: this.workflowManager.load(this.projectId),
			workflowStates: this.workflowStateManager.loadFull(this.projectId),
			workflowActions: this.workflowActionManager.load(this.projectId)
		}).subscribe({
			next: ({workflows}) => {
				if(this.selectedWorkflow) {
					this.selectedWorkflow = workflows.find(
						wf => wf.workflowId === this.selectedWorkflow!.workflowId
					) || null;
					this.updateFilteredWorkflowStates();
					this.updateFilteredWorkflowActions();
				}
				this.loading = false;
				this.emitContext();
			},
			error: error => {
				console.error('Error loading workflows:', error);
				this.snackBar.open('Failed to load workflows', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private updateFilteredWorkflowStates(): void {
		if(!this.selectedWorkflow) {
			this.currentWorkflowStates = [];
			return;
		}
		this.currentWorkflowStates = this.workflowStateManager.getAllForWorkflow(this.selectedWorkflow.workflowId);
	}

	private updateFilteredWorkflowActions(): void {
		if(!this.selectedWorkflow) {
			this.currentWorkflowActions = [];
			return;
		}
		this.currentWorkflowActions = this.workflowActionManager.getAllForWorkflow(this.selectedWorkflow.workflowId);
	}

	onSelectWorkflow(workflow: Workflow): void {
		if(this.selectedWorkflow?.workflowId === workflow.workflowId) {
			this.clearSelection();
		}
		else {
			this.selectWorkflow(workflow);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedWorkflow = null;
		this.selectedWorkflowStateId = null;
		this.selectedWorkflowActionId = null;
		this.currentWorkflowStates = [];
		this.currentWorkflowActions = [];
		this.viewMode = 'workflow-detail';
		this.nodeSelected.emit('workflows');
	}

	backWorkflowDetail(): void {
		this.viewMode = 'workflow-detail';
		this.selectedWorkflowStateId = null;
		this.selectedWorkflowActionId = null;
		this.emitContext();
	}

	private selectWorkflow(workflow: Workflow): void {
		const previousWorkflowId = this.selectedWorkflow?.workflowId;

		this.selectedWorkflow = workflow;
		this.selectedWorkflowStateId = null;
		this.selectedWorkflowActionId = null;

		if(previousWorkflowId !== workflow.workflowId) {
			this.viewMode = 'workflow-detail';
		}

		this.updateFilteredWorkflowStates();
		this.updateFilteredWorkflowActions();
		this.emitContext();
		this.nodeSelected.emit(`workflow-${workflow.workflowId}`);
	}

	isSelected(workflow: Workflow): boolean {
		return this.selectedWorkflow?.workflowId === workflow.workflowId;
	}

	onCreateWorkflow(): void {
		this.workflowDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Workflow | null) => {
			if(result) {
				this.workflowManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Workflow created', 'Close', {duration: 2000});
						this.loadWorkflows();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating workflow', error);
						this.snackBar.open('Failed to create workflow', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onWorkflowUpdated(updatedWorkflow: Workflow): void {
		this.selectedWorkflow = this.workflowManager.getById(updatedWorkflow.workflowId) || null;
		this.emitModificationChange();
	}

	onWorkflowDeleted(workflowId: string): void {
		const workflow = this.workflows.find(wf => wf.workflowId === workflowId);
		if(!workflow) {
			return;
		}
		this.performDelete(workflow);
	}

	private performDelete(workflow: Workflow): void {
		this.workflowManager.delete(this.projectId, workflow.workflowId).subscribe({
			next: () => {
				this.snackBar.open('Workflow deleted', 'Close', {duration: 2000});

				if(this.selectedWorkflow?.workflowId === workflow.workflowId) {
					this.clearSelection();
				}

				this.loadWorkflows();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting workflow:', error);
				this.snackBar.open('Failed to delete workflow', 'Close', {duration: 3000});
			}
		});
	}

	onCreateWorkflowState(): void {
		if(!this.selectedWorkflow) {
			return;
		}

		this.workflowStateDialogService.openCreateDialog(
			this.projectId,
			this.selectedWorkflow.workflowId,
			this.projectLanguages
		).subscribe(result => {
			if(result && this.selectedWorkflow) {
				const newWorkflowState: WorkflowState = {
					workflowStateId: '',
					workflowId: this.selectedWorkflow.workflowId,
					...result,
					possibleActions: []
				};

				this.workflowStateManager.create(this.projectId, newWorkflowState).subscribe({
					next: () => {
						this.snackBar.open('Workflow state created', 'Close', {duration: 2000});
						this.loadWorkflows();
					},
					error: error => {
						console.error('Error creating workflow state:', error);
						this.snackBar.open('Failed to create workflow state', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onWorkflowStateUpdated(updatedWorkflowState: WorkflowState): void {
		if(!updatedWorkflowState) {
			return;
		}
		this.workflowStateManager.update(updatedWorkflowState);
		this.updateFilteredWorkflowStates();
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onWorkflowStateDeleted(workflowStateId: string): void {
		this.workflowStateManager.delete(this.projectId, workflowStateId).subscribe({
			next: () => {
				this.snackBar.open('Workflow state deleted', 'Close', {duration: 2000});

				if(this.selectedWorkflowStateId === workflowStateId) {
					this.selectedWorkflowStateId = null;
					this.viewMode = 'state-list';
				}

				this.updateFilteredWorkflowStates();
				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting workflow state:', error);
				this.snackBar.open('Failed to delete workflow state', 'Close', {duration: 3000});
			}
		});
	}

	switchToWorkflowStateView(): void {
		if(!this.selectedWorkflow) {
			return;
		}
		this.viewMode = 'state-list';
		this.emitContext();
	}

	onSelectWorkflowState(workflowStateId: string): void {
		if(this.selectedWorkflowStateId === workflowStateId) {
			this.selectedWorkflowStateId = null;
			this.viewMode = 'state-list';
		}
		else {
			this.selectedWorkflowStateId = workflowStateId;
			this.viewMode = 'state-detail';
			this.nodeSelected.emit(`workflow-state-${workflowStateId}`);
		}
		this.emitContext();
	}

	onCreateWorkflowAction(): void {
		if(!this.selectedWorkflow) {
			return;
		}

		this.workflowActionDialogService.openCreateDialog(
			this.projectId,
			this.selectedWorkflow.workflowId,
			this.projectLanguages
		).subscribe(result => {
			if(result && this.selectedWorkflow) {
				const newWorkflowAction: WorkflowAction = {
					workflowActionId: '',
					workflowId: this.selectedWorkflow.workflowId,
					...result
				};

				this.workflowActionManager.create(this.projectId, newWorkflowAction).subscribe({
					next: () => {
						this.snackBar.open('Workflow action created', 'Close', {duration: 2000});
						this.loadWorkflows();
					},
					error: error => {
						console.error('Error creating workflow action:', error);
						this.snackBar.open('Failed to create workflow action', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onWorkflowActionUpdated(updatedWorkflowAction: WorkflowAction): void {
		if(!updatedWorkflowAction) {
			return;
		}

		this.workflowActionManager.update(updatedWorkflowAction);
		this.updateFilteredWorkflowActions();
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onWorkflowActionDeleted(workflowActionId: string): void {
		this.workflowActionManager.delete(this.projectId, workflowActionId).subscribe({
			next: () => {
				this.snackBar.open('Workflow action deleted', 'Close', {duration: 2000});

				if(this.selectedWorkflowActionId === workflowActionId) {
					this.selectedWorkflowActionId = null;
					this.viewMode = 'action-list';
				}

				this.updateFilteredWorkflowActions();
				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting workflow action:', error);
				this.snackBar.open('Failed to delete workflow action', 'Close', {duration: 3000});
			}
		});
	}

	switchToWorkflowActionView(): void {
		if(!this.selectedWorkflow) {
			return;
		}
		this.viewMode = 'action-list';
		this.emitContext();
	}

	onSelectWorkflowAction(workflowActionId: string): void {
		if(this.selectedWorkflowActionId === workflowActionId) {
			this.selectedWorkflowActionId = null;
			this.viewMode = 'action-list';
		}
		else {
			this.selectedWorkflowActionId = workflowActionId;
			this.viewMode = 'action-detail';
			this.nodeSelected.emit(`workflow-action-${workflowActionId}`);
		}
		this.emitContext();
	}

	private emitModificationChange(): void {
		this.workflowsChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.workflowContextChanged.emit({
			workflows: [...this.workflows],
			workflowStates: this.workflowStates,
			workflowActions: this.workflowActions,
			selectedWorkflowId: this.selectedWorkflow?.workflowId || null,
			selectedWorkflowStateId: this.selectedWorkflowStateId,
			selectedWorkflowActionId: this.selectedWorkflowActionId
		});
	}

	isWorkflowStateModified(workflowStateId: string): boolean {
		return this.workflowStateManager.isModified(workflowStateId);
	}

	isWorkflowActionModified(workflowActionId: string): boolean {
		return this.workflowActionManager.isModified(workflowActionId);
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
