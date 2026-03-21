package ch.rodano.core.services.bll.configurator;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.EntityRightDTO;
import ch.rodano.core.services.dao.configurator.DatasetModelRightsDAOService;

@Service
@Transactional
public class DatasetModelRightsServiceImpl implements DatasetModelRightsService {

	private final DatasetModelRightsDAOService datasetModelRightsDAOService;

	public DatasetModelRightsServiceImpl(final DatasetModelRightsDAOService datasetModelRightsDAOService) {
		this.datasetModelRightsDAOService = datasetModelRightsDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Map<UUID, EntityRightDTO>> getDatasetModelRights(final UUID projectId) {
		return datasetModelRightsDAOService.getDatasetModelRights(projectId);
	}

	@Override
	public void saveDatasetModelRights(final UUID projectId, final Map<UUID, Map<UUID, EntityRightDTO>> rights) {
		datasetModelRightsDAOService.saveDatasetModelRights(projectId, rights);
	}
}
