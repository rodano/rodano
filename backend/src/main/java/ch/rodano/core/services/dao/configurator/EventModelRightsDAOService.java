package ch.rodano.core.services.dao.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface EventModelRightsDAOService {

	Map<UUID, Map<UUID, EntityRightDTO>> getEventModelRights(UUID projectId);

	void saveEventModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
