import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FormModel} from '@core/model/form-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {LanguageService} from '../../services/language.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {FormLayoutManagerService} from '../../services/manager/form-layout-manager.service';
import {FormModelDialogService} from '../../services/dialogs/form-model-dialog.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {forkJoin, of, Subscription} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';
import {FormModelDetailComponent} from '../form-model-detail/form-model-detail.component';
import {FormLayoutEditorComponent} from '../form-layout-editor/form-layout-editor.component';
import {Layout} from '@core/model/layout';
import {FormLayoutPreviewComponent} from '../form-layout-preview/form-layout-preview.component';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {Profile} from '@core/model/profile';
import {FormModelRightsMatrixComponent} from '../form-model-rights-matrix/form-model-rights-matrix.component';
import {RuleDetailComponent} from '../../rules/rule-detail/rule-detail.component';
import {Rule} from '@core/model/rule';
import {RuleConstraint} from '@core/model/rule-constraint';
import {RuleService} from '../../services/api/rule.service';
import {Cell} from '@core/model/cell';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';

type ViewMode = 'form-list' | 'form-detail' | 'layout-editor' | 'layout-preview' | 'rule-editor' | 'constraint-editor';

@Component({
	selector: 'app-form-model-list',
	standalone: true,
	templateUrl: './form-model-list.component.html',
	styleUrls: ['./form-model-list.component.css', '../../shared/breadcrumb-shared.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule, FormModelDetailComponent, FormLayoutEditorComponent,
		EmptyStateComponent, ListHeaderComponent, ModifiedDirective, FormLayoutPreviewComponent, FormModelRightsMatrixComponent, RuleDetailComponent]
})
export class FormModelListComponent implements OnInit, OnChanges, OnDestroy {
	@ViewChild(FormLayoutEditorComponent) formLayoutEditor?: FormLayoutEditorComponent;
	@ViewChild(RuleDetailComponent) ruleDetailComponent!: RuleDetailComponent;

	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() formModelsChanged = new EventEmitter<boolean>();
	@Output() formModelContextChanged = new EventEmitter<{
		formModels: FormModel[];
		layouts: Layout[];
		selectedFormModelId: string | null;
		selectedLayoutId: string | null;
	}>();

	selectedFormModel: FormModel | null = null;
	viewMode: ViewMode = 'form-list';
	loading = false;

	selectedRule: Rule | null = null;
	ruleModified = false;
	returnTab: 'general' | 'rules' = 'general';
	readonly ruleDomains = ['SCOPE', 'EVENT', 'FORM'];
	private originalRule: Rule | null = null;

	constraintRule: Rule | null = null;
	constraintModified = false;
	constraintEntityPath = '';
	constraintBreadcrumb = '';
	private originalConstraint: RuleConstraint | null = null;
	readonly constraintDomains = ['SCOPE', 'EVENT', 'DATASET', 'FIELD', 'FORM'];

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	showMatrix = false;

	constructor(
		public formModelManager: FormModelManagerService,
		public formLayoutManager: FormLayoutManagerService,
		public languageService: LanguageService,
		private profileManager: ProfileManagerService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private formModelDialogService: FormModelDialogService,
		private ruleService: RuleService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
		this.loadFormModels();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['selectedNode'] && this.formModels.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('form-model-')) {
				const formModel = this.formModels.find(
					fm => fm.formModelId === nodeId?.replace('form-model-', '')
				);
				if(formModel) {
					this.selectedFormModel = formModel;
					this.viewMode = 'form-detail';
					this.formLayoutManager.load(this.projectId, formModel.formModelId).subscribe(() => {
						this.emitContext();
					});
				}
			}
			else if(nodeId === 'form-models') {
				this.selectedFormModel = null;
				this.viewMode = 'form-list';
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedFormModel) {
			return 0;
		}
		if(this.viewMode === 'layout-editor' || this.viewMode === 'layout-preview') {
			return 3;
		}
		if(this.viewMode === 'form-detail') {
			return 2;
		}
		return 0;
	}

	get formModels(): FormModel[] {return this.formModelManager.getAll();}
	get modifiedFormModelIds(): Set<string> {return this.formModelManager.getModifiedIds();}
	get originalFormModels(): FormModel[] {return this.formModelManager.getOriginals();}

	get totalModificationCount(): number {
		return this.formModelManager.getModificationCount() + this.formLayoutManager.getModificationCount();
	}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadFormModels(): void {
		this.loading = true;
		this.formModelManager.load(this.projectId).subscribe({
			next: formModels => {
				if(this.selectedFormModel) {
					this.selectedFormModel = formModels.find(
						fm => fm.formModelId === this.selectedFormModel!.formModelId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: error => {
				console.error('Error loading form models:', error);
				this.snackBar.open('Failed to load form models', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	discardLayouts(): void {
		this.formLayoutEditor?.discardChanges();
	}

	get isSplitEditActive(): boolean {
		return this.formLayoutEditor?.isSplitEditActive() ?? false;
	}

	onLayoutUnsavedChanges(_hasChanges: boolean): void {
		this.emitModificationChange();
	}

	onSelectFormModel(formModel: FormModel): void {
		if(this.selectedFormModel?.formModelId === formModel.formModelId) {
			this.clearSelectionInternal();
		}
		else {
			this.selectFormModel(formModel);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.clearSelectionInternal();
	}

	private clearSelectionInternal(): void {
		this.selectedFormModel = null;
		this.viewMode = 'form-list';
		this.nodeSelected.emit('form-models');
	}

	private selectFormModel(formModel: FormModel): void {
		const previous = this.selectedFormModel?.formModelId;
		this.selectedFormModel = formModel;
		if(previous !== formModel.formModelId) {
			this.viewMode = 'form-detail';
			this.formLayoutManager.load(this.projectId, formModel.formModelId).subscribe(() => {
				this.emitContext();
			});
		}
		else {
			this.emitContext();
		}
		this.nodeSelected.emit(`form-model-${formModel.formModelId}`);
	}

	isSelected(formModel: FormModel): boolean {
		return this.selectedFormModel?.formModelId === formModel.formModelId;
	}

	switchToLayoutView(): void {
		if(!this.selectedFormModel) {
			return;
		}
		this.viewMode = 'layout-editor';
		this.emitContext();
	}

	switchToPreview(): void {
		if(!this.selectedFormModel) {
			return;
		}
		this.viewMode = 'layout-preview';
		this.emitContext();
	}

	backToFormDetail(tab: 'general' | 'rules' = 'general'): void {
		this.selectedRule = null;
		this.returnTab = tab;
		this.viewMode = 'form-detail';
	}

	onLayoutCreated(_layout: Layout): void {
		this.emitContext();
	}

	onLayoutDeleted(_layoutId: string): void {
		this.emitContext();
	}

	onCreateFormModel(): void {
		this.formModelDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Partial<FormModel> | null) => {
			if(result) {
				const newFormModel: FormModel = {
					formModelId: '',
					workflowIds: [],
					...result as any
				};
				this.formModelManager.create(this.projectId, newFormModel).subscribe({
					next: () => {
						this.snackBar.open('Form model created', 'Close', {duration: 2000});
						this.loadFormModels();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating form model:', error);
						this.snackBar.open('Failed to create form model', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onFormModelUpdated(updatedFormModel: FormModel): void {
		this.selectedFormModel = this.formModelManager.getById(updatedFormModel.formModelId) || null;
		this.emitModificationChange();
	}

	onFormModelDeleted(formModelId: string): void {
		this.formModelManager.delete(this.projectId, formModelId).subscribe({
			next: () => {
				this.snackBar.open('Form model deleted', 'Close', {duration: 2000});
				if(this.selectedFormModel?.formModelId === formModelId) {
					this.clearSelectionInternal();
				}
				this.loadFormModels();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting form model:', error);
				this.snackBar.open('Failed to delete form model', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.formModelsChanged.emit(this.totalModificationCount > 0);
	}

	private emitContext(): void {
		this.formModelContextChanged.emit({
			formModels: [...this.formModels],
			layouts: [...this.formLayoutManager.getAll()],
			selectedFormModelId: this.selectedFormModel?.formModelId || null,
			selectedLayoutId: null
		});
	}

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selectedFormModel = null;
		}
	}

	switchToRuleEditor(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
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

	onEditLayoutConstraint(event: {layout: Layout}): void {
		const fm = this.selectedFormModel;
		if(!fm) {
			return;
		}
		const path = `form-models/${fm.formModelId}/layouts/${event.layout.formLayoutId}`;
		this.constraintBreadcrumb = `Layout: ${event.layout.id}`;
		this.openConstraintEditor(path);
	}

	onEditCellConstraint(event: {layout: Layout; cell: Cell}): void {
		const fm = this.selectedFormModel;
		if(!fm) {
			return;
		}
		const path = `form-models/${fm.formModelId}/layouts/${event.layout.formLayoutId}/cells/${event.cell.formLayoutCellId}`;
		this.constraintBreadcrumb = `Cell: ${event.cell.id}`;
		this.openConstraintEditor(path);
	}

	private openConstraintEditor(entityPath: string): void {
		this.constraintEntityPath = entityPath;
		forkJoin({
			constraint: this.ruleService.getConstraint(this.projectId, entityPath),
			eventModels: this.eventModelManager.isLoaded()
				? of(null)
				: this.eventModelManager.load(this.projectId),
			eventGroups: this.eventGroupManager.isLoaded()
				? of(null)
				: this.eventGroupManager.load(this.projectId)
		}).subscribe(({constraint}) => {
			this.constraintRule = {
				ruleId: undefined,
				constraint: constraint as any,
				actions: [],
				tags: [],
				message: {}
			};
			this.originalConstraint = JSON.parse(JSON.stringify(constraint));
			this.constraintModified = false;
			this.viewMode = 'constraint-editor';
		});
	}

	backToLayoutEditor(): void {
		this.constraintRule = null;
		this.viewMode = 'layout-editor';
	}

	onConstraintChanged(): void {
		this.constraintModified = true;
	}

	onConstraintSaved(rule: Rule): void {
		this.constraintModified = false;
		this.originalConstraint = JSON.parse(JSON.stringify(rule.constraint));
		this.constraintRule = rule;
	}

	onRevertConstraint(): void {
		if(this.originalConstraint) {
			this.constraintRule = {
				ruleId: undefined,
				constraint: this.originalConstraint as any,
				actions: [],
				tags: [],
				message: {}
			};
			this.constraintModified = false;
		}
	}
}
