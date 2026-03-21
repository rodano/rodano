package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ResourceCategoryGrantsDAOService {

	Map<UUID, List<UUID>> getResourceCategoryGrants(UUID projectId);

	void saveResourceCategoryGrants(UUID projectId, Map<UUID, List<UUID>> profileResourceCategoryMap);
}
