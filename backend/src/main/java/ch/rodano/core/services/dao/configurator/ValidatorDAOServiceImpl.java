package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.ValidatorDTO;
import ch.rodano.core.model.jooq.tables.records.ValidatorRecord;

import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;

@Repository
public class ValidatorDAOServiceImpl implements ValidatorDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ValidatorDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ValidatorDTO> getValidators(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getValidatorsFull(projectId);
			case VIEW_SUMMARY -> getValidatorsSummary(projectId);
			default -> getValidatorsSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "validators", key = "#projectId.toString() + ':summary'")
	public List<ValidatorDTO> getValidatorsSummary(final UUID projectId) {
		final var records = dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.orderBy(VALIDATOR.CODE.asc())
			.fetch();

		return records.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "validators", key = "#projectId.toString() + ':full'")
	public List<ValidatorDTO> getValidatorsFull(final UUID projectId) {
		return getValidatorsSummary(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "validator", key = "#projectId.toString() + ':' + #validatorId.toString()")
	public ValidatorDTO getValidator(final UUID projectId, final UUID validatorId) {
		final var record = dslContext.selectFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':full'")
	})
	public ValidatorDTO createValidator(final UUID projectId, final ValidatorDTO dto) {
		final var validatorId = dto.getValidatorId() != null ? dto.getValidatorId() : UUID.randomUUID();

		dslContext.insertInto(VALIDATOR)
			.set(VALIDATOR.VALIDATOR_ID, validatorId)
			.set(VALIDATOR.PROJECT_ID, projectId)
			.set(VALIDATOR.CODE, dto.getId())
			.set(VALIDATOR.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(VALIDATOR.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(VALIDATOR.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(VALIDATOR.MESSAGE, jsonMapperService.toJson(dto.getMessage()))
			.set(VALIDATOR.REQUIRED, dto.isRequired())
			.set(VALIDATOR.IS_SCRIPT, dto.isScript())
			.set(VALIDATOR.WORKFLOW_ID, dto.getWorkflowId())
			.set(VALIDATOR.INVALID_STATE_ID, dto.getInvalidStateId())
			.set(VALIDATOR.VALID_STATE_ID, dto.getValidStateId())
			.execute();

		return getValidator(projectId, validatorId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "validator", key = "#projectId.toString() + ':' + #validatorId.toString()")
	})
	public ValidatorDTO updateValidator(final UUID projectId, final UUID validatorId, final ValidatorDTO dto) {
		dslContext.update(VALIDATOR)
			.set(VALIDATOR.CODE, dto.getId())
			.set(VALIDATOR.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(VALIDATOR.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(VALIDATOR.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(VALIDATOR.MESSAGE, jsonMapperService.toJson(dto.getMessage()))
			.set(VALIDATOR.REQUIRED, dto.isRequired())
			.set(VALIDATOR.IS_SCRIPT, dto.isScript())
			.set(VALIDATOR.WORKFLOW_ID, dto.getWorkflowId())
			.set(VALIDATOR.INVALID_STATE_ID, dto.getInvalidStateId())
			.set(VALIDATOR.VALID_STATE_ID, dto.getValidStateId())
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.execute();

		return getValidator(projectId, validatorId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "validators", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "validator", key = "#projectId.toString() + ':' + #validatorId.toString()")
	})
	public void deleteValidator(final UUID projectId, final UUID validatorId) {
		dslContext.deleteFrom(VALIDATOR)
			.where(VALIDATOR.PROJECT_ID.eq(projectId))
			.and(VALIDATOR.VALIDATOR_ID.eq(validatorId))
			.execute();
	}

	private ValidatorDTO mapToDTO(final ValidatorRecord record) {
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
