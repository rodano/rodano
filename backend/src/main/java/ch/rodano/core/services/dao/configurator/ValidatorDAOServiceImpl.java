package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.ValidatorDTO;
import ch.rodano.core.model.jooq.tables.records.ValidatorRecord;

import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;

@Repository
public class ValidatorDAOServiceImpl implements ValidatorDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ValidatorDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "validators", key = "#projectId")
	public List<ValidatorDTO> getValidators(final UUID projectId) {
		final var validators = dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.orderBy(VALIDATOR.CODE)
			.fetch();

		return validators.stream()
			.map(record -> mapToDTO(record, projectId))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "validator", key = "#projectId + '-' + #validatorId")
	public ValidatorDTO getValidator(final UUID projectId, final UUID validatorId) {
		final var record = dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, projectId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "validators", "validator" }, allEntries = true)
	public ValidatorDTO createValidator(final UUID projectId, final ValidatorDTO validator) {
		final var validatorId = validator.getValidatorId() != null
			? validator.getValidatorId()
			: UUID.randomUUID();

		dslContext.insertInto(VALIDATOR)
			.set(VALIDATOR.VALIDATOR_ID, validatorId)
			.set(VALIDATOR.PROJECT_ID, projectId)
			.set(VALIDATOR.CODE, validator.getId())
			.set(VALIDATOR.SHORTNAME, jsonMapperService.toJson(validator.getShortname()))
			.set(VALIDATOR.LONGNAME, jsonMapperService.toJson(validator.getLongname()))
			.set(VALIDATOR.DESCRIPTION, jsonMapperService.toJson(validator.getDescription()))
			.set(VALIDATOR.MESSAGE, jsonMapperService.toJson(validator.getMessage()))
			.set(VALIDATOR.REQUIRED, validator.isRequired())
			.set(VALIDATOR.IS_SCRIPT, validator.isScript())
			.set(VALIDATOR.WORKFLOW_ID, validator.getWorkflowId())
			.set(VALIDATOR.INVALID_STATE_ID, validator.getInvalidStateId())
			.set(VALIDATOR.VALID_STATE_ID, validator.getValidStateId())
			.execute();

		return getValidator(projectId, validatorId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "validators", "validator" }, allEntries = true)
	public ValidatorDTO updateValidator(final UUID projectId, final UUID validatorId, final ValidatorDTO validator) {
		dslContext.update(VALIDATOR)
			.set(VALIDATOR.CODE, validator.getId())
			.set(VALIDATOR.SHORTNAME, jsonMapperService.toJson(validator.getShortname()))
			.set(VALIDATOR.LONGNAME, jsonMapperService.toJson(validator.getLongname()))
			.set(VALIDATOR.DESCRIPTION, jsonMapperService.toJson(validator.getDescription()))
			.set(VALIDATOR.MESSAGE, jsonMapperService.toJson(validator.getMessage()))
			.set(VALIDATOR.REQUIRED, validator.isRequired())
			.set(VALIDATOR.IS_SCRIPT, validator.isScript())
			.set(VALIDATOR.WORKFLOW_ID, validator.getWorkflowId())
			.set(VALIDATOR.INVALID_STATE_ID, validator.getInvalidStateId())
			.set(VALIDATOR.VALID_STATE_ID, validator.getValidStateId())
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.execute();

		return getValidator(projectId, validatorId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "validators", "validator" }, allEntries = true)
	public void deleteValidator(final UUID projectId, final UUID validatorId) {
		dslContext.deleteFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.execute();
	}

	private ValidatorDTO mapToDTO(final ValidatorRecord record, final UUID projectId) {
		final var dto = new ValidatorDTO();
		dto.setValidatorId(record.getValidatorId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setMessage(jsonMapperService.fromJson(record.getMessage(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setRequired(record.getRequired());
		dto.setScript(record.getIsScript());
		dto.setWorkflowId(record.getWorkflowId());
		dto.setInvalidStateId(record.getInvalidStateId());
		dto.setValidStateId(record.getValidStateId());

		return dto;
	}
}
