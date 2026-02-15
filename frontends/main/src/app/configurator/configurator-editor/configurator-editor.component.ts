import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorService} from '../services/api/configurator.service';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectConfigVersion} from '@core/model/project-config-version';
import {ConfiguratorDetailComponent} from '../configurator-detail/configurator-detail.component';
import {ConfiguratorTreeComponent} from '../tree/configurator-tree/configurator-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';
import {SnapshotsListDialogComponent} from '../dialogs/base/snapshots-list-dialog/snapshots-list-dialog.component';
import {CreateSnapshotDialogComponent} from '../dialogs/base/create-snapshot-dialog/create-snapshot-dialog.component';
import {ScopeModelService} from '../services/api/scope-model.service';
import {ScopeModel} from '@core/model/scope-model';
import {ComponentCanDeactivate} from '../../guards/unsaved-changes.guard';
import {LanguageService} from '../services/language.service';
import {EventModel} from '@core/model/event-model';
import {EventModelService} from '../services/api/event-model.service';
import {EventGroupService} from '../services/api/event-group.service';
import {EventGroup} from '@core/model/event-group';
import {DatasetModel} from '@core/model/dataset-model';
import {DatasetModelService} from '../services/api/dataset-model.service';
import {FieldModelService} from '../services/api/field-model.service';
import {FieldModel} from '@core/model/field-model';

@Component({
	selector: 'app-configurator-editor',
	standalone: true,
	templateUrl: './configurator-editor.component.html',
	styleUrls: ['./configurator-editor.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ConfiguratorTreeComponent,
		ConfiguratorDetailComponent,
		MatTooltipModule
	]
})
export class ConfiguratorEditorComponent implements OnInit, ComponentCanDeactivate {
	@ViewChild(ConfiguratorTreeComponent) treeComponent!: ConfiguratorTreeComponent;
	@ViewChild(ConfiguratorDetailComponent) detailComponent!: ConfiguratorDetailComponent;

	projectId = '';
	project: ConfiguratorProject | null = null;
	workingProject: ConfiguratorProject | null = null;
	draftVersion: ProjectConfigVersion | null = null;
	loading = true;
	saving = false;
	selectedNode: string | null = null;
	modifiedFields = new Set<string>();
	scopeModelModificationCount = 0;
	datasetModelModificationCount = 0;

	eventModels: any[] = [];
	eventGroups: any[] = [];
	selectedScopeModelId: string | null = null;
	selectedEventModelId: string | null = null;
	selectedEventGroupId: string | null = null;

	fieldModels: any[] = [];
	selectedDatasetModelId: string | null = null;
	selectedFieldModelId: string | null = null;

	canRollback = false;
	canRollForward = false;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private configuratorService: ConfiguratorService,
		private scopeModelService: ScopeModelService,
		private eventModelService: EventModelService,
		private eventGroupService: EventGroupService,
		private datasetModelService: DatasetModelService,
		private fieldModelService: FieldModelService,
		public languageService: LanguageService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.projectId = this.route.snapshot.paramMap.get('projectId') || '';
		if(this.projectId) {
			this.initializeProject();
		}
	}

	canDeactivate(): boolean {
		if(this.hasModifications) {
			return confirm('You have unsaved changes. Are you sure you want to leave?');
		}
		return true;
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any): void {
		if(this.hasModifications) {
			$event.returnValue = true;
		}
	}

	initializeProject(): void {
		this.loading = true;

		localStorage.setItem('configProjectId', this.projectId);

		this.configuratorService.getProject(this.projectId).subscribe({
			next: project => {
				this.project = project;
				this.workingProject = {...project};

				if(project?.languages?.length) {
					this.languageService.setProjectLanguages(project.languages);

					const defaultLanguage = project.languages.find(language => language.isDefault)?.languageCode || project.languages[0].languageCode || 'en';
					this.languageService.setLanguage(defaultLanguage);
				}

				this.loadDraftVersion();
			},
			error: error => {
				console.error('Error selecting project:', error);
				this.snackBar.open('Failed to load project', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	loadDraftVersion(): void {
		this.configuratorService.getOrCreateDraft(this.projectId).subscribe({
			next: draft => {
				this.draftVersion = draft;
				this.loading = false;
				this.loadSnapshotState();
			},
			error: error => {
				console.error('Error loading draft version:', error);
				this.snackBar.open('Failed to load draft', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	loadSnapshotState(): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				this.canRollback = snapshots.currentIndex !== undefined && snapshots.currentIndex > 0;
				this.canRollForward = snapshots.currentIndex !== undefined && snapshots.snapshots !== undefined && snapshots.currentIndex < snapshots.snapshots.length - 1;
			},
			error: () => {
				this.canRollback = false;
				this.canRollForward = false;
			}
		});
	}

	onLanguageChange(event: Event): void {
		const select = event.target as HTMLSelectElement;
		this.languageService.setLanguage(select.value);
	}

	onNodeSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;

		if(nodeId === 'scope-models') {
			this.selectedScopeModelId = null;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;
			this.selectedDatasetModelId = null;
			this.selectedFieldModelId = null;
			this.eventModels = [];
			this.eventGroups = [];
			this.fieldModels = [];

			const scopeModelsComponent = this.detailComponent?.scopeModelsListComponent;
			if(scopeModelsComponent) {
				scopeModelsComponent.clearSelection();
			}
			const datasetModelsComponent = this.detailComponent?.datasetModelsListComponent;
			if(datasetModelsComponent) {
				datasetModelsComponent.clearSelection();
			}
			return;
		}

		if(nodeId === 'dataset-models') {
			this.selectedScopeModelId = null;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;
			this.selectedDatasetModelId = null;
			this.selectedFieldModelId = null;
			this.eventModels = [];
			this.eventGroups = [];
			this.fieldModels = [];

			const datasetModelsComponent = this.detailComponent?.datasetModelsListComponent;
			if(datasetModelsComponent) {
				datasetModelsComponent.clearSelection();
			}
			const scopeModelsComponent = this.detailComponent?.scopeModelsListComponent;
			if(scopeModelsComponent) {
				scopeModelsComponent.clearSelection();
			}
			return;
		}
	}

	onScopeModelsChanged(event: {modificationCount: number}): void {
		this.scopeModelModificationCount = event.modificationCount;
	}

	onDatasetModelsChanged(event: {modificationCount: number}): void {
		this.datasetModelModificationCount = event.modificationCount;
	}

	onFieldsUpdated(updates: Partial<ConfiguratorProject>): void {
		this.workingProject = {...this.workingProject!, ...updates};

		Object.keys(updates).forEach(key => {
			const originalValue = this.project![key as keyof ConfiguratorProject];
			const newValue = updates[key as keyof ConfiguratorProject];

			if(newValue !== null && originalValue !== null && typeof newValue === 'object' && typeof originalValue === 'object') {
				if(JSON.stringify(originalValue) !== JSON.stringify(newValue)) {
					this.modifiedFields.add(key);
				}
				else {
					this.modifiedFields.delete(key);
				}
			}
			else {
				if(originalValue !== newValue) {
					this.modifiedFields.add(key);
				}
				else {
					this.modifiedFields.delete(key);
				}
			}
		});
	}

	onSaveDraft(): void {
		if(!this.workingProject) {
			return;
		}

		this.saving = true;
		this.configuratorService.updateProject(this.projectId, this.workingProject).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				const savePromises: Promise<any>[] = [];

				if(this.scopeModelModificationCount > 0) {
					savePromises.push(this.saveScopeModelsAndEventModels());
				}

				if(this.datasetModelModificationCount > 0) {
					savePromises.push(this.saveDatasetModelsAndFieldModels());
				}

				if(savePromises.length > 0) {
					Promise.all(savePromises).then(() => {
						this.saving = false;
						this.snackBar.open('Draft saved', 'Close', {duration: 2000});
					}).catch((error: any) => {
						console.error('Error saving:', error);
						this.snackBar.open('Failed to save changes', 'Close', {duration: 3000});
						this.saving = false;
					});
				}
				else {
					this.saving = false;
					this.snackBar.open('Draft saved', 'Close', {duration: 2000});
				}
			},
			error: error => {
				console.error('Error saving draft:', error);
				this.snackBar.open('Failed to save draft', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	onDiscardChanges(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Discard Changes',
				message: 'Are you sure you want to discard all unsaved changes? This action cannot be undone.',
				confirmText: 'Discard',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.workingProject = {...this.project!};
				this.modifiedFields.clear();

				const scopeModelsComponent = this.detailComponent?.scopeModelsListComponent;
				if(scopeModelsComponent) {
					scopeModelsComponent.scopeModelManager.resetToOriginals();
					scopeModelsComponent.eventModelManager.resetToOriginals();
					scopeModelsComponent.eventGroupManager.resetToOriginals();
					scopeModelsComponent.loadScopeModels();
				}

				const datasetModelsComponent = this.detailComponent?.datasetModelsListComponent;
				if(datasetModelsComponent) {
					datasetModelsComponent.datasetModelManager.resetToOriginals();
					datasetModelsComponent.fieldModelManager.resetToOriginals();
					datasetModelsComponent.loadDatasetModels();
				}

				this.scopeModelModificationCount = 0;
				this.datasetModelModificationCount = 0;
				this.snackBar.open('Changes discarded', 'Close', {duration: 2000});
			}
		});
	}

	onPublish(): void {
		//TODO: Open publish dialog
		console.log('Publish clicked');
	}

	onBack(): void {
		localStorage.removeItem('configProjectId');
		this.router.navigate(['/configurator']);
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		const currentLanguage = this.languageService.currentLanguage;
		const defaultLanguage = this.languageService.getDefaultLanguageCode();

		return translations[currentLanguage] || translations[defaultLanguage] || Object.values(translations)[0] || '';
	}

	get hasModifications(): boolean {
		return this.modifiedFields.size > 0 || this.scopeModelModificationCount > 0 || this.datasetModelModificationCount > 0;
	}

	get totalModificationCount(): number {
		return this.modifiedFields.size + this.scopeModelModificationCount + this.datasetModelModificationCount;
	}

	onCreateSnapshot(): void {
		const dialogRef = this.dialog.open(CreateSnapshotDialogComponent, {
			width: '500px'
		});

		dialogRef.afterClosed().subscribe(summary => {
			if(summary && this.draftVersion) {
				this.configuratorService.createSnapshot(
					this.projectId,
					this.draftVersion.pk!,
					summary
				).subscribe({
					next: () => {
						this.snackBar.open('Snapshot created', 'Close', {duration: 2000});
						this.loadSnapshotState();
					},
					error: error => {
						console.error('Error creating snapshot:', error);
						this.snackBar.open('Failed to create snapshot', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onViewSnapshots(): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				const dialogRef = this.dialog.open(SnapshotsListDialogComponent, {
					width: '600px',
					data: {snapshots}
				});

				dialogRef.afterClosed().subscribe(result => {
					if(result?.action === 'restore') {
						this.restoreSnapshot(result.index);
					}
				});
			},
			error: error => {
				console.error('Error loading snapshots:', error);
				this.snackBar.open('Failed to load snapshots', 'Close', {duration: 3000});
			}
		});
	}

	private restoreSnapshot(targetIndex: number): void {
		if(!this.draftVersion) {
			return;
		}

		this.configuratorService.getSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: snapshots => {
				const currentIndex = snapshots.currentIndex;

				if(currentIndex === undefined) {
					this.snackBar.open('Invalid snapshot state', 'Close', {duration: 3000});
					return;
				}

				if(targetIndex < currentIndex) {
					const stepsBack = currentIndex - targetIndex;
					this.performRollback(stepsBack);
				}
				else if(targetIndex > currentIndex) {
					const stepsForward = targetIndex - currentIndex;
					this.performRollForward(stepsForward);
				}
			}
		});
	}

	private performRollback(steps: number): void {
		if(steps <= 0 || !this.draftVersion) {
			return;
		}

		this.configuratorService.rollbackSnapshot(this.projectId, this.draftVersion.pk!).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				if(steps > 1) {
					this.performRollback(steps - 1);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					this.loadSnapshotState();
				}
			},
			error: error => {
				console.error('Error rolling back:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
			}
		});
	}

	private performRollForward(steps: number): void {
		if(steps <= 0 || !this.draftVersion) {
			return;
		}

		this.configuratorService.rollForwardSnapshot(this.projectId, this.draftVersion.pk!).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				if(steps > 1) {
					this.performRollForward(steps - 1);
				}
				else {
					this.snackBar.open('Restored to snapshot', 'Close', {duration: 2000});
					this.loadSnapshotState();
				}
			},
			error: error => {
				console.error('Error rolling forward:', error);
				this.snackBar.open('Failed to restore snapshot', 'Close', {duration: 3000});
			}
		});
	}

	private saveScopeModelsAndEventModels(): Promise<any> {
		const scopeModelsComponent = this.detailComponent?.scopeModelsListComponent;

		if(!scopeModelsComponent) {
			return Promise.resolve();
		}

		const savePromises: Promise<any>[] = [];

		scopeModelsComponent.modifiedScopeModelIds.forEach((id: string) => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = scopeModelsComponent.originalScopeModels.find((sm: ScopeModel) => sm.scopeModelId === originalId);
				if(original) {
					savePromises.push(
						this.scopeModelService.deleteScopeModel(this.projectId, originalId).toPromise()
					);
				}
			}
			else if(id.startsWith('temp-')) {
				const scopeModel = scopeModelsComponent.scopeModels.find((sm: ScopeModel) => sm.scopeModelId === id);
				if(scopeModel) {
					savePromises.push(
						this.scopeModelService.createScopeModel(this.projectId, scopeModel).toPromise()
					);
				}
			}
			else {
				const scopeModel = scopeModelsComponent.scopeModels.find((sm: ScopeModel) => sm.scopeModelId === id);
				if(scopeModel) {
					savePromises.push(
						this.scopeModelService.updateScopeModel(this.projectId, id, scopeModel).toPromise()
					);
				}
			}
		});

		scopeModelsComponent.modifiedEventModelIds.forEach((id: string) => {
			const eventModel = scopeModelsComponent.eventModels.find((em: EventModel) => em.eventModelId === id);
			if(eventModel) {
				savePromises.push(
					this.eventModelService.updateEventModel(this.projectId, id, eventModel).toPromise()
				);
			}
		});

		scopeModelsComponent.modifiedEventGroupIds.forEach((id: string) => {
			const eventGroup = scopeModelsComponent.eventGroups.find((eg: EventGroup) => eg.eventGroupId === id);
			if(eventGroup) {
				savePromises.push(
					this.eventGroupService.updateEventGroup(this.projectId, id, eventGroup).toPromise()
				);
			}
		});

		return Promise.all(savePromises).then(() => {
			scopeModelsComponent.scopeModelManager.syncOriginalsWithCurrent();
			scopeModelsComponent.eventModelManager.syncOriginalsWithCurrent();
			scopeModelsComponent.eventGroupManager.syncOriginalsWithCurrent();

			scopeModelsComponent.loadScopeModels();

			if(scopeModelsComponent.selectedScopeModel) {
				this.eventModelService.getEventModels(this.projectId).subscribe({
					next: allEventModels => {
						scopeModelsComponent.eventModelManager.setAll(allEventModels);
						scopeModelsComponent.eventModelManager.syncOriginalsWithCurrent();
					}
				});
			}

			this.scopeModelModificationCount = 0;

			if(this.treeComponent) {
				this.treeComponent.reloadScopeModels();
			}
		});
	}

	private saveDatasetModelsAndFieldModels(): Promise<any> {
		const datasetModelsComponent = this.detailComponent?.datasetModelsListComponent;

		if(!datasetModelsComponent) {
			return Promise.resolve();
		}

		const savePromises: Promise<any>[] = [];

		datasetModelsComponent.modifiedDatasetModelIds.forEach((id: string) => {
			if(id.endsWith('-deleted')) {
				const originalId = id.replace('-deleted', '');
				const original = datasetModelsComponent.originalDatasetModels.find((dm: DatasetModel) => dm.datasetModelId === originalId);
				if(original) {
					savePromises.push(
						this.datasetModelService.deleteDatasetModel(this.projectId, originalId).toPromise()
					);
				}
			}
			else if(id.startsWith('temp-')) {
				const datasetModel = datasetModelsComponent.datasetModels.find((dm: DatasetModel) => dm.datasetModelId === id);
				if(datasetModel) {
					savePromises.push(
						this.datasetModelService.createDatasetModel(this.projectId, datasetModel).toPromise()
					);
				}
			}
			else {
				const datasetModel = datasetModelsComponent.datasetModels.find((dm: DatasetModel) => dm.datasetModelId === id);
				if(datasetModel) {
					savePromises.push(
						this.datasetModelService.updateDatasetModel(this.projectId, id, datasetModel).toPromise()
					);
				}
			}
		});

		datasetModelsComponent.modifiedFieldModels.forEach((id: string) => {
			const fieldModel = datasetModelsComponent.fieldModels.find((fm: FieldModel) => fm.fieldModelId === id);
			if(fieldModel) {
				savePromises.push(
					this.fieldModelService.updateFieldModel(this.projectId, id, fieldModel).toPromise()
				);
			}
		});

		return Promise.all(savePromises).then(() => {
			datasetModelsComponent.datasetModelManager.syncOriginalsWithCurrent();
			datasetModelsComponent.fieldModelManager.syncOriginalsWithCurrent();

			datasetModelsComponent.loadDatasetModels();

			if(datasetModelsComponent.selectedDatasetModel) {
				this.fieldModelService.getFieldModels(this.projectId).subscribe({
					next: allFieldModels => {
						datasetModelsComponent.fieldModelManager.setAll(allFieldModels);
						datasetModelsComponent.fieldModelManager.syncOriginalsWithCurrent();
					}
				});
			}

			this.datasetModelModificationCount = 0;

			if(this.treeComponent) {
				this.treeComponent.reloadDatasetModels();
			}
		});
	}

	onScopeModelContextChanged(context: {
		eventModels: any[];
		eventGroups: any[];
		selectedScopeModelId: string | null;
		selectedEventModelId: string | null;
		selectedEventGroupId: string | null;
	}): void {
		this.eventModels = context.eventModels;
		this.eventGroups = context.eventGroups;
		this.selectedScopeModelId = context.selectedScopeModelId;
		this.selectedEventModelId = context.selectedEventModelId;
		this.selectedEventGroupId = context.selectedEventGroupId;
	}

	onDatasetModelContextChanged(context: {
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}): void {
		this.fieldModels = context.fieldModels;
		this.selectedDatasetModelId = context.selectedDatasetModelId;
		this.selectedFieldModelId = context.selectedFieldModelId;
	}
}
