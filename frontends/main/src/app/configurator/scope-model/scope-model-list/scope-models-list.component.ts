import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorConfigService} from '@core/services/configurator-config.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ScopeModelBasicInfoDialogComponent} from '../scope-model-dialog/scope-model-basic-info-dialog/scope-model-basic-info-dialog.component';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';
import {ConfiguratorService} from '@core/services/configurator.service';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatTooltipModule} from '@angular/material/tooltip';
import {
	ScopeModelRelationshipsDialogComponent
} from '../scope-model-dialog/scope-model-relationships-dialog/scope-model-relationships-dialog.component';
import {
	ScopeModelDefaultSettingsDialogComponent
} from '../scope-model-dialog/scope-model-default-settings-dialog/scope-model-default-settings-dialog.component';
import {
	ScopeModelResourcesDialogComponent
} from '../scope-model-dialog/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../language/language.service';

@Component({
	selector: 'app-scope-models-list',
	standalone: true,
	templateUrl: './scope-models-list.component.html',
	styleUrls: ['./scope-models-list.component.css'],
	imports: [
		CommonModule,
		MatTableModule,
		MatButtonModule,
		MatIconModule,
		MatTooltipModule
	]
})
export class ScopeModelsListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() scopeModelsChanged = new EventEmitter<{modificationCount: number}>();

	originalScopeModels: ScopeModel[] = [];
	scopeModels: ScopeModel[] = [];
	modifiedScopeModelIds = new Set<string>();
	modifiedFieldsByScopeModel = new Map<string, Set<string>>();

	loading = true;
	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	selectedScopeModel: ScopeModel | null = null;

	constructor(
		private configuratorConfigService: ConfiguratorConfigService,
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
		this.configuratorConfigService.getScopeModels(this.projectId).subscribe({
			next: (scopeModels: ScopeModel[]) => {
				this.originalScopeModels = JSON.parse(JSON.stringify(scopeModels));
				this.scopeModels = JSON.parse(JSON.stringify(scopeModels));

				this.modifiedScopeModelIds.clear();
				this.modifiedFieldsByScopeModel.clear();

				if(this.selectedScopeModel) {
					this.selectedScopeModel = this.scopeModels.find(
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
		if(this.isSelected(scopeModel)) {
			this.selectedScopeModel = null;
			this.nodeSelected.emit('scope-models');
		}
		else {
			this.selectedScopeModel = scopeModel;
			this.nodeSelected.emit(`scope-model-${scopeModel.scopeModelId}`);
		}
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
				this.configuratorConfigService.createScopeModel(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Scope model created', 'Close', {duration: 2000});
						this.loadScopeModels();
						this.scopeModelsChanged.emit({modificationCount: 0});
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
				const updatedScopeModel: ScopeModel = {
					...scopeModel,
					...result
				};

				this.scopeModels = this.scopeModels.map(sm =>
					sm.scopeModelId === scopeModel.scopeModelId ? updatedScopeModel : sm
				);

				const original = this.originalScopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId);

				if(original) {
					this.trackFieldChanges(scopeModel.scopeModelId, original, updatedScopeModel);
				}

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.selectedScopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId) || null;
				}

				this.scopeModelsChanged.emit({modificationCount: this.totalModifiedFieldsCount});
				this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
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
				const updatedScopeModel: ScopeModel = {
					...scopeModel,
					...result
				};

				this.scopeModels = this.scopeModels.map(sm =>
					sm.scopeModelId === scopeModel.scopeModelId ? updatedScopeModel : sm
				);

				const original = this.originalScopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId);

				if(original) {
					this.trackFieldChanges(scopeModel.scopeModelId, original, updatedScopeModel);
				}

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.selectedScopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId) || null;
				}

				this.scopeModelsChanged.emit({modificationCount: this.totalModifiedFieldsCount});
				this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
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
				const updatedScopeModel: ScopeModel = {
					...scopeModel,
					...result
				};

				this.scopeModels = this.scopeModels.map(sm =>
					sm.scopeModelId === scopeModel.scopeModelId ? updatedScopeModel : sm
				);

				const original = this.originalScopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId);

				if(original) {
					this.trackFieldChanges(scopeModel.scopeModelId, original, updatedScopeModel);
				}

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.selectedScopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId) || null;
				}

				this.scopeModelsChanged.emit({modificationCount: this.totalModifiedFieldsCount});
				this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
			}
		});
	}

	onEditResources(scopeModel: ScopeModel): void {
		const dialogRef = this.dialog.open(ScopeModelResourcesDialogComponent, {
			width: '600px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {
					...scopeModel,
					...result
				};

				this.scopeModels = this.scopeModels.map(sm =>
					sm.scopeModelId === scopeModel.scopeModelId ? updatedScopeModel : sm
				);

				const original = this.originalScopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId);

				if(original) {
					this.trackFieldChanges(scopeModel.scopeModelId, original, updatedScopeModel);
				}

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.selectedScopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModel.scopeModelId) || null;
				}

				this.scopeModelsChanged.emit({modificationCount: this.totalModifiedFieldsCount});
				this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
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
					this.configuratorConfigService.updateScopeModel(
						this.projectId,
						child.scopeModelId,
						updatedChild
					).toPromise()
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
					this.configuratorConfigService.updateScopeModel(
						this.projectId,
						child.scopeModelId,
						updatedChild
					).toPromise()
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
		this.configuratorConfigService.deleteScopeModel(this.projectId, scopeModel.scopeModelId).subscribe({
			next: () => {
				this.snackBar.open('Scope model deleted', 'Close', {duration: 2000});

				if(this.selectedScopeModel?.scopeModelId === scopeModel.scopeModelId) {
					this.selectedScopeModel = null;
					this.nodeSelected.emit('scope-models');
				}

				this.loadScopeModels();
				this.scopeModelsChanged.emit({modificationCount: 0});
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting scope model:', error);
				this.snackBar.open('Failed to delete scope model', 'Close', {duration: 3000});
			}
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

	getScopeModelCode(scopeModelId: string): string {
		const scopeModel = this.scopeModels.find(sm => sm.scopeModelId === scopeModelId);
		return scopeModel ? scopeModel.id : scopeModelId;
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

	private trackFieldChanges(scopeModelId: string, original: ScopeModel, updated: ScopeModel): void {
		const modifiedFields = new Set<string>();

		const simpleFieldsToCompare: (keyof ScopeModel)[] = [
			'id', 'virtual', 'defaultParentId', 'defaultProfileId'
		];

		simpleFieldsToCompare.forEach(field => {
			const originalValue = original[field];
			const updatedValue = updated[field];

			if(JSON.stringify(originalValue) !== JSON.stringify(updatedValue)) {
				modifiedFields.add(field as string);
			}
		});

		const translationFields: (keyof ScopeModel)[] = [
			'shortname', 'longname', 'description', 'pluralShortname'
		];

		translationFields.forEach(field => {
			const originalValue = original[field] as Record<string, string> | undefined;
			const updatedValue = updated[field] as Record<string, string> | undefined;

			if(originalValue && updatedValue) {
				const allLanguages = new Set([
					...Object.keys(originalValue),
					...Object.keys(updatedValue)
				]);

				allLanguages.forEach(lang => {
					if(originalValue[lang] !== updatedValue[lang]) {
						modifiedFields.add(`${field as string}.${lang}`);
					}
				});
			}
			else if(originalValue !== updatedValue) {
				modifiedFields.add(field as string);
			}
		});

		const arrayFieldsToCompare: (keyof ScopeModel)[] = [
			'parentIds', 'datasetModelIds', 'formModelIds', 'workflowIds'
		];

		arrayFieldsToCompare.forEach(field => {
			const originalValue = original[field];
			const updatedValue = updated[field];

			if(JSON.stringify(originalValue) !== JSON.stringify(updatedValue)) {
				modifiedFields.add(field as string);
			}
		});

		if(modifiedFields.size > 0) {
			this.modifiedFieldsByScopeModel.set(scopeModelId, modifiedFields);
			this.modifiedScopeModelIds.add(scopeModelId);
		}
		else {
			this.modifiedFieldsByScopeModel.delete(scopeModelId);
			this.modifiedScopeModelIds.delete(scopeModelId);
		}
	}

	get totalModifiedFieldsCount(): number {
		let count = 0;
		this.modifiedFieldsByScopeModel.forEach((fields: Set<string>) => {
			count += fields.size;
		});
		return count;
	}

	isFieldModifiedForScopeModel(scopeModelId: string, fieldName: string): boolean {
		const modifiedFields = this.modifiedFieldsByScopeModel.get(scopeModelId);

		if(!modifiedFields) {
			return false;
		}

		if(modifiedFields.has(fieldName)) {
			return true;
		}

		const languageFieldPattern = new RegExp(`^${fieldName}\\.`);

		return Array.from(modifiedFields).some(field => languageFieldPattern.test(field));
	}
}
