import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {EventModel} from '@core/model/event-model';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {EventGroup} from '@core/model/event-group';
import {EventModelDialogService} from '../../../services/dialogs/event-model-dialog.service';
import {DatasetModelManagerService} from '../../../services/manager/dataset-model-manager.service';
import {EventModelManagerService} from '../../../services/manager/event-model-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowManagerService} from '../../../services/manager/workflow-manager.service';
import {EventGroupManagerService} from '../../../services/manager/event-group-manager.service';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-event-model-detail',
	standalone: true,
	templateUrl: './event-model-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DangerZoneComponent
	]
})
export class EventModelDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() eventModelId = '';
	@Input() eventModels: EventModel[] = [];
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventModels: EventModel[] = [];
	@Input() scopeModel: ScopeModel | null = null;
	@Input() project: ConfiguratorProject | null = null;
	@Output() closed = new EventEmitter<void>();
	@Output() eventModelUpdated = new EventEmitter<any>();
	@Output() eventModelDeleted = new EventEmitter<string>();

	originalEventModel: EventModel | null = null;
	draftEventModel: EventModel | null = null;
	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	constructor(
		public languageService: LanguageService,
		private eventModelDialogService: EventModelDialogService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private workflowManager: WorkflowManagerService,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['eventModelId'] && this.eventModelId) {
			this.eventModelManager.loadFull(this.projectId).subscribe({
				next: () => {
					this.loadEventModel();
				},
				error: error => console.error('Error loading full event models:', error)
			});
		}
		else if(changes['eventModels']) {
			this.loadEventModel();
		}
	}

	ngOnDestroy(): void {
		if(this.languageSubscription) {
			this.languageSubscription.unsubscribe();
		}
	}

	private loadEventModel(): void {
		const eventModel = this.eventModels.find(em => em.eventModelId === this.eventModelId);
		this.draftEventModel = eventModel ? JSON.parse(JSON.stringify(eventModel)) : null;

		const original = this.originalEventModels.find(em => em.eventModelId === this.eventModelId);
		this.originalEventModel = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	isFieldModified(field: keyof EventModel): boolean {
		if(!this.originalEventModel || !this.draftEventModel) {
			return false;
		}
		return JSON.stringify(this.originalEventModel[field]) !== JSON.stringify(this.draftEventModel[field]);
	}

	onClose(): void {
		this.closed.emit();
	}

	onEditBasicInfo(): void {
		if(!this.draftEventModel || !this.scopeModel) {
			return;
		}

		this.eventModelDialogService.openBasicInfoDialog(
			this.draftEventModel,
			this.projectId,
			this.scopeModel.scopeModelId,
			this.projectLanguages
		).subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditScheduling(): void {
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openSchedulingDialog(
			this.draftEventModel,
			this.eventModels
		).subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditLabelPattern(): void {
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openLabelPatternDialog(
			this.draftEventModel
		).subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditResources(): void {
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openResourcesDialog(
			this.projectId,
			this.draftEventModel
		).subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditRelationships(): void {
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openRelationshipsDialog(
			this.draftEventModel,
			this.eventModels
		).subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onDelete(): void {
		if(!this.draftEventModel) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Event Model',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEventModel.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEventModel) {
				this.eventModelDeleted.emit(this.draftEventModel.eventModelId);
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
		//TODO: Implement when form models are ready
		return formModelId;
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}
}
