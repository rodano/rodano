package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.FieldModelDTO;

public interface FieldModelService {

	List<FieldModelDTO> getFieldModels(UUID projectId, String view);

	default List<FieldModelDTO> getFieldModels(final UUID projectId) {
		return getFieldModels(projectId, "summary");
	}

	FieldModelDTO getFieldModel(UUID projectId, UUID fieldModelId);

	FieldModelDTO createFieldModel(UUID projectId, FieldModelDTO fieldModel);

	FieldModelDTO updateFieldModel(UUID projectId, UUID fieldModelId, FieldModelDTO fieldModel);

	void deleteFieldModel(UUID projectId, UUID fieldModelId);
}
