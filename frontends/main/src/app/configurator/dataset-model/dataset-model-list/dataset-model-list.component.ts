import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {DatasetModel} from '@core/model/dataset-model';
import {Subscription} from 'rxjs';
import {DatasetModelService} from '../../services/api/dataset-model.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DatasetModelDetailComponent} from '../dataset-model-detail/dataset-model-detail.component';
import {DatasetModelDialogService} from '../../services/dialogs/dataset-model-dialog.service';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {ProjectLanguage} from '@core/model/project-language';
import {HttpErrorResponse} from '@angular/common/http';
import {ConfiguratorService} from '../../services/api/configurator.service';

type ViewMode = 'list' | 'detail';

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
		selectedDatasetModelId: string | null;
	}>();

	selectedDatasetModel: DatasetModel | null = null;
	viewMode: ViewMode = 'list';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		private datasetModelService: DatasetModelService,
		public datasetModelManager: DatasetModelManagerService,
		private configuratorService: ConfiguratorService,
		private datasetModelDialogService: DatasetModelDialogService,
		private languageService: LanguageService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadProject();
		this.loadDatasetModels();

		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
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
		return this.viewMode === 'detail' ? 2 : 0;
	}

	get datasetModels(): DatasetModel[] {
		return this.datasetModelManager.getAll();
	}

	get modifiedDatasetModelIds(): Set<string> {
		return this.datasetModelManager.getModifiedIds();
	}

	get originalDatasetModels(): DatasetModel[] {
		return this.datasetModelManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.datasetModelManager.getModificationCount();
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

	private clearSelection(): void {
		this.selectedDatasetModel = null;
		this.viewMode = 'list';
		this.nodeSelected.emit('dataset-models');
	}

	backToDatasetDetail(): void {
		this.viewMode = 'list';
		this.emitContext();
	}

	private selectDatasetModel(datasetModel: DatasetModel): void {
		const previousDatasetModelId = this.selectedDatasetModel?.datasetModelId;

		this.selectedDatasetModel = datasetModel;

		if(previousDatasetModelId !== datasetModel.datasetModelId) {
			this.viewMode = 'detail';
		}

		this.emitContext();

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

	private emitModificationChange(): void {
		this.datasetModelChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.datasetModelContextChanged.emit({
			selectedDatasetModelId: this.selectedDatasetModel?.datasetModelId || null
		});
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.languageService.getDefaultTranslation(translations) || '';
	}

	hasLabelPatterns(datasetModel: DatasetModel): boolean {
		return !!(datasetModel.collapsedLabelPattern || datasetModel.expandedLabelPattern);
	}
}
