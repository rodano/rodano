package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ValidatorDTO;

public interface ValidatorService {

	List<ValidatorDTO> getValidators(UUID projectId, String view);

	default List<ValidatorDTO> getValidators(final UUID projectId) {
		return getValidators(projectId, "summary");
	}

	ValidatorDTO getValidator(UUID projectId, UUID validatorId);

	ValidatorDTO createValidator(UUID projectId, ValidatorDTO validator);

	ValidatorDTO updateValidator(UUID projectId, UUID validatorId, ValidatorDTO validator);

	void deleteValidator(UUID projectId, UUID validatorId);
}
