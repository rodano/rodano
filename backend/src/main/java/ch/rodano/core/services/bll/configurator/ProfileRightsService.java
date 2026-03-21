package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface ProfileRightsService {

	Map<UUID, Map<UUID, EntityRightDTO>> getProfileRights(UUID projectId);

	void saveProfileRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
