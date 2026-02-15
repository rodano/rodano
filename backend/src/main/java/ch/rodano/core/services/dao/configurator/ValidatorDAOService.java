package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;

import ch.rodano.api.config.ValidatorDTO;

public interface ValidatorDAOService {

	List<ValidatorDTO> getValidators(UUID projectId);

	ValidatorDTO getValidator(UUID projectId, UUID validatorId);

	ValidatorDTO createValidator(UUID projectId, ValidatorDTO validator);

	ValidatorDTO updateValidator(UUID projectId, UUID validatorId, ValidatorDTO validator);

	void deleteValidator(UUID projectId, UUID validatorId);
}
