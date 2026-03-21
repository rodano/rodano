package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuGrantsConfigDAOService {

	Map<UUID, List<UUID>> getMenuGrants(UUID projectId);

	void saveMenuGrants(UUID projectId, Map<UUID, List<UUID>> profileMenuMap);
}
