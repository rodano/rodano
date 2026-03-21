package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ResourceCategoryGrantsService {

	Map<UUID, List<UUID>> getResourceCategoryGrants(UUID projectId);

	void saveResourceCategoryGrants(UUID projectId, Map<UUID, List<UUID>> profileResourceCategoryMap);
}
