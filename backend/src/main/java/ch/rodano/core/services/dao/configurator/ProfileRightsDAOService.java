package ch.rodano.core.services.dao.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface ProfileRightsDAOService {

	Map<UUID, Map<UUID, EntityRightDTO>> getProfileRights(UUID projectId);

	void saveProfileRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
