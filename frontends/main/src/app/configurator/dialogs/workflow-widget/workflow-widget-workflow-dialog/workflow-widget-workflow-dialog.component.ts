import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {WorkflowState} from '@core/model/workflow-state';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {Workflow} from '@core/model/workflow';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';

export interface WorkflowWidgetWorkflowDialogData {
	workflowWidget: WorkflowWidgetConfig;
	availableWorkflows: Workflow[];
}

interface WorkflowStateSelector {
	id: string;
	selectedWorkflowId: string;
	availableStates: WorkflowState[];
	selectedStates: WorkflowState[];
}

@Component({
	selector: 'app-workflow-widget-workflow-dialog',
	standalone: true,
	templateUrl: './workflow-widget-workflow-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, DualListBoxComponent]
})
export class WorkflowWidgetWorkflowDialogComponent extends BaseDialogComponent<WorkflowWidgetWorkflowDialogData> implements OnInit {
	workflowStateSelectors: WorkflowStateSelector[] = [];
	private selectorIdCounter = 0;

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowWidgetWorkflowDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowWidgetWorkflowDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const stateIds = this.data.workflowWidget.workflowStateIds ?? [];

		const groupedByWorkflow = new Map<string, string[]>();
		for(const stateId of stateIds) {
			const workflow = this.data.availableWorkflows.find(wf =>
				wf.states.some(s => s.workflowStateId === stateId)
			);
			if(workflow) {
				if(!groupedByWorkflow.has(workflow.workflowId)) {
					groupedByWorkflow.set(workflow.workflowId, []);
				}
				groupedByWorkflow.get(workflow.workflowId)!.push(stateId);
			}
		}

		groupedByWorkflow.forEach((ids, workflowId) => {
			const workflow = this.data.availableWorkflows.find(wf => wf.workflowId === workflowId);
			if(workflow) {
				this.workflowStateSelectors.push({
					id: `selector_${this.selectorIdCounter++}`,
					selectedWorkflowId: workflowId,
					selectedStates: workflow.states.filter(s => ids.includes(s.workflowStateId)),
					availableStates: workflow.states.filter(s => !ids.includes(s.workflowStateId))
				});
			}
		});
	}

	addSelector(): void {
		this.workflowStateSelectors = [...this.workflowStateSelectors, {
			id: `selector_${this.selectorIdCounter++}`,
			selectedWorkflowId: '',
			availableStates: [],
			selectedStates: []
		}];
	}

	removeSelector(selector: WorkflowStateSelector): void {
		this.workflowStateSelectors = this.workflowStateSelectors.filter(s => s.id !== selector.id);
	}

	onWorkflowSelected(selector: WorkflowStateSelector, workflowId: string): void {
		const workflow = this.data.availableWorkflows.find(wf => wf.workflowId === workflowId);
		const alreadySelectedStateIds = this.workflowStateSelectors
			.filter(s => s.id !== selector.id && s.selectedWorkflowId === workflowId)
			.flatMap(s => s.selectedStates.map(st => st.workflowStateId));
		const index = this.workflowStateSelectors.indexOf(selector);
		this.workflowStateSelectors[index] = {
			...selector,
			selectedWorkflowId: workflowId,
			selectedStates: [],
			availableStates: workflow
				? workflow.states.filter(st => !alreadySelectedStateIds.includes(st.workflowStateId))
				: []
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

	getWorkflowLabel(workflow: Workflow): string {
		return this.languageService.getTranslatedName(workflow.shortname) || workflow.id;
	}

	onSave(): void {
		const workflowStateIds = this.workflowStateSelectors
			.filter(s => s.selectedWorkflowId)
			.flatMap(s => s.selectedStates.map(st => st.workflowStateId));
		this.dialogRef.close({workflowStateIds});
	}
}
