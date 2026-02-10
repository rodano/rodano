import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export interface EventModelResourcesDialogData {
	eventModel: EventModel;
	availableFormModels: {id: string; name: string}[];
	availableDatasetModels: {id: string; name: string}[];
	availableWorkflows: {id: string; name: string}[];
}

@Component({
	selector: 'app-event-model-resources-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule
	],
	templateUrl: './event-model-resources-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class EventModelResourcesDialogComponent implements OnInit {
	availableFormModels: {id: string; name: string}[] = [];
	selectedFormModels: {id: string; name: string}[] = [];

	availableDatasetModels: {id: string; name: string}[] = [];
	selectedDatasetModels: {id: string; name: string}[] = [];

	availableWorkflows: {id: string; name: string}[] = [];
	selectedWorkflows: {id: string; name: string}[] = [];

	constructor(
		private dialogRef: MatDialogRef<EventModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelResourcesDialogData
	) {}

	ngOnInit(): void {
		this.initializeFormModels();
		this.initializeDatasetModels();
		this.initializeWorkflows();
	}

	private initializeFormModels(): void {
		const selectedIds = this.data.eventModel.formModelIds || [];
		this.selectedFormModels = this.data.availableFormModels.filter(fm => selectedIds.includes(fm.id));
		this.availableFormModels = this.data.availableFormModels.filter(fm => !selectedIds.includes(fm.id));
	}

	private initializeDatasetModels(): void {
		const selectedIds = this.data.eventModel.datasetModelIds || [];
		this.selectedDatasetModels = this.data.availableDatasetModels.filter(dm => selectedIds.includes(dm.id));
		this.availableDatasetModels = this.data.availableDatasetModels.filter(dm => !selectedIds.includes(dm.id));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.eventModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.id));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.id));
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

		const originalFormIds = this.data.eventModel.formModelIds || [];
		const currentFormIds = this.selectedFormModels.map(fm => fm.id);
		if(JSON.stringify(originalFormIds.sort()) !== JSON.stringify(currentFormIds.sort())) {
			result.formModelIds = currentFormIds;
		}

		const originalDatasetIds = this.data.eventModel.datasetModelIds || [];
		const currentDatasetIds = this.selectedDatasetModels.map(dm => dm.id);
		if(JSON.stringify(originalDatasetIds.sort()) !== JSON.stringify(currentDatasetIds.sort())) {
			result.datasetModelIds = currentDatasetIds;
		}

		const originalWorkflowIds = this.data.eventModel.workflowIds || [];
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
