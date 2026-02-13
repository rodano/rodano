import {FieldModel} from '@core/model/field-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export interface FieldModelResourcesDialogData {
	fieldModel: FieldModel;
	availableValidators: {id: string; name: string}[];
	availableWorkflows: {id: string; name: string}[];
}

@Component({
	selector: 'app-field-model-resources-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule
	],
	templateUrl: './field-model-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class FieldModelResourcesDialogComponent implements OnInit {
	availableValidators: {id: string; name: string}[] = [];
	selectedValidators: {id: string; name: string}[] = [];

	availableWorkflows: {id: string; name: string}[] = [];
	selectedWorkflows: {id: string; name: string}[] = [];

	constructor(
		private dialogRef: MatDialogRef<FieldModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelResourcesDialogData
	) {}

	ngOnInit(): void {
		this.initializeValidators();
		this.initializeWorkflows();
	}

	private initializeValidators(): void {
		const selectedIds = this.data.fieldModel.validatorIds || [];
		this.selectedValidators = this.data.availableValidators.filter(v => selectedIds.includes(v.id));
		this.availableValidators = this.data.availableValidators.filter(v => !selectedIds.includes(v.id));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.fieldModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.id));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.id));
	}

	onAddValidator(validator: {id: string; name: string}): void {
		this.availableValidators = this.availableValidators.filter(v => v.id !== validator.id);
		this.selectedValidators = [...this.selectedValidators, validator];
	}

	onRemoveValidator(validator: {id: string; name: string}): void {
		this.selectedValidators = this.selectedValidators.filter(v => v.id !== validator.id);
		this.availableValidators = [...this.availableValidators, validator];
	}

	onAddWorkflow(workflow: {id: string; name: string}): void {
		this.availableWorkflows = this.availableWorkflows.filter(wf => wf.id !== workflow.id);
		this.selectedWorkflows = [...this.selectedWorkflows, workflow];
	}

	onRemoveWorkflow(workflow: {id: string; name: string}): void {
		this.selectedWorkflows = this.selectedWorkflows.filter(wf => wf.id !== workflow.id);
		this.availableWorkflows = [...this.availableWorkflows, workflow];
	}

	onSave(): void {
		const result: any = {};

		const originalValidatorIds = this.data.fieldModel.validatorIds || [];
		const currentValidatorIds = this.selectedValidators.map(v => v.id);
		if(JSON.stringify(originalValidatorIds.sort()) !== JSON.stringify(currentValidatorIds.sort())) {
			result.validatorIds = currentValidatorIds;
		}

		const originalWorkflowIds = this.data.fieldModel.workflowIds || [];
		const currentWorkflowIds = this.selectedWorkflows.map(wf => wf.id);
		if(JSON.stringify(originalWorkflowIds.sort()) !== JSON.stringify(currentWorkflowIds.sort())) {
			result.workflowIds = currentWorkflowIds;
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
