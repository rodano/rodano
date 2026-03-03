import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryService} from '../api/resource-category.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class ResourceCategoryManagerService extends BaseManagerService<ResourceCategory> {
	constructor(private resourceCategoryService: ResourceCategoryService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (rc: ResourceCategory) => rc.categoryId;}
	protected getSimpleFields(): (keyof ResourceCategory)[] {
		return [];
	}

	protected getTranslationFields(): (keyof ResourceCategory)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof ResourceCategory)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<ResourceCategory[]> {
		return this.resourceCategoryService.getResourceCategories(projectId);
	}

	protected createEntity(projectId: string, entity: ResourceCategory): Observable<ResourceCategory> {
		return this.resourceCategoryService.createResourceCategory(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.resourceCategoryService.deleteResourceCategory(projectId, id);
	}
}
