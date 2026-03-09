import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {Report} from '@core/model/report';
import {FieldModel} from '@core/model/field-model';
import {BaseDialogComponent} from '../../base-dialog.component';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';
import {DatasetModel} from '@core/model/dataset-model';
import {Workflow} from '@core/model/workflow';

export interface ReportResourcesDialogData {
	report: Report;
	availableFieldModels: FieldModel[];
	datasetModels: DatasetModel[];
	workflows: Workflow[];
}

@Component({
	selector: 'app-report-resources-dialog',
	standalone: true,
	templateUrl: './report-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, DualListBoxComponent]
})
export class ReportResourcesDialogComponent extends BaseDialogComponent<ReportResourcesDialogData> implements OnInit {
	availableFieldModels: FieldModel[] = [];
	selectedFieldModels: FieldModel[] = [];
	selectedDatasetModelId: string | null = null;
	selectedWorkflowId: string | null = null;

	constructor(
		public languageService: LanguageService,
		dialogRef: MatDialogRef<ReportResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ReportResourcesDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.selectedWorkflowId = this.data.report.workflowId || null;
		this.selectedDatasetModelId = this.data.report.datasetModelId || null;
		this.initializeFieldModels();
	}

	private initializeFieldModels(): void {
		const selectedIds = this.data.report.fieldModelIds || [];
		this.selectedFieldModels = this.data.availableFieldModels.filter(fm => selectedIds.includes(fm.fieldModelId));
		this.availableFieldModels = this.data.availableFieldModels.filter(fm => !selectedIds.includes(fm.fieldModelId));
	}

	onAddFieldModel(fieldModel: FieldModel): void {
		this.availableFieldModels = this.availableFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.selectedFieldModels = [...this.selectedFieldModels, fieldModel];
	}

	onRemoveFieldModel(fieldModel: FieldModel): void {
		this.selectedFieldModels = this.selectedFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.availableFieldModels = [...this.availableFieldModels, fieldModel];
	}

	get filteredAvailableFieldModels(): FieldModel[] {
		if(!this.selectedDatasetModelId) {
			return [];
		}
		return this.availableFieldModels.filter(fm => fm.datasetModelId === this.selectedDatasetModelId);
	}

	onDatasetModelChange(datasetModelId: string | null): void {
		this.selectedDatasetModelId = datasetModelId;
		if(datasetModelId) {
			const evicted = this.selectedFieldModels.filter(fm => fm.datasetModelId !== datasetModelId);
			if(evicted.length) {
				this.selectedFieldModels = this.selectedFieldModels.filter(fm => fm.datasetModelId === datasetModelId);
				this.availableFieldModels = [...this.availableFieldModels, ...evicted];
			}
		}
	}

	onWorkflowChange(workflowId: string | null): void {
		this.selectedWorkflowId = workflowId;
	}

	onSave(): void {
		const result: any = {};

		if(this.selectedDatasetModelId !== (this.data.report.datasetModelId || null)) {
			result.datasetModelId = this.selectedDatasetModelId;
		}

		if(this.selectedWorkflowId !== (this.data.report.workflowId || null)) {
			result.workflowId = this.selectedWorkflowId;
		}

		const originalFormIds = [...(this.data.report.fieldModelIds || [])].sort();
		const currentFormIds = [...this.selectedFieldModels.map(fm => fm.fieldModelId)].sort();
		if(JSON.stringify(originalFormIds) !== JSON.stringify(currentFormIds)) {
			result.fieldModelIds = this.selectedFieldModels.map(fm => fm.fieldModelId);
		}

		this.dialogRef.close(result);
	}
}
