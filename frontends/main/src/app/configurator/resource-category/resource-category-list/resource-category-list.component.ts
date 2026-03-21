import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {ResourceCategoryManagerService} from '../../services/manager/resource-category-manager.service';
import {ResourceCategoryDialogService} from '../../services/dialogs/resource-category-dialog.service';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryDetailComponent} from '../resource-category-detail/resource-category-detail.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {Profile} from '@core/model/profile';
import {
	ResourceCategoryGrantsMatrixComponent
} from '../resource-category-grants-matrix/resource-category-grants-matrix.component';

@Component({
	selector: 'app-resource-category-list',
	standalone: true,
	templateUrl: './resource-category-list.component.html',
	styleUrls: ['../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, ResourceCategoryDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective,
		ResourceCategoryGrantsMatrixComponent]
})
export class ResourceCategoryListComponent
	extends BaseListComponent<ResourceCategory>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() resourceCategoriesChanged = new EventEmitter<boolean>();
	@Output() resourceCategoryContextChanged = new EventEmitter<{
		resourceCategories: any[];
		selectedResourceCategoryId: string | null;
	}>();

	showMatrix = false;

	constructor(
		public resourceCategoryManager: ResourceCategoryManagerService,
		public override languageService: LanguageService,
		private profileManager: ProfileManagerService,
		private resourceCategoryDialogService: ResourceCategoryDialogService,
		snackBar: MatSnackBar
	) {
		super(resourceCategoryManager, languageService, snackBar);
	}

	getEntityId(rc: ResourceCategory): string {return rc.categoryId;}
	getNodePrefix(): string {return 'resourceCategory';}
	getListNodeName(): string {return 'resourceCategories';}

	get resourceCategories(): ResourceCategory[] {return this.resourceCategoryManager.getAll();}
	get selectedResourceCategory(): ResourceCategory | null {return this.selected as ResourceCategory | null;}
	get modifiedResourceCategoryIds(): Set<string> {return this.resourceCategoryManager.getModifiedIds();}
	get originalResourceCategories(): ResourceCategory[] {return this.resourceCategoryManager.getOriginals();}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadResourceCategories(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			resourceCategories: this.resourceCategoryManager.load(this.projectId)
		}).subscribe({
			next: ({resourceCategories}) => this.afterLoad(resourceCategories),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'resourceCategories')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.resourceCategoriesChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.resourceCategoryContextChanged.emit({
			resourceCategories: [...this.resourceCategories],
			selectedResourceCategoryId: this.selected?.categoryId || null
		});
	}

	onCreate(): void {
		this.resourceCategoryDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: ResourceCategory | null) => {
				if(result) {
					this.resourceCategoryManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('ResourceCategory'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create resource category', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: ResourceCategory): void {
		this.resourceCategoryManager.update(updated);
		this.selected = this.resourceCategoryManager.getById(updated.categoryId) || null;
		this.emitModificationChange();
	}

	onDeleted(resourceCategoryId: string): void {
		const resourceCategory = this.resourceCategories.find(f => f.categoryId === resourceCategoryId);
		if(!resourceCategory) {
			return;
		}
		this.resourceCategoryManager.delete(this.projectId, resourceCategory.categoryId).subscribe({
			next: () => this.afterDelete(resourceCategory, 'ResourceCategory'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete resource category', 'Close', {duration: 3000});
			}
		});
	}

	onSelectResourceCategory(rc: ResourceCategory): void {this.onSelect(rc);}
	onCreateResourceCategory(): void {this.onCreate();}
	onResourceCategoryUpdated(rc: ResourceCategory): void {this.onUpdated(rc);}
	onResourceCategoryDeleted(id: string): void {this.onDeleted(id);}

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selected = null;
		}
	}
}
