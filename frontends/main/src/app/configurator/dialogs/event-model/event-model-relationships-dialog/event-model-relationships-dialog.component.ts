import {EventModel} from '@core/model/event-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';

export interface EventModelRelationshipsDialogData {
	eventModel: EventModel;
	availableEventModels: EventModel[];
}

@Component({
	selector: 'app-event-model-relationships-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule
	],
	templateUrl: './event-model-relationships-dialog.component.html',
	styleUrls: ['../../shared-dialog-styles.css']
})
export class EventModelRelationshipsDialogComponent implements OnInit {
	availableBlockedEventModels: EventModel[] = [];
	selectedBlockedEventModels: EventModel[] = [];

	availableImpliedEventModels: EventModel[] = [];
	selectedImpliedEventModels: EventModel[] = [];

	constructor(
		private languageService: LanguageService,
		private dialogRef: MatDialogRef<EventModelRelationshipsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: EventModelRelationshipsDialogData
	) {}

	ngOnInit(): void {
		this.initializeBlockedEventModels();
		this.initializeImpliedEventModels();
	}

	private initializeBlockedEventModels(): void {
		const selectedIds = this.data.eventModel.blockedEventModelIds || [];

		this.selectedBlockedEventModels = this.data.availableEventModels.filter(em =>
			selectedIds.includes(em.eventModelId) && em.eventModelId !== this.data.eventModel.eventModelId
		);

		this.availableBlockedEventModels = this.data.availableEventModels.filter(em =>
			!selectedIds.includes(em.eventModelId) && em.eventModelId !== this.data.eventModel.eventModelId
		);
	}

	private initializeImpliedEventModels(): void {
		const selectedIds = this.data.eventModel.impliedEventModelIds || [];

		this.selectedImpliedEventModels = this.data.availableEventModels.filter(em =>
			selectedIds.includes(em.eventModelId) && em.eventModelId !== this.data.eventModel.eventModelId
		);

		this.availableImpliedEventModels = this.data.availableEventModels.filter(em =>
			!selectedIds.includes(em.eventModelId) && em.eventModelId !== this.data.eventModel.eventModelId
		);
	}

	addBlockedEventModel(eventModel: EventModel): void {
		this.availableBlockedEventModels = this.availableBlockedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.selectedBlockedEventModels = [...this.selectedBlockedEventModels, eventModel];
	}

	removeBlockedEventModel(eventModel: EventModel): void {
		this.selectedBlockedEventModels = this.selectedBlockedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.availableBlockedEventModels = [...this.availableBlockedEventModels, eventModel];
	}

	addImpliedEventModel(eventModel: EventModel): void {
		this.availableImpliedEventModels = this.availableImpliedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.selectedImpliedEventModels = [...this.selectedImpliedEventModels, eventModel];
	}

	removeImpliedEventModel(eventModel: EventModel): void {
		this.selectedImpliedEventModels = this.selectedImpliedEventModels.filter(em => em.eventModelId !== eventModel.eventModelId);
		this.availableImpliedEventModels = [...this.availableImpliedEventModels, eventModel];
	}

	getEventModelName(eventModel: EventModel): string {
		const name = this.languageService.getDefaultTranslation(eventModel.shortname) || eventModel.id;
		return `${name} (${eventModel.id})`;
	}

	onSave(): void {
		const result: any = {};

		const originalBlockedIds = [...(this.data.eventModel.blockedEventModelIds || [])].sort();
		const newBlockedIds = [...this.selectedBlockedEventModels.map(em => em.eventModelId)].sort();

		if(JSON.stringify(originalBlockedIds) !== JSON.stringify(newBlockedIds)) {
			result.blockedEventModelIds = this.selectedBlockedEventModels.map(em => em.eventModelId);
		}

		const originalImpliedIds = [...(this.data.eventModel.impliedEventModelIds || [])].sort();
		const newImpliedIds = [...this.selectedImpliedEventModels.map(em => em.eventModelId)].sort();

		if(JSON.stringify(originalImpliedIds) !== JSON.stringify(newImpliedIds)) {
			result.impliedEventModelIds = this.selectedImpliedEventModels.map(em => em.eventModelId);
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
