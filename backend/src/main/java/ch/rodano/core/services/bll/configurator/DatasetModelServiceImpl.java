package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.api.exception.ConfigurationConstraintException;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.DatasetModelDAOService;

@Service
@Transactional
public class DatasetModelServiceImpl implements DatasetModelService {

	private final DatasetModelDAOService datasetModelDAOService;

	public DatasetModelServiceImpl(final DatasetModelDAOService datasetModelDAOService) {
		this.datasetModelDAOService = datasetModelDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DatasetModelDTO> getDatasetModels(final UUID projectId, final String view) {
		return datasetModelDAOService.getDatasetModels(projectId, view);
	}

	@Override
	@Transactional(readOnly = true)
	public DatasetModelDTO getDatasetModel(final UUID projectId, final UUID datasetModelId) {
		final var datasetModel = datasetModelDAOService.getDatasetModel(projectId, datasetModelId);
		if(datasetModel == null) {
			throw new NotFoundException("Dataset model not found: " + datasetModelId);
		}
		return datasetModel;
	}

	@Override
	public DatasetModelDTO createDatasetModel(final UUID projectId, final DatasetModelDTO datasetModel) {
		return datasetModelDAOService.createDatasetModel(projectId, datasetModel);
	}

	@Override
	public DatasetModelDTO updateDatasetModel(final UUID projectId, final UUID datasetModelId, final DatasetModelDTO datasetModel) {
		final var existing = datasetModelDAOService.getDatasetModel(projectId, datasetModelId);
		if(existing == null) {
			throw new NotFoundException("Dataset model not found: " + datasetModelId);
		}
		return datasetModelDAOService.updateDatasetModel(projectId, datasetModelId, datasetModel);
	}

	@Override
	public void deleteDatasetModel(final UUID projectId, final UUID datasetModelId) {
		final var existing = datasetModelDAOService.getDatasetModel(projectId, datasetModelId);
		if(existing == null) {
			throw new NotFoundException("Dataset model not found: " + datasetModelId);
		}
		if(datasetModelDAOService.hasPatientData(projectId, datasetModelId)) {
			throw new ConfigurationConstraintException(
				"Dataset model '%s' cannot be deleted: it has existing patient data".formatted(existing.getId())
			);
		}
		datasetModelDAOService.deleteDatasetModel(projectId, datasetModelId);
	}
}
