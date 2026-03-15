import {Component, Input, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EventModel} from '@core/model/event-model';
import {EventGroup} from '@core/model/event-group';
import {ScopeModel} from '@core/model/scope-model';
import {EventModelDialogService} from '../../../services/dialogs/event-model-dialog.service';
import {EventModelManagerService} from '../../../services/manager/event-model-manager.service';
import {EventGroupManagerService} from '../../../services/manager/event-group-manager.service';
import {DatasetModelManagerService} from '../../../services/manager/dataset-model-manager.service';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';
import {BaseDraftDetailComponent} from '../../../shared/base-draft-detail.component';
import {SettingItemComponent} from '../../../shared/setting-item/setting-item.component';
import {FormModelManagerService} from '../../../services/manager/form-model-manager.service';

@Component({
	selector: 'app-event-model-detail',
	standalone: true,
	templateUrl: './event-model-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class EventModelDetailComponent extends BaseDraftDetailComponent<EventModel> {
	@Input() eventModelId = '';
	@Input() eventModels: EventModel[] = [];
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventModels: EventModel[] = [];
	@Input() scopeModel: ScopeModel | null = null;

	@Output() eventModelUpdated = this.entityUpdated;
	@Output() eventModelDeleted = this.entityDeleted;

	constructor(
		languageService: LanguageService,
		private eventModelDialogService: EventModelDialogService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private workflowManager: WorkflowManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	protected entityIdInputName(): string {return 'eventModelId';}
	protected entitiesInputName(): string {return 'eventModels';}
	protected getEntityId(): string {return this.eventModelId;}
	protected getEntities(): EventModel[] {return this.eventModels;}
	protected getOriginals(): EventModel[] {return this.originalEventModels;}
	protected findInArray(arr: EventModel[], id: string): EventModel | undefined {
		return arr.find(em => em.eventModelId === id);
	}

	override ngOnChanges(changes: SimpleChanges): void {
		if(changes['eventModelId'] && this.eventModelId) {
			this.eventModelManager.loadFull(this.projectId).subscribe({
				next: () => this.syncDraft(),
				error: error => console.error('Error loading full event models:', error)
			});
		}
		else if(changes['eventModels']) {
			this.syncDraft();
		}
	}

	get draftEventModel(): EventModel | null {return this.draftEntity;}

	onEditBasicInfo(): void {
		if(!this.draftEntity || !this.scopeModel) {
			return;
		}
		this.eventModelDialogService.openBasicInfoDialog(
			this.draftEntity, this.projectId,
			this.scopeModel.scopeModelId, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditScheduling(): void {
		if(!this.draftEntity) {
			return;
		}
		this.eventModelDialogService.openSchedulingDialog(
			this.draftEntity, this.eventModels
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditLabelPattern(): void {
		if(!this.draftEntity) {
			return;
		}
		this.eventModelDialogService.openLabelPatternDialog(this.draftEntity).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditResources(): void {
		if(!this.draftEntity) {
			return;
		}
		this.eventModelDialogService.openResourcesDialog(
			this.projectId, this.draftEntity
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditRelationships(): void {
		if(!this.draftEntity) {
			return;
		}
		this.eventModelDialogService.openRelationshipsDialog(
			this.draftEntity, this.eventModels
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onDelete(): void {
		if(!this.draftEntity) {
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Event Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEntity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.eventModelId);
			}
		});
	}

	getEventModelLabel(eventModelId: string): string {
		return this.languageService.getLabelById(eventModelId, id => this.eventModelManager.getById(id));
	}

	getEventGroupLabel(eventGroupId: string): string {
		return this.languageService.getLabelById(eventGroupId, id => this.eventGroupManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	getFormModelLabel(formModelId: string): string {
		return this.languageService.getLabelById(formModelId, id => this.formModelManager.getById(id));
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
