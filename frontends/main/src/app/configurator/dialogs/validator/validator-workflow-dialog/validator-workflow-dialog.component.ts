import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {Validator} from '@core/model/validator';

export interface ValidatorWorkflowConfiguration {
	workflowId: string;
	invalidWorkflowStateIds: string[];
	validWorkflowStateIds: string[];
}

export interface ValidatorWorkflowDialogData {
	validator: Validator;
	availableWorkflows: {id: string; name: string; states: {id: string; name: string}[]}[];
	currentConfiguration: ValidatorWorkflowConfiguration | null;
}

@Component({
	selector: 'app-validator-workflow-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatSelectModule
	],
	templateUrl: './validator-workflow-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class ValidatorWorkflowDialogComponent implements OnInit {
	selectedWorkflowId = '';
	availableInvalidStates: {id: string; name: string}[] = [];
	selectedInvalidStates: {id: string; name: string}[] = [];
	availableValidStates: {id: string; name: string}[] = [];
	selectedValidStates: {id: string; name: string}[] = [];

	constructor(
		private dialogRef: MatDialogRef<ValidatorWorkflowDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ValidatorWorkflowDialogData
	) {}

	ngOnInit(): void {
		this.initializeConfiguration();
	}

	private initializeConfiguration(): void {
		const config = this.data.currentConfiguration;

		if(config && config.workflowId) {
			this.selectedWorkflowId = config.workflowId;

			const workflow = this.data.availableWorkflows.find(wf => wf.id === config.workflowId);
			if(workflow) {
				this.selectedInvalidStates = workflow.states.filter(
					state => config.invalidWorkflowStateIds.includes(state.id)
				);
				this.availableInvalidStates = workflow.states.filter(
					state => !config.invalidWorkflowStateIds.includes(state.id)
				);

				this.selectedValidStates = workflow.states.filter(
					state => config.validWorkflowStateIds.includes(state.id)
				);
				this.availableValidStates = workflow.states.filter(
					state => !config.validWorkflowStateIds.includes(state.id)
				);
			}
		}
	}

	onWorkflowSelected(workflowId: string): void {
		this.selectedWorkflowId = workflowId;

		const workflow = this.data.availableWorkflows.find(wf => wf.id === workflowId);
		if(workflow) {
			this.availableInvalidStates = [...workflow.states];
			this.selectedInvalidStates = [];
			this.availableValidStates = [...workflow.states];
			this.selectedValidStates = [];
		}
	}

	onAddInvalidState(state: {id: string; name: string}): void {
		this.availableInvalidStates = this.availableInvalidStates.filter(s => s.id !== state.id);
		this.selectedInvalidStates = [...this.selectedInvalidStates, state];
	}

	onRemoveInvalidState(state: {id: string; name: string}): void {
		this.selectedInvalidStates = this.selectedInvalidStates.filter(s => s.id !== state.id);
		this.availableInvalidStates = [...this.availableInvalidStates, state];
	}

	onAddValidState(state: {id: string; name: string}): void {
		this.availableValidStates = this.availableValidStates.filter(s => s.id !== state.id);
		this.selectedValidStates = [...this.selectedValidStates, state];
	}

	onRemoveValidState(state: {id: string; name: string}): void {
		this.selectedValidStates = this.selectedValidStates.filter(s => s.id !== state.id);
		this.availableValidStates = [...this.availableValidStates, state];
	}

	clearConfiguration(): void {
		this.selectedWorkflowId = '';
		this.availableInvalidStates = [];
		this.selectedInvalidStates = [];
		this.availableValidStates = [];
		this.selectedValidStates = [];
	}

	onSave(): void {
		const result: any = {};

		const hasConfiguration = this.selectedWorkflowId && (this.selectedInvalidStates.length > 0 || this.selectedValidStates.length > 0);

		const currentConfig: ValidatorWorkflowConfiguration | null = hasConfiguration
			? {
				workflowId: this.selectedWorkflowId,
				invalidWorkflowStateIds: this.selectedInvalidStates.map(s => s.id),
				validWorkflowStateIds: this.selectedValidStates.map(s => s.id)
			}
			: null;

		const originalConfig = this.data.currentConfiguration;

		const configChanged = JSON.stringify(originalConfig) !== JSON.stringify(currentConfig);

		if(configChanged) {
			result.workflowConfiguration = currentConfig;
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	getWorkflowName(workflowId: string): string {
		const workflow = this.data.availableWorkflows.find(wf => wf.id === workflowId);
		return workflow?.name || workflowId;
	}
}
