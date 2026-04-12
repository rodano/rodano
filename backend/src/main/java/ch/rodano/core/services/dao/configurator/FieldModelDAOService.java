package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.FieldModelDTO;

public interface FieldModelDAOService {

	List<FieldModelDTO> getFieldModels(UUID projectId, String view);

	List<FieldModelDTO> getFieldModelsSummary(UUID projectId);

	List<FieldModelDTO> getFieldModelsFull(UUID projectId);

	FieldModelDTO getFieldModel(UUID projectId, UUID fieldModelId);

	FieldModelDTO createFieldModel(UUID projectId, FieldModelDTO fieldModel);

	FieldModelDTO updateFieldModel(UUID projectId, UUID fieldModelId, FieldModelDTO fieldModel);

	void deleteFieldModel(UUID projectId, UUID fieldModelId);

	boolean hasPatientData(UUID projectId, UUID fieldModelId);
}
