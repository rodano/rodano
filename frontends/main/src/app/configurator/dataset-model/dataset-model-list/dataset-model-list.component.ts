import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {DatasetModel} from '@core/model/dataset-model';
import {Subscription} from 'rxjs';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DatasetModelDetailComponent} from '../dataset-model-detail/dataset-model-detail.component';
import {DatasetModelDialogService} from '../../services/dialogs/dataset-model-dialog.service';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {ProjectLanguage} from '@core/model/project-language';
import {HttpErrorResponse} from '@angular/common/http';
import {ConfiguratorService} from '../../services/api/configurator.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {FieldModel} from '@core/model/field-model';
import {FieldModelDialogService} from '../../services/dialogs/field-model-dialog.service';
import {FieldModelDetailComponent} from '../field-model/field-model-detail/field-model-detail.component';

type ViewMode = 'dataset-detail' | 'field-list' | 'field-detail';

@Component({
	selector: 'app-dataset-model-list',
	standalone: true,
	templateUrl: './dataset-model-list.component.html',
	styleUrls: ['./dataset-model-list.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DatasetModelDetailComponent,
		FieldModelDetailComponent,
		MatProgressSpinnerModule
	]
})
export class DatasetModelListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string>();
	@Output() datasetModelChanged = new EventEmitter<{modificationCount: number}>();
	@Output() datasetModelContextChanged = new EventEmitter<{
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}>();

	selectedDatasetModel: DatasetModel | null = null;
	selectedFieldModelId: string | null = null;
	viewMode: ViewMode = 'dataset-detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public datasetModelManager: DatasetModelManagerService,
		public fieldModelManager: FieldModelManagerService,
		private configuratorService: ConfiguratorService,
		private datasetModelDialogService: DatasetModelDialogService,
		private fieldModelDialogService: FieldModelDialogService,
		private languageService: LanguageService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadProject();
		this.loadDatasetModels();

		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.datasetModels.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('dataset-model-')) {
				const datasetModelId = nodeId.replace('dataset-model-', '');
				const datasetModel = this.datasetModels.find(dm => dm.datasetModelId === datasetModelId);
				if(datasetModel) {
					this.selectedDatasetModel = datasetModel;
				}
			}
			else if(nodeId === 'dataset-models') {
				this.selectedDatasetModel = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedDatasetModel) {
			return 0;
		}

		if(this.viewMode === 'dataset-detail' || this.viewMode === 'field-list') {
			return 2;
		}

		if(this.viewMode === 'field-detail') {
			return 3;
		}

		return 0;
	}

	get datasetModels(): DatasetModel[] {
		return this.datasetModelManager.getAll();
	}

	get fieldModels(): FieldModel[] {
		return this.fieldModelManager.getAll();
	}

	get modifiedDatasetModelIds(): Set<string> {
		return this.datasetModelManager.getModifiedIds();
	}

	get originalDatasetModels(): DatasetModel[] {
		return this.datasetModelManager.getOriginals();
	}

	get modifiedFieldModels(): Set<string> {
		return this.fieldModelManager.getModifiedIds();
	}

	get originalFieldModels(): FieldModel[] {
		return this.fieldModelManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.datasetModelManager.getModificationCount() + this.fieldModelManager.getModificationCount();
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

	loadDatasetModels(): void {
		this.loading = true;
		this.datasetModelManager.load(this.projectId).subscribe({
			next: (datasetModels: DatasetModel[]) => {
				if(this.selectedDatasetModel) {
					this.selectedDatasetModel = datasetModels.find(
						dm => dm.datasetModelId === this.selectedDatasetModel!.datasetModelId
					) || null;
				}
				this.loading = false;
			},
			error: error => {
				console.error('Error loading dataset models:', error);
				this.snackBar.open('Failed to load dataset models', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectDatasetModel(datasetModel: DatasetModel): void {
		if(this.selectedDatasetModel?.datasetModelId === datasetModel.datasetModelId) {
			this.clearSelection();
		}
		else {
			this.selectDatasetModel(datasetModel);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedDatasetModel = null;
		this.selectedFieldModelId = null;
		this.viewMode = 'dataset-detail';
		this.nodeSelected.emit('dataset-models');
	}

	backToDatasetDetail(): void {
		this.viewMode = 'dataset-detail';
		this.selectedFieldModelId = null;
		this.emitContext();
	}

	private selectDatasetModel(datasetModel: DatasetModel): void {
		const previousDatasetModelId = this.selectedDatasetModel?.datasetModelId;

		this.selectedDatasetModel = datasetModel;
		this.selectedFieldModelId = null;

		if(previousDatasetModelId !== datasetModel.datasetModelId) {
			this.viewMode = 'dataset-detail';
			this.fieldModelManager.setAll([]);
		}

		this.emitContext();

		this.fieldModelManager.loadForDataset(this.projectId, datasetModel.datasetModelId).subscribe({
			next: () => this.emitContext(),
			error: error => {
				console.error('Error loading field models:', error);
				this.fieldModelManager.setAll([]);
				this.emitContext();
			}
		});

		this.nodeSelected.emit(`dataset-model-${datasetModel.datasetModelId}`);
	}

	isSelected(datasetModel: DatasetModel): boolean {
		return this.selectedDatasetModel?.datasetModelId === datasetModel.datasetModelId;
	}

	onCreateDatasetModel(): void {
		this.datasetModelDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: DatasetModel | null) => {
			if(result) {
				this.datasetModelManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Dataset model created', 'Close', {duration: 2000});
						this.loadDatasetModels();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating dataset model:', error);
						this.snackBar.open('Failed to create dataset model', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onDatasetModelUpdated(updatedDatasetModel: DatasetModel): void {
		this.selectedDatasetModel = this.datasetModelManager.getById(updatedDatasetModel.datasetModelId) || null;
		this.emitModificationChange();
	}

	onDatasetModelDeleted(datasetModelId: string): void {
		const datasetModel = this.datasetModels.find(dm => dm.datasetModelId === datasetModelId);
		if(!datasetModel) {
			return;
		}
		this.performDelete(datasetModel);
	}

	private performDelete(datasetModel: DatasetModel): void {
		this.datasetModelManager.delete(this.projectId, datasetModel.datasetModelId).subscribe({
			next: () => {
				this.snackBar.open('Dataset model deleted', 'Close', {duration: 2000});

				if(this.selectedDatasetModel?.datasetModelId === datasetModel.datasetModelId) {
					this.clearSelection();
				}

				this.loadDatasetModels();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting dataset model:', error);
				this.snackBar.open('Failed to delete dataset model', 'Close', {duration: 3000});
			}
		});
	}

	onCreateFieldModel(): void {
		if(!this.selectedDatasetModel) {
			return;
		}

		this.fieldModelDialogService.openCreateDialog(
			this.projectId,
			this.selectedDatasetModel.datasetModelId,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const newFieldModel: FieldModel = {
					fieldModelId: '',
					datasetModelId: this.selectedDatasetModel!.datasetModelId,
					...result,
					validatorIds: [],
					workflowIds: [],
					possibleValues: []
				};

				this.fieldModelManager.create(this.projectId, newFieldModel).subscribe({
					next: () => {
						this.snackBar.open('Field model created', 'Close', {duration: 2000});
					},
					error: error => {
						console.error('Error creating fied model:', error);
						this.snackBar.open('Failed to create fied model', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onFieldModelUpdated(updatedFieldModel: FieldModel): void {
		if(!updatedFieldModel) {
			return;
		}
		this.fieldModelManager.update(updatedFieldModel);
		this.emitModificationChange();
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	onFieldModelDeleted(fieldModelId: string): void {
		this.fieldModelManager.delete(this.projectId, fieldModelId).subscribe({
			next: () => {
				this.snackBar.open('Field model deleted', 'Close', {duration: 2000});

				if(this.selectedFieldModelId === fieldModelId) {
					this.selectedFieldModelId = null;
					this.viewMode = 'field-list';
				}

				this.emitModificationChange();
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting field model:', error);
				this.snackBar.open('Failed to delete field model', 'Close', {duration: 3000});
			}
		});
	}

	switchToFieldModelView(): void {
		if(!this.selectedDatasetModel) {
			return;
		}
		this.viewMode = 'field-list';
		this.emitContext();
	}

	onSelectFieldModel(fieldModelId: string): void {
		if(this.selectedFieldModelId === fieldModelId) {
			this.selectedFieldModelId = null;
			this.viewMode = 'field-list';
		}
		else {
			this.selectedFieldModelId = fieldModelId;
			this.viewMode = 'field-detail';
			this.nodeSelected.emit(`field-model-${fieldModelId}`);
		}
		this.emitContext();
	}

	private emitModificationChange(): void {
		this.datasetModelChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.datasetModelContextChanged.emit({
			fieldModels: this.fieldModels,
			selectedDatasetModelId: this.selectedDatasetModel?.datasetModelId || null,
			selectedFieldModelId: this.selectedFieldModelId
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}

	hasLabelPatterns(datasetModel: DatasetModel): boolean {
		return !!(datasetModel.collapsedLabelPattern || datasetModel.expandedLabelPattern);
	}

	getTypeLabel(type: string): string {
		const typeMap: Record<string, string> = {
			STRING: 'String',
			AUTO_COMPLETION: 'Autocompleted String',
			DATE: 'Date',
			DATE_SELECT: 'Date with Selection',
			NUMBER: 'Number',
			SELECT: 'Combobox',
			RADIO: 'Radio',
			CHECKBOX: 'Checkbox',
			CHECKBOX_GROUP: 'Checkbox Group',
			TEXTAREA: 'Text Area',
			FILE: 'File'
		};
		return typeMap[type] || type;
	}

	getDataTypeLabel(dataType: string): string {
		const dataTypeMap: Record<string, string> = {
			STRING: 'String',
			DATE: 'Date',
			NUMBER: 'Number',
			BOOLEAN: 'Boolean',
			BLOB: 'Blob'
		};
		return dataTypeMap[dataType] || dataType;
	}

	isFieldModelModified(fieldModelId: string): boolean {
		return this.fieldModelManager.isModified(fieldModelId);
	}
}
