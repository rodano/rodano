package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import ch.rodano.api.config.EntityRightDTO;

public interface DatasetModelRightsService {

	Map<UUID, Map<UUID, EntityRightDTO>> getDatasetModelRights(UUID projectId);

	void saveDatasetModelRights(UUID projectId, Map<UUID, Map<UUID, EntityRightDTO>> rights);
}
