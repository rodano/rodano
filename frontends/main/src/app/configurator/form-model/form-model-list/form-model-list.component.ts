import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FormModel} from '@core/model/form-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {LanguageService} from '../../services/language.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {FormModelDialogService} from '../../services/dialogs/form-model-dialog.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {forkJoin, Observable, of, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {ProjectLanguage} from '@core/model/project-language';
import {FormModelDetailComponent} from '../form-model-detail/form-model-detail.component';
import {FormLayoutListComponent} from '../form-layout-list/form-layout-list.component';
import {FormLayoutEditorComponent} from '../form-layout-editor/form-layout-editor.component';
import {Layout} from '@core/model/layout';
import {FormLayoutService} from '../../services/api/form-layout.service';

type ViewMode = 'form-list' | 'form-detail' | 'layout-list' | 'layout-detail';

@Component({
	selector: 'app-form-model-list',
	standalone: true,
	templateUrl: './form-model-list.component.html',
	styleUrls: ['./form-model-list.component.css'],
	imports: [CommonModule, MatIconModule, MatTooltipModule, FormModelDetailComponent, FormLayoutListComponent,
		FormLayoutEditorComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective]
})
export class FormModelListComponent implements OnInit, OnChanges, OnDestroy {
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
	selectedLayout: Layout | null = null;
	layouts: Layout[] = [];
	viewMode: ViewMode = 'form-list';
	loading = false;

	modifiedLayoutIds = new Set<string>();
	pendingLayouts = new Map<string, {layout: Layout; formModelId: string}>();

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public formModelManager: FormModelManagerService,
		public languageService: LanguageService,
		private formModelDialogService: FormModelDialogService,
		private formLayoutService: FormLayoutService,
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
		if(this.viewMode === 'layout-detail') {
			return 3;
		}
		if(this.viewMode === 'form-detail' || this.viewMode === 'layout-list') {
			return 2;
		}
		return 0;
	}

	get formModels(): FormModel[] {return this.formModelManager.getAll();}
	get modifiedFormModelIds(): Set<string> {return this.formModelManager.getModifiedIds();}
	get originalFormModels(): FormModel[] {return this.formModelManager.getOriginals();}

	get totalModificationCount(): number {
		return this.formModelManager.getModificationCount();
	}

	loadFormModels(): void {
		this.loading = true;
		this.formModelManager.load(this.projectId).subscribe({
			next: formModels => {
				if(this.selectedFormModel) {
					this.selectedFormModel = formModels.find(
						fm => fm.formModelId === this.selectedFormModel!.formModelId
					) || null;
				}
				this.pendingLayouts.clear();
				this.modifiedLayoutIds = new Set();
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

	onLayoutChanged(layout: Layout): void {
		if(layout.formLayoutId && this.selectedFormModel) {
			this.modifiedLayoutIds = new Set([...this.modifiedLayoutIds, layout.formLayoutId]);
			this.pendingLayouts.set(layout.formLayoutId, {
				layout: JSON.parse(JSON.stringify(layout)),
				formModelId: this.selectedFormModel.formModelId
			});
			const idx = this.layouts.findIndex(l => l.formLayoutId === layout.formLayoutId);
			if(idx !== -1) {
				this.layouts[idx] = layout;
			}
		}
		this.emitModificationChange();
	}

	saveLayouts(): Observable<void> {
		if(this.pendingLayouts.size === 0) {
			return of(void 0);
		}
		const saves = Array.from(this.pendingLayouts.entries()).map(([layoutId, {layout, formModelId}]) =>
			this.formLayoutService.updateLayout(this.projectId, formModelId, layoutId, layout)
		);
		return forkJoin(saves).pipe(
			map(() => {
				this.pendingLayouts.clear();
				this.modifiedLayoutIds = new Set();
				this.emitModificationChange();
			})
		);
	}

	discardLayouts(): void {
		this.pendingLayouts.clear();
		this.modifiedLayoutIds = new Set();
		this.emitModificationChange();
		if(this.viewMode === 'layout-detail') {
			this.viewMode = 'layout-list';
			this.selectedLayout = null;
			this.emitContext();
		}
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
		this.selectedLayout = null;
		this.viewMode = 'form-list';
		this.nodeSelected.emit('form-models');
	}

	private selectFormModel(formModel: FormModel): void {
		const previous = this.selectedFormModel?.formModelId;
		this.selectedFormModel = formModel;
		this.selectedLayout = null;
		if(previous !== formModel.formModelId) {
			this.viewMode = 'form-detail';
			this.formLayoutService.getLayouts(this.projectId, formModel.formModelId).subscribe({
				next: layouts => {
					this.layouts = layouts;
					this.emitContext();
				},
				error: () => {
					this.layouts = [];
					this.emitContext();
				}
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
		this.viewMode = 'layout-list';
		this.selectedLayout = null;
		this.emitContext();
	}

	backToFormDetail(): void {
		this.viewMode = 'form-detail';
		this.selectedLayout = null;
		this.emitContext();
	}

	onSelectLayout(layout: Layout): void {
		if(this.selectedLayout?.formLayoutId === layout.formLayoutId) {
			this.selectedLayout = null;
			this.viewMode = 'layout-list';
		}
		else {
			this.selectedLayout = layout;
			this.viewMode = 'layout-detail';
		}
		this.emitContext();
	}

	onLayoutCreated(layout: Layout): void {
		this.layouts = [...this.layouts, layout];
		this.selectedLayout = layout;
		this.viewMode = 'layout-detail';
		this.emitContext();
	}

	onLayoutDeleted(layoutId: string): void {
		this.layouts = this.layouts.filter(l => l.formLayoutId !== layoutId);
		this.modifiedLayoutIds = new Set([...this.modifiedLayoutIds].filter(id => id !== layoutId));
		this.pendingLayouts.delete(layoutId);
		if(this.selectedLayout?.formLayoutId === layoutId) {
			this.selectedLayout = null;
			this.viewMode = 'layout-list';
			this.emitContext();
		}
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
		this.formModelsChanged.emit(
			this.totalModificationCount > 0 || this.modifiedLayoutIds.size > 0
		);
	}

	private emitContext(): void {
		this.formModelContextChanged.emit({
			formModels: [...this.formModels],
			layouts: [...this.layouts],
			selectedFormModelId: this.selectedFormModel?.formModelId || null,
			selectedLayoutId: this.selectedLayout?.formLayoutId || null
		});
	}
}
