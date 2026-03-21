package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface EventModelRightsService {

	Map<UUID, Map<UUID, EntityRightDTO>> getEventModelRights(UUID projectId);

	void saveEventModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
