import {FieldModel} from '@core/model/field-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {Validator} from '@core/model/validator';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface FieldModelResourcesDialogData {
	fieldModel: FieldModel;
	availableValidators: Validator[];
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-field-model-resources-dialog',
	standalone: true,
	templateUrl: './field-model-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule]
})
export class FieldModelResourcesDialogComponent extends BaseDialogComponent<FieldModelResourcesDialogData> implements OnInit {
	availableValidators: Validator[] = [];
	selectedValidators: Validator[] = [];

	availableWorkflows: Workflow[] = [];
	selectedWorkflows: Workflow[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<FieldModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FieldModelResourcesDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.initializeValidators();
		this.initializeWorkflows();
	}

	private initializeValidators(): void {
		const selectedIds = this.data.fieldModel.validatorIds || [];
		this.selectedValidators = this.data.availableValidators.filter(v => selectedIds.includes(v.validatorId));
		this.availableValidators = this.data.availableValidators.filter(v => !selectedIds.includes(v.validatorId));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.fieldModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.workflowId));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.workflowId));
	}

	onAddValidator(validator: Validator): void {
		this.availableValidators = this.availableValidators.filter(v => v.validatorId !== validator.validatorId);
		this.selectedValidators = [...this.selectedValidators, validator];
	}

	onRemoveValidator(validator: Validator): void {
		this.selectedValidators = this.selectedValidators.filter(v => v.validatorId !== validator.validatorId);
		this.availableValidators = [...this.availableValidators, validator];
	}

	onAddWorkflow(workflow: Workflow): void {
		this.availableWorkflows = this.availableWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.selectedWorkflows = [...this.selectedWorkflows, workflow];
	}

	onRemoveWorkflow(workflow: Workflow): void {
		this.selectedWorkflows = this.selectedWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.availableWorkflows = [...this.availableWorkflows, workflow];
	}

	onSave(): void {
		const result: any = {};

		const originalValidatorIds = this.data.fieldModel.validatorIds || [];
		const currentValidatorIds = this.selectedValidators.map(v => v.validatorId);
		if(JSON.stringify(originalValidatorIds.sort()) !== JSON.stringify(currentValidatorIds.sort())) {
			result.validatorIds = currentValidatorIds;
		}

		const originalWorkflowIds = this.data.fieldModel.workflowIds || [];
		const currentWorkflowIds = this.selectedWorkflows.map(wf => wf.workflowId);
		if(JSON.stringify(originalWorkflowIds.sort()) !== JSON.stringify(currentWorkflowIds.sort())) {
			result.workflowIds = currentWorkflowIds;
		}

		this.dialogRef.close(result);
	}
}
