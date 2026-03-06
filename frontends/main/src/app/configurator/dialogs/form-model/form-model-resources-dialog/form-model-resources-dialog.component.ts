import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {FormModel} from '@core/model/form-model';
import {Workflow} from '@core/model/workflow';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import { DualListBoxComponent } from '../../dual-list-box/dual-list-box.component';

export interface FormModelResourcesDialogData {
	formModel: FormModel;
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-form-model-resources-dialog',
	standalone: true,
	templateUrl: './form-model-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, DualListBoxComponent]
})
export class FormModelResourcesDialogComponent extends BaseDialogComponent<FormModelResourcesDialogData> implements OnInit {
	availableWorkflows: Workflow[] = [];
	selectedWorkflows: Workflow[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<FormModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FormModelResourcesDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const selectedIds = this.data.formModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.workflowId));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.workflowId));
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

		const originalWorkflowIds = [...(this.data.formModel.workflowIds || [])].sort();
		const currentWorkflowIds = [...this.selectedWorkflows.map(wf => wf.workflowId)].sort();
		if(JSON.stringify(originalWorkflowIds) !== JSON.stringify(currentWorkflowIds)) {
			result.workflowIds = this.selectedWorkflows.map(wf => wf.workflowId);
		}

		this.dialogRef.close(result);
	}
}
