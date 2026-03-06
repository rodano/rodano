package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.FormModelDTO;

public interface FormModelService {

	List<FormModelDTO> getFormModels(UUID projectId);

	FormModelDTO getFormModel(UUID projectId, UUID formModelId);

	FormModelDTO createFormModel(UUID projectId, FormModelDTO formModel);

	FormModelDTO updateFormModel(UUID projectId, UUID formModelId, FormModelDTO formModel);

	void deleteFormModel(UUID projectId, UUID formModelId);
}
