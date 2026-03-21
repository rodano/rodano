package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeatureGrantsService {

	Map<UUID, List<UUID>> getFeatureGrants(UUID projectId);

	void saveFeatureGrants(UUID projectId, Map<UUID, List<UUID>> profileFeatureMap);
}
