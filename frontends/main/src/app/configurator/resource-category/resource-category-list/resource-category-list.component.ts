import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ResourceCategoryManagerService} from '../../services/manager/resource-category-manager.service';
import {ResourceCategoryDialogService} from '../../services/dialogs/resource-category-dialog.service';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryDetailComponent} from '../resource-category-detail/resource-category-detail.component';

@Component({
	selector: 'app-resource-category-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ResourceCategoryDetailComponent,
		EmptyStateComponent
	],
	templateUrl: './resource-category-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ResourceCategoryListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() resourceCategoriesChanged = new EventEmitter<{modificationCount: number}>();
	@Output() resourceCategoryContextChanged = new EventEmitter<{
		resourceCategories: any[];
		selectedResourceCategoryId: string | null;
	}>();

	selectedResourceCategory: ResourceCategory | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public resourceCategoryManager: ResourceCategoryManagerService,
		public languageService: LanguageService,
		private resourceCategoryDialogService: ResourceCategoryDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadResourceCategories();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.resourceCategories.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('resource-category-')) {
				const resourceCategoryId = nodeId.replace('resource-category-', '');
				const resourceCategory = this.resourceCategories.find(rc => rc.categoryId === resourceCategoryId);
				if(resourceCategory) {
					this.selectedResourceCategory = resourceCategory;
				}
			}
			else if(nodeId === 'resource-categories') {
				this.selectedResourceCategory = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedResourceCategory) {
			return 0;
		}
		return 2;
	}

	get resourceCategories(): ResourceCategory[] {
		return this.resourceCategoryManager.getAll();
	}

	get modifiedResourceCategoryIds(): Set<string> {
		return this.resourceCategoryManager.getModifiedIds();
	}

	get originalResourceCategories(): ResourceCategory[] {
		return this.resourceCategoryManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.resourceCategoryManager.getModificationCount();
	}

	loadResourceCategories(): void {
		this.loading = true;
		forkJoin({
			resourceCategories: this.resourceCategoryManager.load(this.projectId)
		}).subscribe({
			next: ({resourceCategories}) => {
				if(this.selectedResourceCategory) {
					this.selectedResourceCategory = resourceCategories.find(
						rc => rc.categoryId === this.selectedResourceCategory!.categoryId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading resourceCategories:', error);
				this.snackBar.open('Failed to load resource categories', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectResourceCategory(resourceCategory: ResourceCategory): void {
		if(this.selectedResourceCategory?.categoryId === resourceCategory.categoryId) {
			this.clearSelection();
		}
		else {
			this.selectResourceCategory(resourceCategory);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedResourceCategory = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('resource-categories');
	}

	private selectResourceCategory(resourceCategory: ResourceCategory): void {
		const previousResourceCategoryId = this.selectedResourceCategory?.categoryId;
		this.selectedResourceCategory = resourceCategory;

		if(previousResourceCategoryId !== resourceCategory.categoryId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`resource-category-${resourceCategory.categoryId}`);
	}

	isSelected(resourceCategory: ResourceCategory): boolean {
		return this.selectedResourceCategory?.categoryId === resourceCategory.categoryId;
	}

	onCreateResourceCategory(): void {
		this.resourceCategoryDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: ResourceCategory | null) => {
			if(result) {
				this.resourceCategoryManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Resource category created', 'Close', {duration: 2000});
						this.loadResourceCategories();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating resource category', error);
						this.snackBar.open('Failed to create resource category', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onResourceCategoryUpdated(updatedResourceCategory: ResourceCategory): void {
		this.selectedResourceCategory = this.resourceCategoryManager.getById(updatedResourceCategory.categoryId) || null;
		this.emitModificationChange();
	}

	onResourceCategoryDeleted(resourceCategoryId: string): void {
		const resourceCategory = this.resourceCategories.find(f => f.categoryId === resourceCategoryId);
		if(!resourceCategory) {
			return;
		}
		this.performDelete(resourceCategory);
	}

	private performDelete(resourceCategory: ResourceCategory): void {
		this.resourceCategoryManager.delete(this.projectId, resourceCategory.categoryId).subscribe({
			next: () => {
				this.snackBar.open('Resource category deleted', 'Close', {duration: 2000});

				if(this.selectedResourceCategory?.categoryId === resourceCategory.categoryId) {
					this.clearSelection();
				}

				this.loadResourceCategories();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting resource category', error);
				this.snackBar.open('Failed to delete resource category', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.resourceCategoriesChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.resourceCategoryContextChanged.emit({
			resourceCategories: [...this.resourceCategories],
			selectedResourceCategoryId: this.selectedResourceCategory?.categoryId || null
		});
	}

	isModified(resourceCategoryId: string): boolean {
		return this.resourceCategoryManager.isModified(resourceCategoryId);
	}
}
