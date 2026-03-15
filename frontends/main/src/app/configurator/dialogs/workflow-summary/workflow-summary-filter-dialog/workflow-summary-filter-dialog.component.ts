import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {BaseDialogComponent} from '../../base-dialog.component';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {WorkflowSummary} from '@core/model/workflow-summary';

export interface WorkflowSummaryFilterDialogData {
	workflowSummary: WorkflowSummary;
	eventModels: EventModel[];
}

@Component({
	selector: 'app-workflow-summary-filter-dialog',
	standalone: true,
	templateUrl: './workflow-summary-filter-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, DualListBoxComponent, FormsModule,
		MatSelectModule, ReactiveFormsModule, MatCheckboxModule]
})
export class WorkflowSummaryFilterDialogComponent extends BaseDialogComponent<WorkflowSummaryFilterDialogData> implements OnInit {
	form: FormGroup;

	availableEventModels: EventModel[] = [];
	selectedEventModels: EventModel[] = [];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<WorkflowSummaryFilterDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: WorkflowSummaryFilterDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const ws = this.data.workflowSummary;

		this.form = this.fb.group({
			filterExpectedEvents: [ws.filterExpectedEvents ?? false]
		});

		this.initializeEventModels();
	}

	private initializeEventModels(): void {
		const selectedIds = this.data.workflowSummary.eventModelIds || [];
		this.selectedEventModels = this.data.eventModels.filter(em => selectedIds.includes(em.eventModelId));
		this.availableEventModels = this.data.eventModels.filter(em => !selectedIds.includes(em.eventModelId));
	}

	addEventModel(eventModel: EventModel): void {
		this.availableEventModels = this.availableEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.selectedEventModels = [...this.selectedEventModels, eventModel];
	}

	removeEventModel(eventModel: EventModel): void {
		this.selectedEventModels = this.selectedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.availableEventModels = [...this.availableEventModels, eventModel];
	}

	onSave(): void {
		const ws = this.data.workflowSummary;
		const fv = this.form.value;
		const result: any = {};

		if(fv.filterExpectedEvents !== ws.filterExpectedEvents) {
			result.filterExpectedEvents = fv.filterExpectedEvents;
		}

		const originalEventModelIds = [...(ws.eventModelIds || [])].sort();
		const currentEventModelIds = this.selectedEventModels.map(em => em.eventModelId).sort();
		if(JSON.stringify(originalEventModelIds) !== JSON.stringify(currentEventModelIds)) {
			result.eventModelIds = currentEventModelIds;
		}

		this.dialogRef.close(result);
	}
}
