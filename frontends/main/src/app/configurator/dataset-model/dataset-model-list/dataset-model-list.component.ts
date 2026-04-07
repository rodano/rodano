import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {DatasetModel} from '@core/model/dataset-model';
import {forkJoin, Subscription} from 'rxjs';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DatasetModelDetailComponent} from '../dataset-model-detail/dataset-model-detail.component';
import {DatasetModelDialogService} from '../../services/dialogs/dataset-model-dialog.service';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {ProjectLanguage} from '@core/model/project-language';
import {HttpErrorResponse} from '@angular/common/http';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {FieldModel} from '@core/model/field-model';
import {FieldModelDialogService} from '../../services/dialogs/field-model-dialog.service';
import {FieldModelDetailComponent} from '../field-model/field-model-detail/field-model-detail.component';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {Profile} from '@core/model/profile';
import {
	DatasetModelRightsMatrixComponent
} from '../dataset-model-rights-matrix/dataset-model-rights-matrix.component';
import {RuleDetailComponent} from '../../rules/rule-detail/rule-detail.component';
import {Rule} from '@core/model/rule';

type ViewMode = 'dataset-detail' | 'field-list' | 'field-detail' | 'rule-editor';

@Component({
	selector: 'app-dataset-model-list',
	standalone: true,
	templateUrl: './dataset-model-list.component.html',
	styleUrls: ['../../shared/list-shared.css', '../../shared/breadcrumb-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DatasetModelDetailComponent,
		FieldModelDetailComponent, MatProgressSpinnerModule, EmptyStateComponent, ListHeaderComponent, ModifiedDirective,
		DatasetModelRightsMatrixComponent, RuleDetailComponent]
})
export class DatasetModelListComponent implements OnInit, OnChanges, OnDestroy {
	@ViewChild(RuleDetailComponent) ruleDetailComponent!: RuleDetailComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string>();
	@Output() datasetModelChanged = new EventEmitter<boolean>();
	@Output() datasetModelContextChanged = new EventEmitter<{
		datasetModels: any[];
		fieldModels: any[];
		selectedDatasetModelId: string | null;
		selectedFieldModelId: string | null;
	}>();

	selectedDatasetModel: DatasetModel | null = null;
	selectedFieldModelId: string | null = null;
	viewMode: ViewMode = 'dataset-detail';
	loading = false;

	selectedRule: Rule | null = null;
	ruleModified = false;
	returnTab: 'general' | 'rules' = 'general';
	selectedRuleEntityPath = '';
	ruleEditorContext: 'dataset-model' | 'field-model' = 'dataset-model';
	readonly ruleDomains = ['SCOPE', 'EVENT', 'DATASET'];
	readonly fieldModelRuleDomains = ['SCOPE', 'EVENT', 'DATASET', 'FIELD'];
	private originalRule: Rule | null = null;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	private currentFieldModels: FieldModel[] = [];

	showMatrix = false;

	constructor(
		public datasetModelManager: DatasetModelManagerService,
		public fieldModelManager: FieldModelManagerService,
		public languageService: LanguageService,
		private profileManager: ProfileManagerService,
		private datasetModelDialogService: DatasetModelDialogService,
		private fieldModelDialogService: FieldModelDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
		this.loadDatasetModels();
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.datasetModels.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('dataset-model-')) {
				const datasetModel = this.datasetModels.find(
					dm => dm.datasetModelId === nodeId?.replace('dataset-model-', ''));
				if(datasetModel) {
					this.selectedDatasetModel = datasetModel;
					this.updateFilteredFieldModels();
				}
			}
			else if(nodeId === 'dataset-models') {
				this.selectedDatasetModel = null;
				this.currentFieldModels = [];
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	selectById(id: string): void {
		const entity = this.datasetModels.find(dm => dm.datasetModelId === id);
		if(entity) {
			this.selectDatasetModel(entity);
		}
	}

	selectFieldModelById(id: string): void {
		this.viewMode = 'field-list';
		this.updateFilteredFieldModels();
		if(this.currentFieldModels.some(fm => fm.fieldModelId === id)) {
			this.selectedFieldModelId = id;
			this.viewMode = 'field-detail';
			this.nodeSelected.emit(`field-model-${id}`);
			this.emitContext();
		}
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

	get datasetModels(): DatasetModel[] {return this.datasetModelManager.getAll();}
	get fieldModels(): FieldModel[] {return this.currentFieldModels;}

	get modifiedDatasetModelIds(): Set<string> {return this.datasetModelManager.getModifiedIds();}
	get modifiedFieldModels(): Set<string> {return this.fieldModelManager.getModifiedIds();}

	get originalDatasetModels(): DatasetModel[] {return this.datasetModelManager.getOriginals();}
	get originalFieldModels(): FieldModel[] {return this.fieldModelManager.getOriginals();}

	get totalModificationCount(): number {
		return this.datasetModelManager.getModificationCount() + this.fieldModelManager.getModificationCount();
	}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadDatasetModels(): void {
		this.loading = true;

		forkJoin({
			datasetModels: this.datasetModelManager.load(this.projectId),
			fieldModels: this.fieldModelManager.loadFull(this.projectId)
		}).subscribe({
			next: ({datasetModels}) => {
				if(this.selectedDatasetModel) {
					this.selectedDatasetModel = datasetModels.find(
						dm => dm.datasetModelId === this.selectedDatasetModel!.datasetModelId
					) || null;
					this.updateFilteredFieldModels();
				}
				this.loading = false;
				this.emitContext();
			},
			error: error => {
				console.error('Error loading dataset models:', error);
				this.snackBar.open('Failed to load dataset models', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private updateFilteredFieldModels(): void {
		if(!this.selectedDatasetModel) {
			this.currentFieldModels = [];
			return;
		}
		this.currentFieldModels = this.fieldModelManager.getAllForDataset(this.selectedDatasetModel.datasetModelId);
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
		this.currentFieldModels = [];
		this.viewMode = 'dataset-detail';
		this.nodeSelected.emit('dataset-models');
	}

	backToDatasetDetail(): void {
		this.viewMode = 'dataset-detail';
		this.returnTab = 'general';
		this.selectedFieldModelId = null;
		this.emitContext();
	}

	backToDatasetRules(): void {
		this.viewMode = 'dataset-detail';
		this.returnTab = 'rules';
		this.emitContext();
	}

	backToFieldModelDetail(): void {
		this.viewMode = 'field-detail';
		this.returnTab = 'general';
		this.emitContext();
	}

	backToFieldModelRules(): void {
		this.viewMode = 'field-detail';
		this.returnTab = 'rules';
		this.emitContext();
	}

	private selectDatasetModel(datasetModel: DatasetModel): void {
		const previousDatasetModelId = this.selectedDatasetModel?.datasetModelId;

		this.selectedDatasetModel = datasetModel;
		this.selectedFieldModelId = null;

		if(previousDatasetModelId !== datasetModel.datasetModelId) {
			this.viewMode = 'dataset-detail';
		}

		this.updateFilteredFieldModels();
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
						this.loadDatasetModels();
					},
					error: error => {
						console.error('Error creating field model:', error);
						this.snackBar.open('Failed to create field model', 'Close', {duration: 3000});
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
		this.updateFilteredFieldModels();
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

				this.updateFilteredFieldModels();
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
		this.datasetModelChanged.emit(this.totalModificationCount > 0);
	}

	private emitContext(): void {
		this.datasetModelContextChanged.emit({
			datasetModels: [...this.datasetModels],
			fieldModels: this.currentFieldModels,
			selectedDatasetModelId: this.selectedDatasetModel?.datasetModelId || null,
			selectedFieldModelId: this.selectedFieldModelId
		});
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

	getFieldModelCount(datasetModelId: string): number {
		return this.fieldModelManager.getAllForDataset(datasetModelId).length;
	}

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selectedDatasetModel = null;
		}
	}

	get activeRuleDomains(): string[] {
		return this.ruleEditorContext === 'field-model'
			? this.fieldModelRuleDomains
			: this.ruleDomains;
	}

	getFieldModelLabel(fieldModelId: string): string {
		return this.languageService.getLabelById(fieldModelId, id => this.fieldModelManager.getById(id));
	}

	switchToRuleEditor(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
		this.selectedRuleEntityPath = `dataset-models/${this.selectedDatasetModel?.datasetModelId}`;
		this.ruleEditorContext = 'dataset-model';
		this.viewMode = 'rule-editor';
	}

	onFieldModelRuleEdit(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
		this.selectedRuleEntityPath = `field-models/${this.selectedFieldModelId}`;
		this.ruleEditorContext = 'field-model';
		this.viewMode = 'rule-editor';
	}

	onRuleChanged(): void {
		this.ruleModified = true;
	}

	onSaveRule(): void {
		this.ruleDetailComponent?.onSave();
	}

	onRevertRule(): void {
		if(this.originalRule) {
			this.selectedRule = JSON.parse(JSON.stringify(this.originalRule));
			this.ruleModified = false;
		}
	}

	onRuleSaved(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
	}
}
