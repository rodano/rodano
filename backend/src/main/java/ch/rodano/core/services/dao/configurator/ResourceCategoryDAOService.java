package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ResourceCategoryDTO;


public interface ResourceCategoryDAOService {

	List<ResourceCategoryDTO> getResourceCategories(UUID projectId);

	ResourceCategoryDTO getResourceCategory(UUID projectId, UUID resourceCategoryId);

	ResourceCategoryDTO createResourceCategory(UUID projectId, ResourceCategoryDTO resourceCategory);

	ResourceCategoryDTO updateResourceCategory(UUID projectId, UUID resourceCategoryId, ResourceCategoryDTO resourceCategory);

	void deleteResourceCategory(UUID projectId, UUID resourceCategoryId);

	boolean hasResources(UUID projectId, UUID resourceCategoryId);
}
