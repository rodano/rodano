import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {forkJoin} from 'rxjs';
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

type ViewMode = 'workflow-list' | 'workflow-detail' | 'state-list' | 'state-detail' | 'action-list' | 'action-detail';

@Component({
	selector: 'app-workflow-list',
	standalone: true,
	templateUrl: './workflow-list.component.html',
	styleUrls: ['./workflow-list.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		WorkflowDetailComponent,
		WorkflowStateDetailComponent,
		WorkflowActionDetailComponent,
		MatTooltipModule
	]
})
export class WorkflowListComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() workflowsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() workflowContextChanged = new EventEmitter<{
		workflowStates: any[];
		workflowActions: any[];
		selectedWorkflowId: string | null;
		selectedWorkflowStateId: string | null;
		selectedWorkflowActionId: string | null;
	}>();

	loading = true;
	workflows: Workflow[] = [];
	originalWorkflows: Workflow[] = [];
	workflowStates: WorkflowState[] = [];
	originalWorkflowStates: WorkflowState[] = [];
	workflowActions: WorkflowAction[] = [];
	originalWorkflowActions: WorkflowAction[] = [];

	selectedWorkflow: Workflow | null = null;
	selectedWorkflowStateId: string | null = null;
	selectedWorkflowActionId: string | null = null;

	modifiedWorkflowIds = new Set<string>();
	modifiedWorkflowStateIds = new Set<string>();
	modifiedWorkflowActionIds = new Set<string>();

	viewMode: ViewMode = 'workflow-list';
	viewLevel = 1;

	constructor(
		public workflowManager: WorkflowManagerService,
		public workflowStateManager: WorkflowStateManagerService,
		public workflowActionManager: WorkflowActionManagerService,
		private workflowDialogService: WorkflowDialogService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowActionDialogService: WorkflowActionDialogService,
		private languageService: LanguageService
	) {}

	ngOnInit(): void {
		this.loadWorkflows();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode'] && this.selectedNode) {
			this.handleNodeSelection(this.selectedNode);
		}
	}

	loadWorkflows(): void {
		this.loading = true;

		forkJoin({
			workflows: this.workflowManager.load(this.projectId),
			workflowStates: this.workflowStateManager.load(this.projectId),
			workflowActions: this.workflowActionManager.load(this.projectId)
		}).subscribe({
			next: ({workflows, workflowStates, workflowActions}) => {
				this.workflows = workflows;
				this.originalWorkflows = this.workflowManager.getOriginals();

				this.originalWorkflowStates = this.workflowStateManager.getOriginals();
				this.originalWorkflowActions = this.workflowActionManager.getOriginals();

				this.modifiedWorkflowIds = this.workflowManager.getModifiedIds();
				this.modifiedWorkflowStateIds = this.workflowStateManager.getModifiedIds();
				this.modifiedWorkflowActionIds = this.workflowActionManager.getModifiedIds();

				this.emitModificationCount();
				this.loading = false;
			},
			error: error => {
				console.error('Error loading workflows:', error);
				this.loading = false;
			}
		});
	}

	private handleNodeSelection(nodeId: string): void {
		if(nodeId === 'workflows') {
			this.viewMode = 'workflow-list';
			this.viewLevel = 1;
			this.clearSelection();
		}
		else if(nodeId.startsWith('workflow-')) {
			const workflowId = nodeId.replace('workflow-', '');
			const workflow = this.workflows.find(wf => wf.workflowId === workflowId);
			if(workflow) {
				this.onSelectWorkflow(workflow);
			}
		}
		else if(nodeId.startsWith('workflow-state-')) {
			const workflowStateId = nodeId.replace('workflow-state-', '');
			this.onSelectWorkflowState(workflowStateId);
		}
		else if(nodeId.startsWith('workflow-action-')) {
			const workflowActionId = nodeId.replace('workflow-action-', '');
			this.onSelectWorkflowAction(workflowActionId);
		}
	}

	onSelectWorkflow(workflow: Workflow | null): void {
		if(!workflow) {
			this.viewMode = 'workflow-list';
			this.viewLevel = 1;
			this.selectedWorkflow = null;
			this.selectedWorkflowStateId = null;
			this.selectedWorkflowActionId = null;
			this.workflowStates = [];
			this.workflowActions = [];
			this.emitContext();
			return;
		}

		if(this.selectedWorkflow?.workflowId === workflow.workflowId && this.viewMode === 'workflow-detail') {
			this.viewMode = 'workflow-list';
			this.viewLevel = 1;
			this.selectedWorkflow = null;
			this.selectedWorkflowStateId = null;
			this.selectedWorkflowActionId = null;
			this.workflowStates = [];
			this.workflowActions = [];
		}
		else {
			this.selectedWorkflow = workflow;
			this.viewMode = 'workflow-detail';
			this.viewLevel = 2;
			this.selectedWorkflowStateId = null;
			this.selectedWorkflowActionId = null;

			this.workflowStates = this.workflowStateManager.getAllForWorkflow(workflow.workflowId);
			this.workflowActions = this.workflowActionManager.getAllForWorkflow(workflow.workflowId);
		}

		this.emitContext();
	}

	isSelected(workflow: Workflow): boolean {
		return this.selectedWorkflow?.workflowId === workflow.workflowId;
	}

	onCreateWorkflow(): void {
		this.workflowDialogService.openCreateDialog(
			this.projectId,
			this.project?.languages || []
		).subscribe(result => {
			if(result) {
				const newWorkflow: Workflow = {
					workflowId: `temp-${Date.now()}`,
					...result
				};

				this.workflows.push(newWorkflow);
				this.workflowManager.update(newWorkflow);
				this.modifiedWorkflowIds = this.workflowManager.getModifiedIds();
				this.emitModificationCount();
			}
		});
	}

	onWorkflowUpdated(updatedWorkflow: Workflow): void {
		const index = this.workflows.findIndex(wf => wf.workflowId === updatedWorkflow.workflowId);
		if(index !== -1) {
			this.workflows[index] = updatedWorkflow;
		}

		this.selectedWorkflow = updatedWorkflow;
		this.workflowManager.update(updatedWorkflow);
		this.modifiedWorkflowIds = this.workflowManager.getModifiedIds();
		this.emitModificationCount();
	}

	onWorkflowDeleted(workflowId: string): void {
		this.workflows = this.workflows.filter(wf => wf.workflowId !== workflowId);

		const wasOriginal = this.originalWorkflows.some(wf => wf.workflowId === workflowId);
		if(wasOriginal) {
			this.modifiedWorkflowIds.add(`${workflowId}-deleted`);
		}
		else {
			this.modifiedWorkflowIds.delete(workflowId);
		}

		this.selectedWorkflow = null;
		this.viewMode = 'workflow-list';
		this.viewLevel = 1;
		this.emitModificationCount();
		this.emitContext();
	}

	switchToWorkflowStateView(): void {
		this.viewMode = 'state-list';
		this.viewLevel = 2;
		this.emitContext();
	}

	switchToWorkflowActionView(): void {
		this.viewMode = 'action-list';
		this.viewLevel = 2;
		this.emitContext();
	}

	backWorkflowDetail(): void {
		this.viewMode = 'workflow-detail';
		this.viewLevel = 2;
		this.selectedWorkflowStateId = null;
		this.selectedWorkflowActionId = null;
		this.emitContext();
	}

	onSelectWorkflowState(workflowStateId: string | null): void {
		if(!workflowStateId) {
			this.viewMode = 'state-list';
			this.viewLevel = 2;
			this.selectedWorkflowStateId = null;
		}
		else if(this.selectedWorkflowStateId === workflowStateId && this.viewMode === 'state-detail') {
			this.viewMode = 'state-list';
			this.viewLevel = 2;
			this.selectedWorkflowStateId = null;
		}
		else {
			this.selectedWorkflowStateId = workflowStateId;
			this.viewMode = 'state-detail';
			this.viewLevel = 3;
		}

		this.emitContext();
	}

	onSelectWorkflowAction(workflowActionId: string | null): void {
		if(!workflowActionId) {
			this.viewMode = 'action-list';
			this.viewLevel = 2;
			this.selectedWorkflowActionId = null;
		}
		else if(this.selectedWorkflowActionId === workflowActionId && this.viewMode === 'action-detail') {
			this.viewMode = 'action-list';
			this.viewLevel = 2;
			this.selectedWorkflowActionId = null;
		}
		else {
			this.selectedWorkflowActionId = workflowActionId;
			this.viewMode = 'action-detail';
			this.viewLevel = 3;
		}

		this.emitContext();
	}

	onCreateWorkflowState(): void {
		if(!this.selectedWorkflow) {
			return;
		}

		this.workflowStateDialogService.openCreateDialog(
			this.projectId,
			this.selectedWorkflow.workflowId,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.selectedWorkflow) {
				const newWorkflowState: WorkflowState = {
					workflowStateId: `temp-${Date.now()}`,
					workflowId: this.selectedWorkflow.workflowId,
					...result
				};

				this.workflowStates.push(newWorkflowState);
				this.workflowStateManager.update(newWorkflowState);
				this.modifiedWorkflowStateIds = this.workflowStateManager.getModifiedIds();
				this.emitModificationCount();
				this.emitContext();
			}
		});
	}

	onWorkflowStateUpdated(updatedWorkflowState: WorkflowState): void {
		this.workflowStateManager.update(updatedWorkflowState);

		if(this.selectedWorkflow) {
			this.workflowStates = this.workflowStateManager.getAllForWorkflow(this.selectedWorkflow.workflowId);
		}

		this.modifiedWorkflowStateIds = this.workflowStateManager.getModifiedIds();
		this.emitModificationCount();
		this.emitContext();
	}

	onWorkflowStateDeleted(workflowStateId: string): void {
		this.workflowStates = this.workflowStates.filter(wfs => wfs.workflowStateId !== workflowStateId);

		const wasOriginal = this.originalWorkflowStates.some(wfs => wfs.workflowStateId === workflowStateId);
		if(wasOriginal) {
			this.modifiedWorkflowStateIds.add(`${workflowStateId}-deleted`);
		}
		else {
			this.modifiedWorkflowStateIds.delete(workflowStateId);
		}

		this.selectedWorkflowStateId = null;
		this.viewMode = 'state-list';
		this.viewLevel = 2;
		this.emitModificationCount();
		this.emitContext();
	}

	onCreateWorkflowAction(): void {
		if(!this.selectedWorkflow) {
			return;
		}

		this.workflowActionDialogService.openCreateDialog(
			this.projectId,
			this.selectedWorkflow.workflowId,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.selectedWorkflow) {
				const newWorkflowAction: WorkflowAction = {
					workflowActionId: `temp-${Date.now()}`,
					workflowId: this.selectedWorkflow.workflowId,
					...result
				};

				this.workflowActions.push(newWorkflowAction);
				this.workflowActionManager.update(newWorkflowAction);
				this.modifiedWorkflowActionIds = this.workflowActionManager.getModifiedIds();
				this.emitModificationCount();
				this.emitContext();
			}
		});
	}

	onWorkflowActionUpdated(updatedWorkflowAction: WorkflowAction): void {
		this.workflowActionManager.update(updatedWorkflowAction);

		if(this.selectedWorkflow) {
			this.workflowActions = this.workflowActionManager.getAllForWorkflow(this.selectedWorkflow.workflowId);
		}

		this.modifiedWorkflowActionIds = this.workflowActionManager.getModifiedIds();
		this.emitModificationCount();
		this.emitContext();
	}

	onWorkflowActionDeleted(workflowActionId: string): void {
		this.workflowActions = this.workflowActions.filter(wfa => wfa.workflowActionId !== workflowActionId);

		const wasOriginal = this.originalWorkflowActions.some(wfa => wfa.workflowActionId === workflowActionId);
		if(wasOriginal) {
			this.modifiedWorkflowActionIds.add(`${workflowActionId}-deleted`);
		}
		else {
			this.modifiedWorkflowActionIds.delete(workflowActionId);
		}

		this.selectedWorkflowActionId = null;
		this.viewMode = 'action-list';
		this.viewLevel = 2;
		this.emitModificationCount();
		this.emitContext();
	}

	isWorkflowStateModified(workflowStateId: string): boolean {
		return this.modifiedWorkflowStateIds.has(workflowStateId);
	}

	isWorkflowActionModified(workflowActionId: string): boolean {
		return this.modifiedWorkflowActionIds.has(workflowActionId);
	}

	clearSelection(): void {
		this.selectedWorkflow = null;
		this.selectedWorkflowStateId = null;
		this.selectedWorkflowActionId = null;
		this.workflowStates = [];
		this.workflowActions = [];
		this.viewMode = 'workflow-list';
		this.viewLevel = 1;
		this.emitContext();
	}

	private emitModificationCount(): void {
		const totalCount = this.modifiedWorkflowIds.size + this.modifiedWorkflowStateIds.size + this.modifiedWorkflowActionIds.size;

		this.workflowsChanged.emit({modificationCount: totalCount});
	}

	private emitContext(): void {
		this.workflowContextChanged.emit({
			workflowStates: this.workflowStates,
			workflowActions: this.workflowActions,
			selectedWorkflowId: this.selectedWorkflow?.workflowId || null,
			selectedWorkflowStateId: this.selectedWorkflowStateId,
			selectedWorkflowActionId: this.selectedWorkflowActionId
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}
}
