package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ResourceCategoryDTO;
import ch.rodano.api.exception.ConfigurationConstraintException;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ResourceCategoryDAOService;

@Service
@Transactional
public class ResourceCategoryServiceImpl implements ResourceCategoryService {

	private final ResourceCategoryDAOService resourceCategoryDAOService;

	public ResourceCategoryServiceImpl(final ResourceCategoryDAOService resourceCategoryDAOService) {
		this.resourceCategoryDAOService = resourceCategoryDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResourceCategoryDTO> getResourceCategories(final UUID projectId) {
		return resourceCategoryDAOService.getResourceCategories(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public ResourceCategoryDTO getResourceCategory(final UUID projectId, final UUID resourceCategoryId) {
		final var resourceCategory = resourceCategoryDAOService.getResourceCategory(projectId, resourceCategoryId);
		if(resourceCategory == null) {
			throw new NotFoundException("Resource category not found: " + resourceCategoryId);
		}
		return resourceCategory;
	}

	@Override
	public ResourceCategoryDTO createResourceCategory(final UUID projectId, final ResourceCategoryDTO resourceCategory) {
		return resourceCategoryDAOService.createResourceCategory(projectId, resourceCategory);
	}

	@Override
	public ResourceCategoryDTO updateResourceCategory(final UUID projectId, final UUID resourceCategoryId, final ResourceCategoryDTO resourceCategory) {
		final var existing = resourceCategoryDAOService.getResourceCategory(projectId, resourceCategoryId);
		if(existing == null) {
			throw new NotFoundException("Resource category not found: " + resourceCategoryId);
		}
		return resourceCategoryDAOService.updateResourceCategory(projectId, resourceCategoryId, resourceCategory);
	}

	@Override
	public void deleteResourceCategory(final UUID projectId, final UUID resourceCategoryId) {
		final var existing = resourceCategoryDAOService.getResourceCategory(projectId, resourceCategoryId);
		if(existing == null) {
			throw new NotFoundException("Resource category not found: " + resourceCategoryId);
		}
		if(resourceCategoryDAOService.hasResources(projectId, resourceCategoryId)) {
			throw new ConfigurationConstraintException(
				"Resource category '%s' cannot be deleted: it still has resources attached to it".formatted(existing.getId())
			);
		}
		resourceCategoryDAOService.deleteResourceCategory(projectId, resourceCategoryId);
	}
}
