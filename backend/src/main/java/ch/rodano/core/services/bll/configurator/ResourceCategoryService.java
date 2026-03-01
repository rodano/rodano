package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ResourceCategoryDTO;

public interface ResourceCategoryService {

	List<ResourceCategoryDTO> getResourceCategories(UUID projectId);

	ResourceCategoryDTO getResourceCategory(UUID projectId, UUID resourceCategoryId);

	ResourceCategoryDTO createResourceCategory(UUID projectId, ResourceCategoryDTO resourceCategory);

	ResourceCategoryDTO updateResourceCategory(UUID projectId, UUID resourceCategoryId, ResourceCategoryDTO resourceCategory);

	void deleteResourceCategory(UUID projectId, UUID resourceCategoryId);
}
