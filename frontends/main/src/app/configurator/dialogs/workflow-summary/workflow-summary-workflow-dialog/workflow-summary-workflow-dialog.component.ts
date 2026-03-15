import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {Workflow} from '@core/model/workflow';
import {WorkflowSummary} from '@core/model/workflow-summary';
import { DualListBoxComponent } from '../../dual-list-box/dual-list-box.component';

export interface WorkflowSummaryWorkflowDialogData {
	workflowSummary: WorkflowSummary;
	availableWorkflows: Workflow[];
}

@Component({
	selector: 'app-workflow-summary-workflow-dialog',
	standalone: true,
	templateUrl: './workflow-summary-workflow-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, DualListBoxComponent]
})
export class WorkflowSummaryWorkflowDialogComponent extends BaseDialogComponent<WorkflowSummaryWorkflowDialogData> implements OnInit {
	availableWorkflows: Workflow[] = [];
	selectedWorkflows: Workflow[] = [];

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<WorkflowSummaryWorkflowDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowSummaryWorkflowDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.initializeWorkflows();
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.workflowSummary.workflowIds || [];
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

		const originalFormIds = [...(this.data.workflowSummary.workflowIds || [])].sort();
		const currentFormIds = [...this.selectedWorkflows.map(wf => wf.workflowId)].sort();
		if(JSON.stringify(originalFormIds) !== JSON.stringify(currentFormIds)) {
			result.workflowIds = this.selectedWorkflows.map(wf => wf.workflowId);
		}

		this.dialogRef.close(result);
	}
}
