import {ResourceCategory} from '@core/model/resource-category';
import {ResourceCategoryManagerService} from '../manager/resource-category-manager.service';

export interface ResourceCategoryContext {
	resourceCategoryManager: ResourceCategoryManagerService;
	resourceCategories: ResourceCategory[];
	originalResourceCategories: ResourceCategory[];
	modifiedResourceCategoryIds: Set<string>;
}
