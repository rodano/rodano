package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.DatasetModelDTO;

public interface DatasetModelService {

	List<DatasetModelDTO> getDatasetModels(UUID projectId, String view);

	default List<DatasetModelDTO> getDatasetModels(final UUID projectId) {
		return getDatasetModels(projectId, "summary");
	}

	DatasetModelDTO getDatasetModel(UUID projectId, UUID datasetModelId);

	DatasetModelDTO createDatasetModel(UUID projectId, DatasetModelDTO datasetModel);

	DatasetModelDTO updateDatasetModel(UUID projectId, UUID datasetModelId, DatasetModelDTO datasetModel);

	void deleteDatasetModel(UUID projectId, UUID datasetModelId);
}
