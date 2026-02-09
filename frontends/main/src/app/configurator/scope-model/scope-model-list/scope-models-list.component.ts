import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModel} from '@core/model/scope-model';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {
	ScopeModelBasicInfoDialogComponent
} from '../scope-model-dialog/scope-model-basic-info-dialog/scope-model-basic-info-dialog.component';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';
import {ConfiguratorService} from '../../services/configurator.service';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltipModule} from '@angular/material/tooltip';
import {
	ScopeModelRelationshipsDialogComponent
} from '../scope-model-dialog/scope-model-relationships-dialog/scope-model-relationships-dialog.component';
import {
	ScopeModelDefaultSettingsDialogComponent
} from '../scope-model-dialog/scope-model-default-settings-dialog/scope-model-default-settings-dialog.component';
import {
	ScopeModelResourcesDialogComponent,
	WorkflowStateSelection
} from '../scope-model-dialog/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {EventModel} from '@core/model/event-model';
import {
	EventModelCreateDialogComponent
} from '../scope-model-dialog/event-model-create-dialog/event-model-create-dialog.component';
import {EventModelTimelineComponent} from '../event-model/event-model-timeline/event-model-timeline.component';
import {EventModelDetailComponent} from '../event-model/event-model-detail/event-model-detail.component';
import {
	ScopeModelPatternDialogComponent
} from '../scope-model-dialog/scope-model-pattern-dialog/scope-model-pattern-dialog.component';
import {EventGroup} from '@core/model/event-group';
import {EventGroupDialogComponent} from '../scope-model-dialog/event-group-dialog/event-group-dialog.component';
import {EventGroupDetailComponent} from '../event-group/event-group-detail/event-group-detail.component';
import {ScopeModelManagerService} from '../../services/scope-model-manager.service';
import {EventModelManagerService} from '../../services/event-model-manager.service';
import {EventGroupManagerService} from '../../services/event-group-manager.service';

export type ViewMode = 'scope-detail' | 'event-list' | 'event-detail' | 'event-group-list' | 'event-group-detail';

interface WorkflowStateGroup {
	workflowId: string;
	workflowName: string;
	states: {id: string; name: string}[];
}

@Component({
	selector: 'app-scope-models-list',
	standalone: true,
	templateUrl: './scope-models-list.component.html',
	styleUrls: ['./scope-models-list.component.css', './scope-models-list-event-models.component.css'],
	imports: [
		CommonModule,
		MatTableModule,
		MatButtonModule,
		MatIconModule,
		MatTooltipModule,
		EventModelTimelineComponent,
		EventModelDetailComponent,
		EventGroupDetailComponent
	]
})
export class ScopeModelsListComponent implements OnInit, OnChanges, OnDestroy {
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

	selectedScopeModel: ScopeModel | null = null;
	selectedEventModelId: string | null = null;
	selectedEventGroupId: string | null = null;
	viewMode: ViewMode = 'scope-detail';
	loading = true;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	private workflowStateSelectionsMap = new Map<string, WorkflowStateSelection[]>();

	constructor(
		public scopeModelManager: ScopeModelManagerService,
		public eventModelManager: EventModelManagerService,
		public eventGroupManager: EventGroupManagerService,
		private configuratorService: ConfiguratorService,
		private languageService: LanguageService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadProject();
		this.loadScopeModels();

		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.scopeModels.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('scope-model-')) {
				const scopeModelId = nodeId.replace('scope-model-', '');
				const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
				if(scopeModel) {
					this.selectedScopeModel = scopeModel;
				}
			}
			else if(nodeId === 'scope-models') {
				this.selectedScopeModel = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get scopeModels(): ScopeModel[] {
		return this.scopeModelManager.getAll();
	}

	get eventModels(): EventModel[] {
		return this.eventModelManager.getAll();
	}

	get eventGroups(): EventGroup[] {
		return this.eventGroupManager.getAll();
	}

	get modifiedScopeModelIds(): Set<string> {
		return this.scopeModelManager.getModifiedIds();
	}

	get modifiedFieldsByScopeModel(): Map<string, Set<string>> {
		return this.scopeModelManager.getModifiedFieldsMap();
	}

	get originalScopeModels(): ScopeModel[] {
		return this.scopeModelManager.getOriginals();
	}

	get modifiedEventModelIds(): Set<string> {
		return this.eventModelManager.getModifiedIds();
	}

	get modifiedFieldsByEventModel(): Map<string, Set<string>> {
		return this.eventModelManager.getModifiedFieldsMap();
	}

	get originalEventModels(): EventModel[] {
		return this.eventModelManager.getOriginals();
	}

	get modifiedEventGroupIds(): Set<string> {
		return this.eventGroupManager.getModifiedIds();
	}

	get originalEventGroups(): EventGroup[] {
		return this.eventGroupManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.scopeModelManager.getModificationCount() + this.eventModelManager.getModificationCount() + this.eventGroupManager.getModificationCount();
	}

	loadProject(): void {
		this.configuratorService.getProject(this.projectId).subscribe({
			next: (project: ConfiguratorProject) => {
				this.projectLanguages = project.languages || [];
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading project:', error);
				this.projectLanguages = [{languageCode: 'en', isDefault: true}];
			}
		});
	}

	loadScopeModels(): void {
		this.loading = true;
		this.scopeModelManager.load(this.projectId).subscribe({
			next: (scopeModels: ScopeModel[]) => {
				if(this.selectedScopeModel) {
					this.selectedScopeModel = scopeModels.find(
						sm => sm.scopeModelId === this.selectedScopeModel!.scopeModelId
					) || null;
				}
				this.loading = false;
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading scope models:', error);
				this.snackBar.open('Failed to load scope models', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
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

	private clearSelection(): void {
		this.selectedScopeModel = null;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.viewMode = 'scope-detail';
		this.nodeSelected.emit('scope-models');
	}

	private selectScopeModel(scopeModel: ScopeModel): void {
		const previousScopeModelId = this.selectedScopeModel?.scopeModelId;

		this.selectedScopeModel = scopeModel;
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;

		if(previousScopeModelId !== scopeModel.scopeModelId) {
			this.viewMode = 'scope-detail';
			this.eventModelManager.setAll([]);
			this.eventGroupManager.setAll([]);
		}

		this.emitContext();

		this.eventModelManager.loadForScope(this.projectId, scopeModel.scopeModelId).subscribe({
			next: () => this.emitContext(),
			error: error => {
				console.error('Error loading event models:', error);
				this.eventModelManager.setAll([]);
				this.emitContext();
			}
		});

		this.eventGroupManager.loadForScope(this.projectId, scopeModel.scopeModelId).subscribe({
			next: () => this.emitContext(),
			error: error => {
				console.error('Error loading event groups:', error);
				this.eventGroupManager.setAll([]);
				this.emitContext();
			}
		});

		this.nodeSelected.emit(`scope-model-${scopeModel.scopeModelId}`);
	}

	isSelected(scopeModel: ScopeModel): boolean {
		return this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId;
	}

	onCreateScopeModel(): void {
		const dialogRef = this.dialog.open(ScopeModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: null,
				languages: this.projectLanguages
			}
		});

		dialogRef.afterClosed().subscribe((result: ScopeModel | null) => {
			if(result) {
				this.scopeModelManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Scope model created', 'Close', {duration: 2000});
						this.loadScopeModels();
						this.emitModificationChange();
					},
					error: (error: HttpErrorResponse) => {
						console.error('Error creating scope model:', error);
						this.snackBar.open('Failed to create scope model', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onEditBasicInfo(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel)),
				languages: this.projectLanguages
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.refreshSelectedScopeModel();
				this.showStagedMessage();
			}
		});
	}

	onEditRelationships(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelRelationshipsDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.refreshSelectedScopeModel();
				this.showStagedMessage();
			}
		});
	}

	onEditDefaultSettings(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelDefaultSettingsDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.refreshSelectedScopeModel();
				this.showStagedMessage();
			}
		});
	}

	onEditPattern(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelPatternDialogComponent, {
			width: '500px',
			data: {
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.refreshSelectedScopeModel();
				this.showStagedMessage();
			}
		});
	}

	onEditResources(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelResourcesDialogComponent, {
			data: {
				scopeModel: this.scopeModelManager.getById(scopeModel.scopeModelId),
				availableForms: [],
				availableDatasets: [],
				availableWorkflows: [],
				workflowStateSelections: this.workflowStateSelectionsMap.get(scopeModel.scopeModelId) || []
			},
			width: '500px',
			maxHeight: '90vh',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result) {
				const draft = this.scopeModelManager.getById(scopeModel.scopeModelId);
				if(draft) {
					Object.assign(draft, result);

					if(result.workflowStateSelections !== undefined) {
						this.workflowStateSelectionsMap.set(scopeModel.scopeModelId, result.workflowStateSelections);
					}

					this.scopeModelManager.update(draft);
					this.refreshSelectedScopeModel();
					this.showStagedMessage();
				}
			}
		});
	}

	onDeleteScopeModel(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Scope Model',
				message: `Are you sure you want to delete "${this.getTranslatedName(scopeModel.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && scopeModel.scopeModelId) {
				const hasChildren = scopeModel.childScopeModelIds && scopeModel.childScopeModelIds.length > 0;

				if(hasChildren) {
					this.deleteScopeModelWithChildren(scopeModel);
				}
				else {
					this.performDelete(scopeModel);
				}
			}
		});
	}

	private deleteScopeModelWithChildren(scopeModel: ScopeModel): void {
		const isRoot = !scopeModel.parentIds || scopeModel.parentIds.length === 0;
		const updatePromises: Promise<any>[] = [];

		if(isRoot) {
			scopeModel.childScopeModelIds.forEach(childId => {
				const child = this.scopeModels.find(sm => sm.scopeModelId === childId);
				if(!child) {
					return;
				}

				const updatedChild: ScopeModel = {
					...child,
					parentIds: [],
					defaultParentId: '',
					root: true,
					leaf: child.childScopeModelIds.length === 0
				};

				updatePromises.push(
					this.scopeModelManager.updateOnServer(this.projectId, child.scopeModelId, updatedChild).toPromise()
				);
			});
		}
		else {
			scopeModel.childScopeModelIds.forEach(childId => {
				const child = this.scopeModels.find(sm => sm.scopeModelId === childId);
				if(!child) {
					return;
				}

				const updatedChild: ScopeModel = {
					...child,
					parentIds: scopeModel.parentIds,
					defaultParentId: scopeModel.defaultParentId || scopeModel.parentIds[0],
					root: false,
					leaf: child.childScopeModelIds.length === 0
				};

				updatePromises.push(
					this.scopeModelManager.updateOnServer(this.projectId, child.scopeModelId, updatedChild).toPromise()
				);
			});
		}

		Promise.all(updatePromises)
			.then(() => {
				this.performDelete(scopeModel);
			})
			.catch(error => {
				console.error('Error updating children:', error);
				this.snackBar.open('Failed to update children', 'Close', {duration: 3000});
			});
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

		const dialogRef = this.dialog.open(EventModelCreateDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModelId: this.selectedScopeModel.scopeModelId,
				eventModel: null,
				languages: this.projectLanguages
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const newEventModel: EventModel = {
					eventModelId: '',
					scopeModelId: this.selectedScopeModel!.scopeModelId,
					eventGroupId: '',
					number: this.eventModels.length + 1,
					...result,
					formModelIds: [],
					datasetModelIds: [],
					workflowIds: [],
					deadlineReferenceEventModelIds: [],
					blockedEventModelIds: [],
					impliedEventModelIds: []
				};

				this.eventModelManager.create(this.projectId, newEventModel).subscribe({
					next: () => {
						this.snackBar.open('Event model created', 'Close', {duration: 2000});
					},
					error: error => {
						console.error('Error creating event model:', error);
						this.snackBar.open('Failed to create event model', 'Close', {duration: 3000});
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
		this.showStagedMessage();
	}

	onEventModelDeleted(eventModelId: string): void {
		this.eventModelManager.delete(this.projectId, eventModelId).subscribe({
			next: () => {
				this.snackBar.open('Event model deleted', 'Close', {duration: 2000});

				if(this.selectedEventModelId === eventModelId) {
					this.selectedEventModelId = null;
					this.viewMode = 'event-list';
				}

				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting event model:', error);
				this.snackBar.open('Failed to delete event model', 'Close', {duration: 3000});
			}
		});
	}

	onCreateEventGroup(): void {
		if(!this.selectedScopeModel) {
			return;
		}

		const dialogRef = this.dialog.open(EventGroupDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModelId: this.selectedScopeModel.scopeModelId,
				eventGroup: null,
				languages: this.projectLanguages
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const newEventGroup: EventGroup = {
					eventGroupId: '',
					scopeModelId: this.selectedScopeModel!.scopeModelId,
					...result
				};

				this.eventGroupManager.create(this.projectId, newEventGroup).subscribe({
					next: () => {
						this.snackBar.open('Event group created', 'Close', {duration: 2000});
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
		this.showStagedMessage();
	}

	onEventGroupDeleted(eventGroupId: string): void {
		this.eventGroupManager.delete(this.projectId, eventGroupId).subscribe({
			next: () => {
				this.snackBar.open('Event group deleted', 'Close', {duration: 2000});

				if(this.selectedEventGroupId === eventGroupId) {
					this.selectedEventGroupId = null;
					this.viewMode = 'event-group-list';
				}

				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting event group:', error);
				this.snackBar.open('Failed to delete event group', 'Close', {duration: 3000});
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

	backToScopeDetail(): void {
		this.viewMode = 'scope-detail';
		this.selectedEventModelId = null;
		this.selectedEventGroupId = null;
		this.emitContext();
	}

	private refreshSelectedScopeModel(): void {
		if(this.selectedScopeModel) {
			this.selectedScopeModel = this.scopeModelManager.getById(
				this.selectedScopeModel.scopeModelId
			) || null;
		}
	}

	private showStagedMessage(): void {
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	private emitModificationChange(): void {
		this.scopeModelsChanged.emit({modificationCount: this.totalModificationCount});
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
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.selectedLanguage;
		return translations[lang] || '';
	}

	getScopeModelLabel(scopeModelId: string): string {
		const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
		if(!scopeModel) {
			return scopeModelId;
		}

		const name = this.languageService.getDefaultTranslation(scopeModel.shortname) || scopeModel.id;
		return `${name} (${scopeModel.id})`;
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	getWorkflowStateSelections(scopeModel: ScopeModel): WorkflowStateGroup[] {
		const selections = this.workflowStateSelectionsMap.get(scopeModel.scopeModelId) || [];

		const grouped = new Map<string, {id: string; name: string}[]>();

		selections.forEach(selection => {
			if(!grouped.has(selection.workflowId)) {
				grouped.set(selection.workflowId, []);
			}

			grouped.get(selection.workflowId)!.push({
				id: selection.workflowStateId,
				name: selection.workflowStateId
			});
		});

		const result: WorkflowStateGroup[] = [];
		grouped.forEach((states, workflowId) => {
			result.push({
				workflowId,
				workflowName: workflowId,
				states
			});
		});

		return result;
	}

	isFieldModifiedForScopeModel(scopeModelId: string, fieldName: string): boolean {
		return this.scopeModelManager.isFieldModified(scopeModelId, fieldName);
	}

	isEventModelModified(eventModelId: string): boolean {
		return this.eventModelManager.isModified(eventModelId);
	}

	isEventGroupModified(eventGroupId: string): boolean {
		return this.eventGroupManager.isModified(eventGroupId);
	}
}
