package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface FormModelRightsService {

	Map<UUID, Map<UUID, EntityRightDTO>> getFormModelRights(UUID projectId);

	void saveFormModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
