package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface ScopeModelRightsService {

	Map<UUID, Map<UUID, EntityRightDTO>> getScopeModelRights(UUID projectId);

	void saveScopeModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
