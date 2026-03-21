package ch.rodano.core.services.dao.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface ScopeModelRightsDAOService {

	Map<UUID, Map<UUID, EntityRightDTO>> getScopeModelRights(UUID projectId);

	void saveScopeModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
