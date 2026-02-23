import {ScopeModel} from '@core/model/scope-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {WorkflowState} from '@core/model/workflow-state';
import {FormModel} from '@core/model/form-model';
import {DatasetModel} from '@core/model/dataset-model';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';

interface WorkflowStateSelector {
	id: string;
	selectedWorkflowId: string;
	availableStates: WorkflowState[];
	selectedStates: WorkflowState[];
}

export interface WorkflowStateSelection {
	workflowId: string;
	workflowStateId: string;
}

export interface ScopeModelResourcesDialogData {
	scopeModel: ScopeModel;
	availableForms: FormModel[];
	availableDatasets: DatasetModel[];
	availableWorkflows: Workflow[];
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
	styleUrls: ['../../dialog-shared.css']
})
export class ScopeModelResourcesDialogComponent implements OnInit {
	availableFormModels: FormModel[] = [];
	selectedFormModels: FormModel[] = [];

	availableDatasetModels: DatasetModel[] = [];
	selectedDatasetModels: DatasetModel[] = [];

	availableWorkflows: Workflow[] = [];
	selectedWorkflows: Workflow[] = [];

	workflowStateSelectors: WorkflowStateSelector[] = [];
	private selectorIdCounter = 0;

	constructor(
		public languageService: LanguageService,
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
		this.selectedFormModels = this.data.availableForms.filter(fm => selectedIds.includes(fm.formModelId));
		this.availableFormModels = this.data.availableForms.filter(fm => !selectedIds.includes(fm.formModelId));
	}

	private initializeDatasetModels(): void {
		const selectedIds = this.data.scopeModel.datasetModelIds || [];
		this.selectedDatasetModels = this.data.availableDatasets.filter(dm => selectedIds.includes(dm.datasetModelId));
		this.availableDatasetModels = this.data.availableDatasets.filter(dm => !selectedIds.includes(dm.datasetModelId));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.scopeModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.workflowId));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.workflowId));
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
			const workflow = this.selectedWorkflows.find(wf => wf.workflowId === workflowId);
			if(workflow) {
				const selectedStates = workflow.states.filter(st => stateIds.includes(st.workflowStateId));
				const availableStates = workflow.states.filter(st => !stateIds.includes(st.workflowStateId));

				this.workflowStateSelectors.push({
					id: `selector_${this.selectorIdCounter++}`,
					selectedWorkflowId: workflowId,
					availableStates,
					selectedStates
				});
			}
		});
	}

	onAddFormModel(formModel: FormModel): void {
		this.availableFormModels = this.availableFormModels.filter(fm => fm.formModelId !== formModel.formModelId);
		this.selectedFormModels = [...this.selectedFormModels, formModel];
	}

	onRemoveFormModel(formModel: FormModel): void {
		this.selectedFormModels = this.selectedFormModels.filter(fm => fm.formModelId !== formModel.formModelId);
		this.availableFormModels = [...this.availableFormModels, formModel];
	}

	onAddDatasetModel(datasetModel: DatasetModel): void {
		this.availableDatasetModels = this.availableDatasetModels.filter(dm => dm.datasetModelId !== datasetModel.datasetModelId);
		this.selectedDatasetModels = [...this.selectedDatasetModels, datasetModel];
	}

	onRemoveDatasetModel(datasetModel: DatasetModel): void {
		this.selectedDatasetModels = this.selectedDatasetModels.filter(dm => dm.datasetModelId !== datasetModel.datasetModelId);
		this.availableDatasetModels = [...this.availableDatasetModels, datasetModel];
	}

	onAddWorkflow(workflow: Workflow): void {
		this.availableWorkflows = this.availableWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.selectedWorkflows = [...this.selectedWorkflows, workflow];
	}

	onRemoveWorkflow(workflow: Workflow): void {
		this.selectedWorkflows = this.selectedWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.availableWorkflows = [...this.availableWorkflows, workflow];
		this.workflowStateSelectors = this.workflowStateSelectors.filter(
			selector => selector.selectedWorkflowId !== workflow.workflowId
		);
	}

	addWorkflowStateSelector(): void {
		this.workflowStateSelectors = [...this.workflowStateSelectors, {
			id: `selector_${this.selectorIdCounter++}`,
			selectedWorkflowId: '',
			availableStates: [],
			selectedStates: []
		}];
	}

	removeWorkflowStateSelector(selector: WorkflowStateSelector): void {
		this.workflowStateSelectors = this.workflowStateSelectors.filter(s => s.id !== selector.id);
	}

	onWorkflowSelected(selector: WorkflowStateSelector, workflowId: string): void {
		const workflow = this.selectedWorkflows.find(wf => wf.workflowId === workflowId);
		const index = this.workflowStateSelectors.indexOf(selector);

		const alreadySelectedStateIds = this.workflowStateSelectors
			.filter(s => s.id !== selector.id && s.selectedWorkflowId === workflowId)
			.flatMap(s => s.selectedStates.map(st => st.workflowStateId));

		const availableStates = workflow
			? workflow.states.filter(st => !alreadySelectedStateIds.includes(st.workflowStateId))
			: [];

		this.workflowStateSelectors[index] = {
			...selector,
			selectedWorkflowId: workflowId,
			availableStates,
			selectedStates: []
		};
		this.workflowStateSelectors = [...this.workflowStateSelectors];
	}

	onAddState(selector: WorkflowStateSelector, state: WorkflowState): void {
		selector.availableStates = selector.availableStates.filter(s => s.workflowStateId !== state.workflowStateId);
		selector.selectedStates = [...selector.selectedStates, state];

		this.workflowStateSelectors = this.workflowStateSelectors.map(s => {
			if(s.id !== selector.id && s.selectedWorkflowId === selector.selectedWorkflowId) {
				return {...s, availableStates: s.availableStates.filter(st => st.workflowStateId !== state.workflowStateId)};
			}
			return s;
		});
	}

	onRemoveState(selector: WorkflowStateSelector, state: WorkflowState): void {
		selector.selectedStates = selector.selectedStates.filter(s => s.workflowStateId !== state.workflowStateId);
		selector.availableStates = [...selector.availableStates, state];

		this.workflowStateSelectors = this.workflowStateSelectors.map(s => {
			if(s.id !== selector.id && s.selectedWorkflowId === selector.selectedWorkflowId) {
				return {...s, availableStates: [...s.availableStates, state]};
			}
			return s;
		});
	}

	onSave(): void {
		const result: any = {};

		const originalFormIds = [...(this.data.scopeModel.formModelIds || [])].sort();
		const currentFormIds = [...this.selectedFormModels.map(fm => fm.formModelId)].sort();
		if(JSON.stringify(originalFormIds) !== JSON.stringify(currentFormIds)) {
			result.formModelIds = this.selectedFormModels.map(fm => fm.formModelId);
		}

		const originalDatasetIds = [...(this.data.scopeModel.datasetModelIds || [])].sort();
		const currentDatasetIds = [...this.selectedDatasetModels.map(dm => dm.datasetModelId)].sort();
		if(JSON.stringify(originalDatasetIds) !== JSON.stringify(currentDatasetIds)) {
			result.datasetModelIds = this.selectedDatasetModels.map(dm => dm.datasetModelId);
		}

		const originalWorkflowIds = [...(this.data.scopeModel.workflowIds || [])].sort();
		const currentWorkflowIds = [...this.selectedWorkflows.map(wf => wf.workflowId)].sort();
		if(JSON.stringify(originalWorkflowIds) !== JSON.stringify(currentWorkflowIds)) {
			result.workflowIds = this.selectedWorkflows.map(wf => wf.workflowId);
		}

		const currentSelections: WorkflowStateSelection[] = this.workflowStateSelectors
			.filter(selector => selector.selectedWorkflowId)
			.flatMap(selector => selector.selectedStates.map(state => ({
				workflowId: selector.selectedWorkflowId,
				workflowStateId: state.workflowStateId
			})));

		result.workflowStateIds = currentSelections.map(s => s.workflowStateId);

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
