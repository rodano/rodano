import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryService} from '../api/resource-category.service';

@Injectable({providedIn: 'root'})
export class ResourceCategoryManagerService {
	private tracker: EntityModificationTracker<ResourceCategory>;
	private loaded = false;

	constructor(private resourceCategoryService: ResourceCategoryService) {
		this.tracker = new EntityModificationTracker<ResourceCategory>(
			resourceCategory => resourceCategory.categoryId,
			['id', 'icon', 'color'],
			['shortname', 'longname', 'description'],
			[]
		);
	}

	load(projectId: string): Observable<ResourceCategory[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.resourceCategoryService.getResourceCategories(projectId).pipe(
			map(resourceCategories => {
				this.tracker.initialize(resourceCategories);
				this.loaded = true;
				return resourceCategories;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, resourceCategory: ResourceCategory): Observable<ResourceCategory> {
		return this.resourceCategoryService.createResourceCategory(projectId, resourceCategory).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(resourceCategory: ResourceCategory): void {
		this.tracker.updateEntity(resourceCategory);
	}

	delete(projectId: string, resourceCategoryId: string): Observable<void> {
		return this.resourceCategoryService.deleteResourceCategory(projectId, resourceCategoryId).pipe(
			map(() => {
				this.tracker.removeEntity(resourceCategoryId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): ResourceCategory[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): ResourceCategory[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): ResourceCategory | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(resourceCategoryId: string): boolean {
		return this.tracker.isModified(resourceCategoryId);
	}

	isFieldModified(resourceCategoryId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(resourceCategoryId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, resourceCategoryId: string, resourceCategory: ResourceCategory): Observable<ResourceCategory> {
		return this.resourceCategoryService.updateResourceCategory(projectId, resourceCategoryId, resourceCategory);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
