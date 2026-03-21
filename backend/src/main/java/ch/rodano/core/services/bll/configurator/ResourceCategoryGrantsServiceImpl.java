package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.services.dao.configurator.ResourceCategoryGrantsDAOService;

@Service
@Transactional
public class ResourceCategoryGrantsServiceImpl implements ResourceCategoryGrantsService {

	private final ResourceCategoryGrantsDAOService resourceCategoryGrantsDAOService;

	public ResourceCategoryGrantsServiceImpl(final ResourceCategoryGrantsDAOService resourceCategoryGrantsDAOService) {
		this.resourceCategoryGrantsDAOService = resourceCategoryGrantsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, List<UUID>> getResourceCategoryGrants(final UUID projectId) {
		return resourceCategoryGrantsDAOService.getResourceCategoryGrants(projectId);
	}

	@Override
	public void saveResourceCategoryGrants(final UUID projectId, final Map<UUID, List<UUID>> profileResourceCategoryMap) {
		resourceCategoryGrantsDAOService.saveResourceCategoryGrants(projectId, profileResourceCategoryMap);
	}
}
