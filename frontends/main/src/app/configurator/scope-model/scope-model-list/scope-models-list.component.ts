import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {EventModel} from '@core/model/event-model';
import {EventModelTimelineComponent} from '../event-model/event-model-timeline/event-model-timeline.component';
import {EventModelDetailComponent} from '../event-model/event-model-detail/event-model-detail.component';
import {EventGroup} from '@core/model/event-group';
import {EventGroupDetailComponent} from '../event-group/event-group-detail/event-group-detail.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {ScopeModelDetailComponent} from '../scope-model-detail/scope-model-detail.component';
import {EventModelDialogService} from '../../services/dialogs/event-model-dialog.service';
import {EventGroupDialogService} from '../../services/dialogs/event-group-dialog.service';
import {ScopeModelDialogService} from '../../services/dialogs/scope-model-dialog.service';
import {forkJoin} from 'rxjs';

type ViewMode = 'scope-list' | 'scope-detail' | 'event-list' | 'event-detail' | 'event-group-list' | 'event-group-detail';

@Component({
	selector: 'app-scope-models-list',
	standalone: true,
	templateUrl: './scope-models-list.component.html',
	styleUrls: ['./scope-models-list.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		ScopeModelDetailComponent,
		EventModelDetailComponent,
		EventModelTimelineComponent,
		EventGroupDetailComponent,
		MatTooltipModule
	]
})
export class ScopeModelsListComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() scopeModelsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() scopeModelContextChanged = new EventEmitter<{
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}>();

	loading = true;
	scopeModels: ScopeModel[] = [];
	originalScopeModels: ScopeModel[] = [];
	eventModels: EventModel[] = [];
	originalEventModels: EventModel[] = [];
	eventGroups: EventGroup[] = [];
	originalEventGroups: EventGroup[] = [];

	selectedScopeModel: ScopeModel | null = null;
	selectedEventModelId: string | null = null;
	selectedEventGroupId: string | null = null;

	modifiedScopeModelIds = new Set<string>();
	modifiedEventModelIds = new Set<string>();
	modifiedEventGroupIds = new Set<string>();

	viewMode: ViewMode = 'scope-list';
	viewLevel = 1;

	constructor(
		public scopeModelManager: ScopeModelManagerService,
		public eventModelManager: EventModelManagerService,
		public eventGroupManager: EventGroupManagerService,
		private scopeModelDialogService: ScopeModelDialogService,
		private eventModelDialogService: EventModelDialogService,
		private eventGroupDialogService: EventGroupDialogService,
		private languageService: LanguageService
	) {}

	ngOnInit(): void {
		this.loadScopeModels();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode'] && this.selectedNode) {
			this.handleNodeSelection(this.selectedNode);
		}
	}

	loadScopeModels(): void {
		this.loading = true;

		forkJoin({
			scopeModels: this.scopeModelManager.load(this.projectId),
			eventModels: this.eventModelManager.load(this.projectId),
			eventGroups: this.eventGroupManager.load(this.projectId)
		}).subscribe({
			next: ({scopeModels, eventModels, eventGroups}) => {
				this.scopeModels = scopeModels;
				this.originalScopeModels = this.scopeModelManager.getOriginals();

				this.originalEventModels = this.eventModelManager.getOriginals();
				this.originalEventGroups = this.eventGroupManager.getOriginals();

				this.modifiedScopeModelIds = this.scopeModelManager.getModifiedIds();
				this.modifiedEventModelIds = this.eventModelManager.getModifiedIds();
				this.modifiedEventGroupIds = this.eventGroupManager.getModifiedIds();

				this.emitModificationCount();
				this.loading = false;
			},
			error: error => {
				console.error('Error loading scope models:', error);
				this.loading = false;
			}
		});
	}

	private handleNodeSelection(nodeId: string): void {
		if(nodeId === 'scope-models') {
			this.viewMode = 'scope-list';
			this.viewLevel = 1;
			this.clearSelection();
		}
		else if(nodeId.startsWith('scope-model-')) {
			const scopeModelId = nodeId.replace('scope-model-', '');
			const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
			if(scopeModel) {
				this.onSelectScopeModel(scopeModel);
			}
		}
		else if(nodeId.startsWith('event-model-')) {
			const eventModelId = nodeId.replace('event-model-', '');
			this.onSelectEventModel(eventModelId);
		}
		else if(nodeId.startsWith('event-group-')) {
			const eventGroupId = nodeId.replace('event-group-', '');
			this.onSelectEventGroup(eventGroupId);
		}
	}

	onSelectScopeModel(scopeModel: ScopeModel | null): void {
		if(!scopeModel) {
			this.viewMode = 'scope-list';
			this.viewLevel = 1;
			this.selectedScopeModel = null;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;
			this.eventModels = [];
			this.eventGroups = [];
			this.emitContext();
			return;
		}

		if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId && this.viewMode === 'scope-detail') {
			this.viewMode = 'scope-list';
			this.viewLevel = 1;
			this.selectedScopeModel = null;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;
			this.eventModels = [];
			this.eventGroups = [];
		}
		else {
			this.selectedScopeModel = scopeModel;
			this.viewMode = 'scope-detail';
			this.viewLevel = 2;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;

			this.eventModels = this.eventModelManager.getAllForScope(scopeModel.scopeModelId);
			this.eventGroups = this.eventGroupManager.getAllForScope(scopeModel.scopeModelId);
		}

		this.emitContext();
	}

	isSelected(scopeModel: ScopeModel): boolean {
		return this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId;
	}

	onCreateScopeModel(): void {
		this.scopeModelDialogService.openCreateDialog(
			this.projectId,
			this.project?.languages || []
		).subscribe(result => {
			if(result) {
				const newScopeModel: ScopeModel = {
					scopeModelId: `temp-${Date.now()}`,
					...result,
					parentIds: [],
					childScopeModelIds: [],
					datasetModelIds: [],
					formModelIds: [],
					workflowIds: []
				};

				this.scopeModels.push(newScopeModel);
				this.scopeModelManager.update(newScopeModel);
				this.modifiedScopeModelIds = this.scopeModelManager.getModifiedIds();
				this.emitModificationCount();
			}
		});
	}

	onScopeModelUpdated(updatedScopeModel: ScopeModel): void {
		const index = this.scopeModels.findIndex(sm => sm.scopeModelId === updatedScopeModel.scopeModelId);
		if(index !== -1) {
			this.scopeModels[index] = updatedScopeModel;
		}

		this.selectedScopeModel = updatedScopeModel;
		this.scopeModelManager.update(updatedScopeModel);
		this.modifiedScopeModelIds = this.scopeModelManager.getModifiedIds();
		this.emitModificationCount();
	}

	onScopeModelDeleted(scopeModelId: string): void {
		this.scopeModels = this.scopeModels.filter(sm => sm.scopeModelId !== scopeModelId);

		const wasOriginal = this.originalScopeModels.some(sm => sm.scopeModelId === scopeModelId);
		if(wasOriginal) {
			this.modifiedScopeModelIds.add(`${scopeModelId}-deleted`);
		}
		else {
			this.modifiedScopeModelIds.delete(scopeModelId);
		}

		this.selectedScopeModel = null;
		this.viewMode = 'scope-list';
		this.viewLevel = 1;
		this.emitModificationCount();
		this.emitContext();
	}

	switchToEventModelView(): void {
		this.viewMode = 'event-list';
		this.viewLevel = 2;
		this.emitContext();
	}

	switchToEventGroupView(): void {
		this.viewMode = 'event-group-list';
		this.viewLevel = 2;
		this.emitContext();
	}

	backToScopeDetail(): void {
		this.viewMode = 'scope-detail';
		this.viewLevel = 2;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.emitContext();
	}

	onSelectEventModel(eventModelId: string | null): void {
		if(!eventModelId) {
			this.viewMode = 'event-list';
			this.viewLevel = 2;
			this.selectedEventModelId = null;
		}
		else if(this.selectedEventModelId === eventModelId && this.viewMode === 'event-detail') {
			this.viewMode = 'event-list';
			this.viewLevel = 2;
			this.selectedEventModelId = null;
		}
		else {
			this.selectedEventModelId = eventModelId;
			this.viewMode = 'event-detail';
			this.viewLevel = 3;
		}

		this.emitContext();
	}

	onSelectEventGroup(eventGroupId: string | null): void {
		if(!eventGroupId) {
			this.viewMode = 'event-group-list';
			this.viewLevel = 2;
			this.selectedEventGroupId = null;
		}
		else if(this.selectedEventGroupId === eventGroupId && this.viewMode === 'event-group-detail') {
			this.viewMode = 'event-group-list';
			this.viewLevel = 2;
			this.selectedEventGroupId = null;
		}
		else {
			this.selectedEventGroupId = eventGroupId;
			this.viewMode = 'event-group-detail';
			this.viewLevel = 3;
		}

		this.emitContext();
	}

	onCreateEventModel(): void {
		if(!this.selectedScopeModel) {
			return;
		}

		const formattedEventGroups = this.eventGroups.map(eg => ({
			id: eg.eventGroupId,
			name: this.languageService.getDefaultTranslation(eg.shortname) || eg.id,
			code: eg.id
		}));

		this.eventModelDialogService.openCreateDialog(
			this.projectId,
			this.selectedScopeModel.scopeModelId,
			this.project?.languages || [],
			formattedEventGroups
		).subscribe(result => {
			if(result && this.selectedScopeModel) {
				const newEventModel: EventModel = {
					eventModelId: `temp-${Date.now()}`,
					scopeModelId: this.selectedScopeModel.scopeModelId,
					...result,
					datasetModelIds: [],
					formModelIds: [],
					workflowIds: [],
					deadlineReferenceEventModelIds: [],
					blockedEventModelIds: [],
					impliedEventModelIds: []
				};

				this.eventModels.push(newEventModel);
				this.eventModelManager.update(newEventModel);
				this.modifiedEventModelIds = this.eventModelManager.getModifiedIds();
				this.emitModificationCount();
				this.emitContext();
			}
		});
	}

	onEventModelUpdated(updatedEventModel: EventModel): void {
		this.eventModelManager.update(updatedEventModel);

		if(this.selectedScopeModel) {
			this.eventModels = this.eventModelManager.getAllForScope(this.selectedScopeModel.scopeModelId);
		}

		this.modifiedEventModelIds = this.eventModelManager.getModifiedIds();
		this.emitModificationCount();
		this.emitContext();
	}

	onEventModelDeleted(eventModelId: string): void {
		this.eventModels = this.eventModels.filter(em => em.eventModelId !== eventModelId);

		const wasOriginal = this.originalEventModels.some(em => em.eventModelId === eventModelId);
		if(wasOriginal) {
			this.modifiedEventModelIds.add(`${eventModelId}-deleted`);
		}
		else {
			this.modifiedEventModelIds.delete(eventModelId);
		}

		this.selectedEventModelId = null;
		this.viewMode = 'event-list';
		this.viewLevel = 2;
		this.emitModificationCount();
		this.emitContext();
	}

	onCreateEventGroup(): void {
		if(!this.selectedScopeModel) {
			return;
		}

		this.eventGroupDialogService.openCreateDialog(
			this.projectId,
			this.selectedScopeModel.scopeModelId,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.selectedScopeModel) {
				const newEventGroup: EventGroup = {
					eventGroupId: `temp-${Date.now()}`,
					scopeModelId: this.selectedScopeModel.scopeModelId,
					...result
				};

				this.eventGroups.push(newEventGroup);
				this.eventGroupManager.update(newEventGroup);
				this.modifiedEventGroupIds = this.eventGroupManager.getModifiedIds();
				this.emitModificationCount();
				this.emitContext();
			}
		});
	}

	onEventGroupUpdated(updatedEventGroup: EventGroup): void {
		this.eventGroupManager.update(updatedEventGroup);

		if(this.selectedScopeModel) {
			this.eventGroups = this.eventGroupManager.getAllForScope(this.selectedScopeModel.scopeModelId);
		}

		this.modifiedEventGroupIds = this.eventGroupManager.getModifiedIds();
		this.emitModificationCount();
		this.emitContext();
	}

	onEventGroupDeleted(eventGroupId: string): void {
		this.eventGroups = this.eventGroups.filter(eg => eg.eventGroupId !== eventGroupId);

		const wasOriginal = this.originalEventGroups.some(eg => eg.eventGroupId === eventGroupId);
		if(wasOriginal) {
			this.modifiedEventGroupIds.add(`${eventGroupId}-deleted`);
		}
		else {
			this.modifiedEventGroupIds.delete(eventGroupId);
		}

		this.selectedEventGroupId = null;
		this.viewMode = 'event-group-list';
		this.viewLevel = 2;
		this.emitModificationCount();
		this.emitContext();
	}

	isEventModelModified(eventModelId: string): boolean {
		return this.modifiedEventModelIds.has(eventModelId);
	}

	isEventGroupModified(eventGroupId: string): boolean {
		return this.modifiedEventGroupIds.has(eventGroupId);
	}

	clearSelection(): void {
		this.selectedScopeModel = null;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.eventModels = [];
		this.eventGroups = [];
		this.viewMode = 'scope-list';
		this.viewLevel = 1;
		this.emitContext();
	}

	private emitModificationCount(): void {
		const totalCount = this.modifiedScopeModelIds.size + this.modifiedEventModelIds.size + this.modifiedEventGroupIds.size;

		this.scopeModelsChanged.emit({modificationCount: totalCount});
	}

	private emitContext(): void {
		this.scopeModelContextChanged.emit({
			eventModels: this.eventModels,
			eventGroups: this.eventGroups,
			selectedScopeModelId: this.selectedScopeModel?.scopeModelId || null,
			selectedEventModelId: this.selectedEventModelId,
			selectedEventGroupId: this.selectedEventGroupId
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}
}
