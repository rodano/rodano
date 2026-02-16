package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.DatasetModelDTO;

public interface DatasetModelDAOService {

	List<DatasetModelDTO> getDatasetModels(UUID projectId, String view);

	List<DatasetModelDTO> getDatasetModelsSummary(UUID projectId);

	List<DatasetModelDTO> getDatasetModelsFull(UUID projectId);

	DatasetModelDTO getDatasetModel(UUID projectId, UUID datasetModelId);

	DatasetModelDTO createDatasetModel(UUID projectId, DatasetModelDTO datasetModel);

	DatasetModelDTO updateDatasetModel(UUID projectId, UUID datasetModelId, DatasetModelDTO datasetModel);

	void deleteDatasetModel(UUID projectId, UUID datasetModelId);
}
