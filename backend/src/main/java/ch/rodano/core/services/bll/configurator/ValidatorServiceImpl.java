package ch.rodano.core.services.bll.configurator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.ValidatorDTO;
import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.services.dao.configurator.ValidatorDAOService;

@Service
@Transactional
public class ValidatorServiceImpl implements ValidatorService {

	private final ValidatorDAOService validatorDAOService;

	public ValidatorServiceImpl(final ValidatorDAOService validatorDAOService) {
		this.validatorDAOService = validatorDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ValidatorDTO> getValidators(final UUID projectId, final String view) {
		return validatorDAOService.getValidators(projectId, view);
	}

	@Override
	@Transactional(readOnly = true)
	public ValidatorDTO getValidator(final UUID projectId, final UUID validatorId) {
		final var validator = validatorDAOService.getValidator(projectId, validatorId);
		if(validator == null) {
			throw new NotFoundException("Validator not found: " + validatorId);
		}
		return validator;
	}

	@Override
	public ValidatorDTO createValidator(final UUID projectId, final ValidatorDTO validator) {
		return validatorDAOService.createValidator(projectId, validator);
	}

	@Override
	public ValidatorDTO updateValidator(final UUID projectId, final UUID validatorId, final ValidatorDTO validator) {
		final var existing = validatorDAOService.getValidator(projectId, validatorId);
		if(existing == null) {
			throw new NotFoundException("Validator not found: " + validatorId);
		}
		return validatorDAOService.updateValidator(projectId, validatorId, validator);
	}

	@Override
	public void deleteValidator(final UUID projectId, final UUID validatorId) {
		final var existing = validatorDAOService.getValidator(projectId, validatorId);
		if(existing == null) {
			throw new NotFoundException("Validator not found: " + validatorId);
		}
		validatorDAOService.deleteValidator(projectId, validatorId);
	}
}
