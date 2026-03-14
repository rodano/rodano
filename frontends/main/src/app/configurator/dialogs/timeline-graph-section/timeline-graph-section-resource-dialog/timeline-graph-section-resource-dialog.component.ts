import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {DatasetModel} from '@core/model/dataset-model';
import {BaseDialogComponent} from '../../base-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {FieldModel} from '@core/model/field-model';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {DualListBoxComponent} from '../../dual-list-box/dual-list-box.component';
import {MatSelectModule} from '@angular/material/select';
import {MatCheckboxModule} from '@angular/material/checkbox';

export interface TimelineGraphSectionResourcesDialogData {
	section: TimelineGraphSection;
	eventModels: EventModel[];
	fieldModels: FieldModel[];
	datasetModels: DatasetModel[];
}

@Component({
	selector: 'app-timeline-graph-section-resources-dialog',
	standalone: true,
	templateUrl: './timeline-graph-section-resource-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, DualListBoxComponent, FormsModule,
		MatSelectModule, ReactiveFormsModule, MatCheckboxModule]
})
export class TimelineGraphSectionResourcesDialogComponent extends BaseDialogComponent<TimelineGraphSectionResourcesDialogData> implements OnInit {
	form: FormGroup;

	availableEventModels: EventModel[] = [];
	selectedEventModels: EventModel[] = [];

	availableMetaFieldModels: FieldModel[] = [];
	selectedMetaFieldModels: FieldModel[] = [];

	constructor(
		public languageService: LanguageService,
		private fb: FormBuilder,
		dialogRef: MatDialogRef<TimelineGraphSectionResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: TimelineGraphSectionResourcesDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		const tgs = this.data.section;

		this.form = this.fb.group({
			useScopePaths: [tgs.useScopePaths ?? false],
			hideExpectedEvent: [tgs.hideExpectedEvent ?? false],
			hideDoneEvent: [tgs.hideDoneEvent ?? false],
			datasetModelId: [tgs.datasetModelId || null],
			dateFieldId: [tgs.dateFieldId || null],
			endDateFieldId: [tgs.endDateFieldId || null],
			valueFieldId: [tgs.valueFieldId || null],
			labelFieldId: [tgs.labelFieldId || null]
		});

		this.form.get('datasetModelId')!.valueChanges.subscribe(() => {
			this.form.patchValue({
				dateFieldId: null,
				endDateFieldId: null,
				valueFieldId: null,
				labelFieldId: null
			});
			this.initializeMetaFieldModels();
		});

		this.initializeEventModels();
		this.initializeMetaFieldModels();
	}

	private initializeEventModels(): void {
		const selectedIds = this.data.section.eventModelIds || [];
		this.selectedEventModels = this.data.eventModels.filter(em => selectedIds.includes(em.eventModelId));
		this.availableEventModels = this.data.eventModels.filter(em => !selectedIds.includes(em.eventModelId));
	}

	private initializeMetaFieldModels(): void {
		const datasetModelId = this.form.get('datasetModelId')?.value;
		const selectedIds = this.data.section.metaFieldIds || [];

		const fieldsForDataset = datasetModelId
			? this.data.fieldModels.filter(fm => fm.datasetModelId === datasetModelId)
			: [];

		this.selectedMetaFieldModels = fieldsForDataset.filter(fm => selectedIds.includes(fm.fieldModelId));
		this.availableMetaFieldModels = fieldsForDataset.filter(fm => !selectedIds.includes(fm.fieldModelId));
	}

	get filteredFieldModels(): FieldModel[] {
		const datasetModelId = this.form.get('datasetModelId')?.value;
		if(!datasetModelId) {
			return [];
		}
		return this.data.fieldModels.filter(fm => fm.datasetModelId === datasetModelId);
	}

	addEventModel(eventModel: EventModel): void {
		this.availableEventModels = this.availableEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.selectedEventModels = [...this.selectedEventModels, eventModel];
	}

	removeEventModel(eventModel: EventModel): void {
		this.selectedEventModels = this.selectedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.availableEventModels = [...this.availableEventModels, eventModel];
	}

	addFieldModel(fieldModel: FieldModel): void {
		this.availableMetaFieldModels = this.availableMetaFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.selectedMetaFieldModels = [...this.selectedMetaFieldModels, fieldModel];
	}

	removeFieldModel(fieldModel: FieldModel): void {
		this.selectedMetaFieldModels = this.selectedMetaFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.availableMetaFieldModels = [...this.availableMetaFieldModels, fieldModel];
	}

	onSave(): void {
		const s = this.data.section;
		const fv = this.form.value;
		const result: any = {};

		if(fv.useScopePaths !== s.useScopePaths) {
			result.useScopePaths = fv.useScopePaths;
		}

		if(fv.hideExpectedEvent !== s.hideExpectedEvent) {
			result.hideExpectedEvent = fv.hideExpectedEvent;
		}
		if(fv.hideDoneEvent !== s.hideDoneEvent) {
			result.hideDoneEvent = fv.hideDoneEvent;
		}
		if((fv.datasetModelId || null) !== (s.datasetModelId || null)) {
			result.datasetModelId = fv.datasetModelId;
		}
		if((fv.dateFieldId || null) !== (s.dateFieldId || null)) {
			result.dateFieldId = fv.dateFieldId;
		}
		if((fv.endDateFieldId || null) !== (s.endDateFieldId || null)) {
			result.endDateFieldId = fv.endDateFieldId;
		}
		if((fv.valueFieldId || null) !== (s.valueFieldId || null)) {
			result.valueFieldId = fv.valueFieldId;
		}
		if((fv.labelFieldId || null) !== (s.labelFieldId || null)) {
			result.labelFieldId = fv.labelFieldId;
		}

		const originalEventModelIds = [...(s.eventModelIds || [])].sort();
		const currentEventModelIds = this.selectedEventModels.map(em => em.eventModelId).sort();
		if(JSON.stringify(originalEventModelIds) !== JSON.stringify(currentEventModelIds)) {
			result.eventModelIds = currentEventModelIds;
		}

		const originalMetaFieldIds = [...(s.metaFieldIds || [])].sort();
		const currentMetaFieldIds = this.selectedMetaFieldModels.map(fm => fm.fieldModelId).sort();
		if(JSON.stringify(originalMetaFieldIds) !== JSON.stringify(currentMetaFieldIds)) {
			result.metaFieldIds = currentMetaFieldIds;
		}

		this.dialogRef.close(result);
	}
}
