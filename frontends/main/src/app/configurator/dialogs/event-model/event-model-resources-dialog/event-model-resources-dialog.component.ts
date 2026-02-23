import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {DatasetModel} from '@core/model/dataset-model';
import {Workflow} from '@core/model/workflow';
import {FormModel} from '@core/model/form-model';

export interface EventModelResourcesDialogData {
	eventModel: EventModel;
	availableFormModels: FormModel[];
	availableDatasetModels: DatasetModel[];
	availableWorkflows: Workflow[];
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
	styleUrls: ['../../dialog-shared.css']
})
export class EventModelResourcesDialogComponent implements OnInit {
	availableFormModels: FormModel[] = [];
	selectedFormModels: FormModel[] = [];

	availableDatasetModels: DatasetModel[] = [];
	selectedDatasetModels: DatasetModel[] = [];

	availableWorkflows: Workflow[] = [];
	selectedWorkflows: Workflow[] = [];

	constructor(
		public languageService: LanguageService,
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
		this.selectedFormModels = this.data.availableFormModels.filter(fm => selectedIds.includes(fm.formModelId));
		this.availableFormModels = this.data.availableFormModels.filter(fm => !selectedIds.includes(fm.formModelId));
	}

	private initializeDatasetModels(): void {
		const selectedIds = this.data.eventModel.datasetModelIds || [];
		this.selectedDatasetModels = this.data.availableDatasetModels.filter(dm => selectedIds.includes(dm.datasetModelId));
		this.availableDatasetModels = this.data.availableDatasetModels.filter(dm => !selectedIds.includes(dm.datasetModelId));
	}

	private initializeWorkflows(): void {
		const selectedIds = this.data.eventModel.workflowIds || [];
		this.selectedWorkflows = this.data.availableWorkflows.filter(wf => selectedIds.includes(wf.workflowId));
		this.availableWorkflows = this.data.availableWorkflows.filter(wf => !selectedIds.includes(wf.workflowId));
	}

	addFormModel(formModel: FormModel): void {
		this.availableFormModels = this.availableFormModels.filter(fm => fm.formModelId !== formModel.formModelId);
		this.selectedFormModels = [...this.selectedFormModels, formModel];
	}

	removeFormModel(formModel: FormModel): void {
		this.selectedFormModels = this.selectedFormModels.filter(fm => fm.formModelId !== formModel.formModelId);
		this.availableFormModels = [...this.availableFormModels, formModel];
	}

	addDatasetModel(datasetModel: DatasetModel): void {
		this.availableDatasetModels = this.availableDatasetModels.filter(dm => dm.datasetModelId !== datasetModel.datasetModelId);
		this.selectedDatasetModels = [...this.selectedDatasetModels, datasetModel];
	}

	removeDatasetModel(datasetModel: DatasetModel): void {
		this.selectedDatasetModels = this.selectedDatasetModels.filter(dm => dm.datasetModelId !== datasetModel.datasetModelId);
		this.availableDatasetModels = [...this.availableDatasetModels, datasetModel];
	}

	addWorkflow(workflow: Workflow): void {
		this.availableWorkflows = this.availableWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.selectedWorkflows = [...this.selectedWorkflows, workflow];
	}

	removeWorkflow(workflow: Workflow): void {
		this.selectedWorkflows = this.selectedWorkflows.filter(wf => wf.workflowId !== workflow.workflowId);
		this.availableWorkflows = [...this.availableWorkflows, workflow];
	}

	onSave(): void {
		const result: any = {};

		const originalFormIds = this.data.eventModel.formModelIds || [];
		const currentFormIds = this.selectedFormModels.map(fm => fm.formModelId);
		if(JSON.stringify(originalFormIds.sort()) !== JSON.stringify(currentFormIds.sort())) {
			result.formModelIds = currentFormIds;
		}

		const originalDatasetIds = this.data.eventModel.datasetModelIds || [];
		const currentDatasetIds = this.selectedDatasetModels.map(dm => dm.datasetModelId);
		if(JSON.stringify(originalDatasetIds.sort()) !== JSON.stringify(currentDatasetIds.sort())) {
			result.datasetModelIds = currentDatasetIds;
		}

		const originalWorkflowIds = this.data.eventModel.workflowIds || [];
		const currentWorkflowIds = this.selectedWorkflows.map(wf => wf.workflowId);
		if(JSON.stringify(originalWorkflowIds.sort()) !== JSON.stringify(currentWorkflowIds.sort())) {
			result.workflowIds = currentWorkflowIds;
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
