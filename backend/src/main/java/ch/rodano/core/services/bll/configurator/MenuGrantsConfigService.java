package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuGrantsConfigService {

	Map<UUID, List<UUID>> getMenuGrants(UUID projectId);

	void saveMenuGrants(UUID projectId, Map<UUID, List<UUID>> profileMenuMap);
}
