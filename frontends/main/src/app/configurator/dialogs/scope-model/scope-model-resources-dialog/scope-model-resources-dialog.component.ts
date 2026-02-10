import {ScopeModel} from '@core/model/scope-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';

interface WorkflowStateSelector {
	id: string;
	selectedWorkflowId: string;
	availableStates: {id: string; name: string}[];
	selectedStates: {id: string; name: string}[];
}

export interface WorkflowStateSelection {
	workflowId: string;
	workflowStateId: string;
}

export interface ScopeModelResourcesDialogData {
	scopeModel: ScopeModel;
	availableForms: {id: string; name: string}[];
	availableDatasets: {id: string; name: string}[];
	availableWorkflows: {id: string; name: string; states: {id: string; name: string}[]}[];
	workflowStateSelections: WorkflowStateSelection[];
}

@Component({
	selector: 'app-scope-model-resources-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatSelectModule
	],
	templateUrl: './scope-model-resources-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class ScopeModelResourcesDialogComponent implements OnInit {
	availableFormModels: {id: string; name: string}[] = [];
	selectedFormModels: {id: string; name: string}[] = [];

	availableDatasetModels: {id: string; name: string}[] = [];
	selectedDatasetModels: {id: string; name: string}[] = [];

	availableWorkflows: {id: string; name: string; states: {id: string; name: string}[]}[] = [];
	selectedWorkflows: {id: string; name: string; states: {id: string; name: string}[]}[] = [];

	workflowStateSelectors: WorkflowStateSelector[] = [];
	private selectorIdCounter = 0;

	constructor(
		private dialogRef: MatDialogRef<ScopeModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ScopeModelResourcesDialogData
	) {}

	ngOnInit(): void {
		this.initializeFormModels();
		this.initializeDatasetModels();
		this.initializeWorkflows();
		this.initializeWorkflowStateSelectors();
	}

	private initializeFormModels(): void {
		const selectedIds = this.data.scopeModel.formModelIds || [];
		this.selectedFormModels = this.data.availableForms.filter(fm => selectedIds.includes(fm.id));
		this.availableFormModels = this.data.availableForms.filter(fm => !selectedIds.includes(fm.id));
	}

	private initializeDatasetModels(): void {
		const selectedIds = this.data.scopeModel.datasetModelIds || [];
		this.selectedDatasetModels = this.data.availableDatasets.filter(dm => selectedIds.includes(dm.id));
		this.availableDatasetModels = this.data.availableDatasets.filter(dm => !selectedIds.includes(dm.id));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.scopeModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.id));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.id));
	}

	private initializeWorkflowStateSelectors(): void {
		const selectionsByWorkflow = new Map<string, string[]>();

		this.data.workflowStateSelections.forEach(selection => {
			if(!selectionsByWorkflow.has(selection.workflowId)) {
				selectionsByWorkflow.set(selection.workflowId, []);
			}
			selectionsByWorkflow.get(selection.workflowId)!.push(selection.workflowStateId);
		});

		selectionsByWorkflow.forEach((stateIds, workflowId) => {
			const workflow = this.selectedWorkflows.find(wf => wf.id === workflowId);
			if(workflow) {
				const selectorId = `selector_${this.selectorIdCounter++}`;

				const selectedStates = workflow.states.filter(state => stateIds.includes(state.id));
				const availableStates = workflow.states.filter(state => !stateIds.includes(state.id));

				this.workflowStateSelectors.push({
					id: selectorId,
					selectedWorkflowId: workflowId,
					availableStates,
					selectedStates
				});
			}
		});
	}

	onAddFormModel(formModel: {id: string; name: string}): void {
		this.availableFormModels = this.availableFormModels.filter(fm => fm.id !== formModel.id);
		this.selectedFormModels = [...this.selectedFormModels, formModel];
	}

	onRemoveFormModel(formModel: {id: string; name: string}): void {
		this.selectedFormModels = this.selectedFormModels.filter(fm => fm.id !== formModel.id);
		this.availableFormModels = [...this.availableFormModels, formModel];
	}

	onAddDatasetModel(datasetModel: {id: string; name: string}): void {
		this.availableDatasetModels = this.availableDatasetModels.filter(dm => dm.id !== datasetModel.id);
		this.selectedDatasetModels = [...this.selectedDatasetModels, datasetModel];
	}

	onRemoveDatasetModel(datasetModel: {id: string; name: string}): void {
		this.selectedDatasetModels = this.selectedDatasetModels.filter(dm => dm.id !== datasetModel.id);
		this.availableDatasetModels = [...this.availableDatasetModels, datasetModel];
	}

	onAddWorkflow(workflow: {id: string; name: string; states: {id: string; name: string}[]}): void {
		this.availableWorkflows = this.availableWorkflows.filter(wf => wf.id !== workflow.id);
		this.selectedWorkflows = [...this.selectedWorkflows, workflow];
	}

	onRemoveWorkflow(workflow: {id: string; name: string; states: {id: string; name: string}[]}): void {
		this.selectedWorkflows = this.selectedWorkflows.filter(wf => wf.id !== workflow.id);
		this.availableWorkflows = [...this.availableWorkflows, workflow];

		this.workflowStateSelectors = this.workflowStateSelectors.filter(
			selector => selector.selectedWorkflowId !== workflow.id
		);
	}

	addWorkflowStateSelector(): void {
		const selectorId = `selector_${this.selectorIdCounter++}`;
		this.workflowStateSelectors.push({
			id: selectorId,
			selectedWorkflowId: '',
			availableStates: [],
			selectedStates: []
		});
	}

	removeWorkflowStateSelector(selector: WorkflowStateSelector): void {
		this.workflowStateSelectors = this.workflowStateSelectors.filter(s => s.id !== selector.id);
	}

	onWorkflowSelected(selector: WorkflowStateSelector, workflowId: string): void {
		selector.selectedWorkflowId = workflowId;

		const workflow = this.selectedWorkflows.find(wf => wf.id === workflowId);
		if(workflow) {
			selector.availableStates = [...workflow.states];
			selector.selectedStates = [];
		}
	}

	onAddState(selector: WorkflowStateSelector, state: {id: string; name: string}): void {
		selector.availableStates = selector.availableStates.filter(s => s.id !== state.id);
		selector.selectedStates = [...selector.selectedStates, state];
	}

	onRemoveState(selector: WorkflowStateSelector, state: {id: string; name: string}): void {
		selector.selectedStates = selector.selectedStates.filter(s => s.id !== state.id);
		selector.availableStates = [...selector.availableStates, state];
	}

	getWorkflowName(workflowId: string): string {
		const workflow = this.selectedWorkflows.find(wf => wf.id === workflowId);
		return workflow?.name || workflowId;
	}

	onSave(): void {
		const result: any = {};

		const originalFormIds = this.data.scopeModel.formModelIds || [];
		const currentFormIds = this.selectedFormModels.map(fm => fm.id);
		if(JSON.stringify(originalFormIds.sort()) !== JSON.stringify(currentFormIds.sort())) {
			result.formModelIds = currentFormIds;
		}

		const originalDatasetIds = this.data.scopeModel.datasetModelIds || [];
		const currentDatasetIds = this.selectedDatasetModels.map(dm => dm.id);
		if(JSON.stringify(originalDatasetIds.sort()) !== JSON.stringify(currentDatasetIds.sort())) {
			result.datasetModelIds = currentDatasetIds;
		}

		const originalWorkflowIds = this.data.scopeModel.workflowIds || [];
		const currentWorkflowIds = this.selectedWorkflows.map(wf => wf.id);
		if(JSON.stringify(originalWorkflowIds.sort()) !== JSON.stringify(currentWorkflowIds.sort())) {
			result.workflowIds = currentWorkflowIds;
		}

		const currentSelections: WorkflowStateSelection[] = [];
		this.workflowStateSelectors.forEach(selector => {
			if(selector.selectedWorkflowId) {
				selector.selectedStates.forEach(state => {
					currentSelections.push({
						workflowId: selector.selectedWorkflowId,
						workflowStateId: state.id
					});
				});
			}
		});

		const originalSelections = this.data.workflowStateSelections || [];
		const originalSelectionsStr = JSON.stringify(
			originalSelections
				.map(s => `${s.workflowId}:${s.workflowStateId}`)
				.sort()
		);
		const currentSelectionsStr = JSON.stringify(
			currentSelections
				.map(s => `${s.workflowId}:${s.workflowStateId}`)
				.sort()
		);

		if(originalSelectionsStr !== currentSelectionsStr) {
			result.workflowStateSelections = currentSelections;
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
