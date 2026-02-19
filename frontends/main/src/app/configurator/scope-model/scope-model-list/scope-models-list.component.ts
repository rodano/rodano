import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
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
import {forkJoin, Subscription} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ProjectLanguage} from '@core/model/project-language';
import {HttpErrorResponse} from '@angular/common/http';

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
export class ScopeModelsListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() scopeModelsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() scopeModelContextChanged = new EventEmitter<{
		scopeModels: any[];
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}>();

	selectedScopeModel: ScopeModel | null = null;
	selectedEventModelId: string | null = null;
	selectedEventGroupId: string | null = null;
	viewMode: ViewMode = 'scope-list';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	private currentEventModels: EventModel[] = [];
	private currentEventGroups: EventGroup[] = [];

	constructor(
		public scopeModelManager: ScopeModelManagerService,
		public eventModelManager: EventModelManagerService,
		public eventGroupManager: EventGroupManagerService,
		private scopeModelDialogService: ScopeModelDialogService,
		private eventModelDialogService: EventModelDialogService,
		private eventGroupDialogService: EventGroupDialogService,
		private languageService: LanguageService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadScopeModels();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode'] && this.scopeModels.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('scope-model-')) {
				const scopeModelId = nodeId?.replace('scope-model-', '');
				const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
				if(scopeModel) {
					this.selectedScopeModel = scopeModel;
					this.updateFilteredEventModels();
					this.updateFilteredEventGroups();
				}
			}
			else if(nodeId === 'scope-models') {
				this.selectedScopeModel = null;
				this.currentEventModels = [];
				this.currentEventGroups = [];
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedScopeModel) {
			return 0;
		}

		if(this.viewMode === 'scope-detail' || this.viewMode === 'event-list' || this.viewMode === 'event-group-list') {
			return 2;
		}

		if(this.viewMode === 'event-detail' || this.viewMode === 'event-group-detail') {
			return 3;
		}

		return 0;
	}

	get scopeModels(): ScopeModel[] {
		return this.scopeModelManager.getAll();
	}

	get eventModels(): EventModel[] {
		return this.currentEventModels;
	}

	get eventGroups(): EventGroup[] {
		return this.currentEventGroups;
	}

	get modifiedScopeModelIds(): Set<string> {
		return this.scopeModelManager.getModifiedIds();
	}

	get originalScopeModels(): ScopeModel[] {
		return this.scopeModelManager.getOriginals();
	}

	get modifiedEventModels(): Set<string> {
		return this.eventModelManager.getModifiedIds();
	}

	get modifiedEventGroups(): Set<string> {
		return this.eventGroupManager.getModifiedIds();
	}

	get originalEventModels(): EventModel[] {
		return this.eventModelManager.getOriginals();
	}

	get originalEventGroups(): EventGroup[] {
		return this.eventGroupManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.scopeModelManager.getModificationCount() + this.eventModelManager.getModificationCount() + this.eventGroupManager.getModificationCount();
	}

	loadScopeModels(): void {
		this.loading = true;

		forkJoin({
			scopeModels: this.scopeModelManager.load(this.projectId),
			eventModels: this.eventModelManager.load(this.projectId),
			eventGroups: this.eventGroupManager.load(this.projectId)
		}).subscribe({
			next: ({scopeModels}) => {
				if(this.selectedScopeModel) {
					this.selectedScopeModel = scopeModels.find(
						sm => sm.scopeModelId === this.selectedScopeModel!.scopeModelId
					) || null;
					this.updateFilteredEventModels();
					this.updateFilteredEventGroups();
				}
				this.loading = false;
				this.emitContext();
			},
			error: error => {
				console.error('Error loading scope models:', error);
				this.snackBar.open('Failed to load scope models', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private updateFilteredEventModels(): void {
		if(!this.selectedScopeModel) {
			this.currentEventModels = [];
			return;
		}
		this.currentEventModels = this.eventModelManager.getAllForScope(this.selectedScopeModel.scopeModelId);
	}

	private updateFilteredEventGroups(): void {
		if(!this.selectedScopeModel) {
			this.currentEventGroups = [];
			return;
		}
		this.currentEventGroups = this.eventGroupManager.getAllForScope(this.selectedScopeModel.scopeModelId);
	}

	onSelectScopeModel(scopeModel: ScopeModel): void {
		if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
			this.clearSelection();
		}
		else {
			this.selectScopeModel(scopeModel);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedScopeModel = null;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.currentEventModels = [];
		this.currentEventGroups = [];
		this.viewMode = 'scope-detail';
		this.nodeSelected.emit('scope-models');
	}

	backToScopeDetail(): void {
		this.viewMode = 'scope-detail';
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.emitContext();
	}

	private selectScopeModel(scopeModel: ScopeModel): void {
		const previousScopeModelId = this.selectedScopeModel?.scopeModelId;

		this.selectedScopeModel = scopeModel;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;

		if(previousScopeModelId !== scopeModel.scopeModelId) {
			this.viewMode = 'scope-detail';
		}

		this.updateFilteredEventModels();
		this.updateFilteredEventGroups();
		this.emitContext();
		this.nodeSelected.emit(`scope-model-${scopeModel.scopeModelId}`);
	}

	isSelected(scopeModel: ScopeModel): boolean {
		return this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId;
	}

	onCreateScopeModel(): void {
		this.scopeModelDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: ScopeModel | null) => {
			if(result) {
				this.scopeModelManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Scope model created', 'Close', {duration: 2000});
						this.loadScopeModels();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating scope model', error);
						this.snackBar.open('Failed to create scope model', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onScopeModelUpdated(updatedScopeModel: ScopeModel): void {
		this.selectedScopeModel = this.scopeModelManager.getById(updatedScopeModel.scopeModelId) || null;
		this.emitModificationChange();
	}

	onScopeModelDeleted(scopeModelId: string): void {
		const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
		if(!scopeModel) {
			return;
		}
		this.performDelete(scopeModel);
	}

	private performDelete(scopeModel: ScopeModel): void {
		this.scopeModelManager.delete(this.projectId, scopeModel.scopeModelId).subscribe({
			next: () => {
				this.snackBar.open('Scope model deleted', 'Close', {duration: 2000});

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.clearSelection();
				}

				this.loadScopeModels();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting scope model:', error);
				this.snackBar.open('Failed to delete scope model', 'Close', {duration: 3000});
			}
		});
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
			this.projectLanguages,
			formattedEventGroups
		).subscribe(result => {
			if(result && this.selectedScopeModel) {
				const newEventModel: EventModel = {
					eventModelId: '',
					scopeModelId: this.selectedScopeModel.scopeModelId,
					...result,
					datasetModelIds: [],
					formModelIds: [],
					workflowIds: [],
					deadlineReferenceEventModelIds: [],
					blockedEventModelIds: [],
					impliedEventModelIds: []
				};

				this.eventModelManager.create(this.projectId, newEventModel).subscribe({
					next: () => {
						this.snackBar.open('Event model created', 'Close', {duration: 2000});
						this.loadScopeModels();
					},
					error: error => {
						console.error('Error creating event model:', error);
						this.snackBar.open('Failed to create event', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onEventModelUpdated(updatedEventModel: EventModel): void {
		if(!updatedEventModel) {
			return;
		}
		this.eventModelManager.update(updatedEventModel);
		this.updateFilteredEventModels();
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onEventModelDeleted(eventModelId: string): void {
		this.eventModelManager.delete(this.projectId, eventModelId).subscribe({
			next: () => {
				this.snackBar.open('Event model deleted', 'Close', {duration: 2000});

				if(this.selectedEventModelId === eventModelId) {
					this.selectedEventModelId = null;
					this.viewMode = 'event-list';
				}

				this.updateFilteredEventModels();
				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting event model:', error);
				this.snackBar.open('Failed to delete event', 'Close', {duration: 3000});
			}
		});
	}

	switchToEventModelView(): void {
		if(!this.selectedScopeModel) {
			return;
		}
		this.viewMode = 'event-list';
		this.emitContext();
	}

	onSelectEventModel(eventModelId: string): void {
		if(this.selectedEventModelId === eventModelId) {
			this.selectedEventModelId = null;
			this.viewMode = 'event-list';
		}
		else {
			this.selectedEventModelId = eventModelId;
			this.viewMode = 'event-detail';
			this.nodeSelected.emit(`event-model-${eventModelId}`);
		}
		this.emitContext();
	}

	onCreateEventGroup(): void {
		if(!this.selectedScopeModel) {
			return;
		}

		this.eventGroupDialogService.openCreateDialog(
			this.projectId,
			this.selectedScopeModel.scopeModelId,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				const newEventGroup: EventGroup = {
					eventGroupId: '',
					scopeModelId: this.selectedScopeModel!.scopeModelId,
					...result
				};

				this.eventGroupManager.create(this.projectId, newEventGroup).subscribe({
					next: () => {
						this.snackBar.open('Event group created', 'Close', {duration: 2000});
						this.loadScopeModels();
					},
					error: error => {
						console.error('Error creating event group:', error);
						this.snackBar.open('Failed to create event group', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onEventGroupUpdated(updatedEventGroup: EventGroup): void {
		if(!updatedEventGroup) {
			return;
		}

		this.eventGroupManager.update(updatedEventGroup);
		this.updateFilteredEventGroups();
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onEventGroupDeleted(eventGroupId: string): void {
		this.eventGroupManager.delete(this.projectId, eventGroupId).subscribe({
			next: () => {
				this.snackBar.open('Event model deleted', 'Close', {duration: 2000});

				if(this.selectedEventGroupId === eventGroupId) {
					this.selectedEventGroupId = null;
					this.viewMode = 'event-group-list';
				}

				this.updateFilteredEventGroups();
				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting event group:', error);
				this.snackBar.open('Failed to delete event group', 'Close', {duration: 3000});
			}
		});
	}

	switchToEventGroupView(): void {
		if(!this.selectedScopeModel) {
			return;
		}
		this.viewMode = 'event-group-list';
		this.emitContext();
	}

	onSelectEventGroup(eventGroupId: string): void {
		if(this.selectedEventGroupId === eventGroupId) {
			this.selectedEventGroupId = null;
			this.viewMode = 'event-group-list';
		}
		else {
			this.selectedEventGroupId = eventGroupId;
			this.viewMode = 'event-group-detail';
			this.nodeSelected.emit(`event-group-${eventGroupId}`);
		}
		this.emitContext();
	}

	private emitModificationChange(): void {
		this.scopeModelsChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.scopeModelContextChanged.emit({
			scopeModels: [...this.scopeModels],
			eventModels: this.currentEventModels,
			eventGroups: this.currentEventGroups,
			selectedScopeModelId: this.selectedScopeModel?.scopeModelId || null,
			selectedEventModelId: this.selectedEventModelId,
			selectedEventGroupId: this.selectedEventGroupId
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}

	isEventModelModified(eventModelId: string): boolean {
		return this.eventModelManager.isModified(eventModelId);
	}

	isEventGroupModified(eventGroupId: string): boolean {
		return this.eventGroupManager.isModified(eventGroupId);
	}
}
