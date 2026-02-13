package ch.rodano.core.services.dao.configurator;

import java.math.BigDecimal;
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

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.config.PossibleValueDTO;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.jooq.tables.records.FieldModelRecord;
import ch.rodano.core.model.jooq.tables.records.FieldPossibleValueRecord;

import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModelValidator.FIELD_MODEL_VALIDATOR;
import static ch.rodano.core.model.jooq.tables.FieldModelWorkflow.FIELD_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;

@Repository
public class FieldModelDAOServiceImpl implements FieldModelDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public FieldModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModels", key = "#projectId")
	public List<FieldModelDTO> getFieldModels(final UUID projectId) {
		final var fieldModels = dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(FIELD_MODEL.CODE)
			.fetch();

		return fieldModels.stream()
			.map(record -> mapToDTO(record, projectId))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModel", key = "#projectId + '-' + #fieldModelId")
	public FieldModelDTO getFieldModel(final UUID projectId, final UUID fieldModelId) {
		final var record = dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, projectId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "fieldModels", "fieldModel", "fieldModelsByDatasetModel" }, allEntries = true)
	public FieldModelDTO createFieldModel(final UUID projectId, final FieldModelDTO fieldModel) {
		final var fieldModelId = fieldModel.getFieldModelId() != null
			? fieldModel.getFieldModelId()
			: UUID.randomUUID();

		dslContext.insertInto(FIELD_MODEL)
			.set(FIELD_MODEL.FIELD_MODEL_ID, fieldModelId)
			.set(FIELD_MODEL.PROJECT_ID, projectId)
			.set(FIELD_MODEL.CODE, fieldModel.getId())
			.set(FIELD_MODEL.DATASET_MODEL_ID, fieldModel.getDatasetModelId())
			.set(FIELD_MODEL.TYPE, fieldModel.getType().name())
			.set(FIELD_MODEL.DATA_TYPE, fieldModel.getDataType().name())
			.set(FIELD_MODEL.SHORTNAME, jsonMapperService.toJson(fieldModel.getShortname()))
			.set(FIELD_MODEL.LONGNAME, jsonMapperService.toJson(fieldModel.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, jsonMapperService.toJson(fieldModel.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, jsonMapperService.toJson(fieldModel.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, jsonMapperService.toJson(fieldModel.getAdvancedHelp()))
			.set(FIELD_MODEL.PLUGIN, fieldModel.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, fieldModel.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, fieldModel.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, fieldModel.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, fieldModel.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, fieldModel.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, fieldModel.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, fieldModel.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, fieldModel.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, fieldModel.getMinValue() != null ? BigDecimal.valueOf(fieldModel.getMinValue()) : null)
			.set(FIELD_MODEL.MAX_VALUE, fieldModel.getMaxValue() != null ? BigDecimal.valueOf(fieldModel.getMaxValue()) : null)
			.set(FIELD_MODEL.MIN_YEAR, fieldModel.getMinYear())
			.set(FIELD_MODEL.DICTIONARY, fieldModel.getDictionary())
			.set(FIELD_MODEL.MATCHER, fieldModel.getMatcher())
			.set(FIELD_MODEL.INLINE_HELP, fieldModel.getInlineHelp())
			.set(FIELD_MODEL.WITH_YEARS, fieldModel.isWithYears())
			.set(FIELD_MODEL.WITH_MONTHS, fieldModel.isWithMonths())
			.set(FIELD_MODEL.WITH_DAYS, fieldModel.isWithDays())
			.set(FIELD_MODEL.WITH_HOURS, fieldModel.isWithHours())
			.set(FIELD_MODEL.WITH_MINUTES, fieldModel.isWithMinutes())
			.set(FIELD_MODEL.WITH_SECONDS, fieldModel.isWithSeconds())
			.set(FIELD_MODEL.YEARS_MANDATORY, fieldModel.isYearsMandatory())
			.set(FIELD_MODEL.MONTHS_MANDATORY, fieldModel.isMonthsMandatory())
			.set(FIELD_MODEL.DAYS_MANDATORY, fieldModel.isDaysMandatory())
			.set(FIELD_MODEL.HOURS_MANDATORY, fieldModel.isHoursMandatory())
			.set(FIELD_MODEL.MINUTES_MANDATORY, fieldModel.isMinutesMandatory())
			.set(FIELD_MODEL.SECONDS_MANDATORY, fieldModel.isSecondsMandatory())
			.set(FIELD_MODEL.VALUE_FORMULA, fieldModel.getValueFormula())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER, fieldModel.getPossibleValuesProvider())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER_DESC, fieldModel.getPossibleValuesProviderDescription())
			.execute();

		insertValidators(projectId, fieldModelId, fieldModel.getValidatorIds());
		insertWorkflows(projectId, fieldModelId, fieldModel.getWorkflowIds());
		insertPossibleValues(projectId, fieldModelId, fieldModel.getPossibleValues());

		return getFieldModel(projectId, fieldModelId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "fieldModels", "fieldModel", "fieldModelsByDatasetModel" }, allEntries = true)
	public FieldModelDTO updateFieldModel(final UUID projectId, final UUID fieldModelId, final FieldModelDTO fieldModel) {
		dslContext.update(FIELD_MODEL)
			.set(FIELD_MODEL.CODE, fieldModel.getId())
			.set(FIELD_MODEL.DATASET_MODEL_ID, fieldModel.getDatasetModelId())
			.set(FIELD_MODEL.TYPE, fieldModel.getType().name())
			.set(FIELD_MODEL.DATA_TYPE, fieldModel.getDataType().name())
			.set(FIELD_MODEL.SHORTNAME, jsonMapperService.toJson(fieldModel.getShortname()))
			.set(FIELD_MODEL.LONGNAME, jsonMapperService.toJson(fieldModel.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, jsonMapperService.toJson(fieldModel.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, jsonMapperService.toJson(fieldModel.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, jsonMapperService.toJson(fieldModel.getAdvancedHelp()))
			.set(FIELD_MODEL.PLUGIN, fieldModel.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, fieldModel.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, fieldModel.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, fieldModel.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, fieldModel.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, fieldModel.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, fieldModel.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, fieldModel.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, fieldModel.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, fieldModel.getMinValue() != null ? BigDecimal.valueOf(fieldModel.getMinValue()) : null)
			.set(FIELD_MODEL.MAX_VALUE, fieldModel.getMaxValue() != null ? BigDecimal.valueOf(fieldModel.getMaxValue()) : null)
			.set(FIELD_MODEL.MIN_YEAR, fieldModel.getMinYear())
			.set(FIELD_MODEL.DICTIONARY, fieldModel.getDictionary())
			.set(FIELD_MODEL.MATCHER, fieldModel.getMatcher())
			.set(FIELD_MODEL.INLINE_HELP, fieldModel.getInlineHelp())
			.set(FIELD_MODEL.WITH_YEARS, fieldModel.isWithYears())
			.set(FIELD_MODEL.WITH_MONTHS, fieldModel.isWithMonths())
			.set(FIELD_MODEL.WITH_DAYS, fieldModel.isWithDays())
			.set(FIELD_MODEL.WITH_HOURS, fieldModel.isWithHours())
			.set(FIELD_MODEL.WITH_MINUTES, fieldModel.isWithMinutes())
			.set(FIELD_MODEL.WITH_SECONDS, fieldModel.isWithSeconds())
			.set(FIELD_MODEL.YEARS_MANDATORY, fieldModel.isYearsMandatory())
			.set(FIELD_MODEL.MONTHS_MANDATORY, fieldModel.isMonthsMandatory())
			.set(FIELD_MODEL.DAYS_MANDATORY, fieldModel.isDaysMandatory())
			.set(FIELD_MODEL.HOURS_MANDATORY, fieldModel.isHoursMandatory())
			.set(FIELD_MODEL.MINUTES_MANDATORY, fieldModel.isMinutesMandatory())
			.set(FIELD_MODEL.SECONDS_MANDATORY, fieldModel.isSecondsMandatory())
			.set(FIELD_MODEL.VALUE_FORMULA, fieldModel.getValueFormula())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER, fieldModel.getPossibleValuesProvider())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER_DESC, fieldModel.getPossibleValuesProviderDescription())
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();

		deleteRelationships(projectId, fieldModelId);

		insertValidators(projectId, fieldModelId, fieldModel.getValidatorIds());
		insertWorkflows(projectId, fieldModelId, fieldModel.getWorkflowIds());
		insertPossibleValues(projectId, fieldModelId, fieldModel.getPossibleValues());

		return getFieldModel(projectId, fieldModelId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "fieldModels", "fieldModel", "fieldModelsByDatasetModel" }, allEntries = true)
	public void deleteFieldModel(final UUID projectId, final UUID fieldModelId) {
		deleteRelationships(projectId, fieldModelId);

		dslContext.deleteFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModelsByDatasetModel", key = "#projectId + '-' + #datasetModelId")
	public List<FieldModelDTO> getFieldModelsByDatasetModel(final UUID projectId, final UUID datasetModelId) {
		final var fieldModels = dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.orderBy(FIELD_MODEL.CODE)
			.fetch();

		return fieldModels.stream()
			.map(record -> mapToDTO(record, projectId))
			.collect(Collectors.toList());
	}

	private void deleteRelationships(final UUID projectId, final UUID fieldModelId) {
		dslContext.deleteFrom(FIELD_MODEL_VALIDATOR)
			.where(FIELD_MODEL_VALIDATOR.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();

		dslContext.deleteFrom(FIELD_MODEL_WORKFLOW)
			.where(FIELD_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();

		dslContext.deleteFrom(FIELD_POSSIBLE_VALUE)
			.where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId))
			.and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();
	}

	private void insertValidators(final UUID projectId, final UUID fieldModelId, final List<UUID> validatorIds) {
		if(validatorIds == null || validatorIds.isEmpty()) {
			return;
		}

		for(final var validatorId : validatorIds) {
			dslContext.insertInto(FIELD_MODEL_VALIDATOR)
				.set(FIELD_MODEL_VALIDATOR.PROJECT_ID, projectId)
				.set(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID, fieldModelId)
				.set(FIELD_MODEL_VALIDATOR.VALIDATOR_ID, validatorId)
				.execute();
		}
	}

	private void insertWorkflows(final UUID projectId, final UUID fieldModelId, final List<UUID> workflowIds) {
		if(workflowIds == null || workflowIds.isEmpty()) {
			return;
		}

		for(final var workflowId : workflowIds) {
			dslContext.insertInto(FIELD_MODEL_WORKFLOW)
				.set(FIELD_MODEL_WORKFLOW.PROJECT_ID, projectId)
				.set(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID, fieldModelId)
				.set(FIELD_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
				.execute();
		}
	}

	private void insertPossibleValues(final UUID projectId, final UUID fieldModelId, final List<PossibleValueDTO> possibleValues) {
		if(possibleValues == null || possibleValues.isEmpty()) {
			return;
		}

		int sortOrder = 0;
		for(final var possibleValue : possibleValues) {
			final var possibleValueId = possibleValue.getPossibleValueId() != null
				? possibleValue.getPossibleValueId()
				: UUID.randomUUID();

			dslContext.insertInto(FIELD_POSSIBLE_VALUE)
				.set(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID, possibleValueId)
				.set(FIELD_POSSIBLE_VALUE.PROJECT_ID, projectId)
				.set(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID, fieldModelId)
				.set(FIELD_POSSIBLE_VALUE.CODE, possibleValue.getId())
				.set(FIELD_POSSIBLE_VALUE.SHORTNAME, jsonMapperService.toJson(possibleValue.getShortname()))
				.set(FIELD_POSSIBLE_VALUE.SPECIFY, possibleValue.isSpecify())
				.set(FIELD_POSSIBLE_VALUE.EXPORT_LABEL, possibleValue.getExportLabel())
				.set(FIELD_POSSIBLE_VALUE.SORT_ORDER, sortOrder++)
				.execute();
		}
	}

	private FieldModelDTO mapToDTO(final FieldModelRecord record, final UUID projectId) {
		final var dto = new FieldModelDTO();
		dto.setFieldModelId(record.getFieldModelId());
		dto.setId(record.getCode());
		dto.setType(FieldModelType.valueOf(record.getType()));
		dto.setDataType(OperandType.valueOf(record.getDataType()));
		dto.setDatasetModelId(record.getDatasetModelId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setMatcherMessage(jsonMapperService.fromJson(record.getMatcherMessage(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setAdvancedHelp(jsonMapperService.fromJson(record.getAdvancedHelp(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setPlugin(record.getPlugin());
		dto.setSearchable(record.getSearchable());
		dto.setReadOnly(record.getReadOnly());
		dto.setExportable(record.getExportable());
		dto.setAllowDateInFuture(record.getAllowDateInFuture());
		dto.setExportOrder(record.getExportOrder());
		dto.setMaxLength(record.getMaxLength());
		dto.setMaxIntegerDigits(record.getMaxIntegerDigits());
		dto.setMaxDecimalDigits(record.getMaxDecimalDigits());
		dto.setMinValue(record.getMinValue() != null ? record.getMinValue().doubleValue() : null);
		dto.setMaxValue(record.getMaxValue() != null ? record.getMaxValue().doubleValue() : null);
		dto.setMinYear(record.getMinYear());
		dto.setDictionary(record.getDictionary());
		dto.setMatcher(record.getMatcher());
		dto.setInlineHelp(record.getInlineHelp());
		dto.setWithYears(record.getWithYears());
		dto.setWithMonths(record.getWithMonths());
		dto.setWithDays(record.getWithDays());
		dto.setWithHours(record.getWithHours());
		dto.setWithMinutes(record.getWithMinutes());
		dto.setWithSeconds(record.getWithSeconds());
		dto.setYearsMandatory(record.getYearsMandatory());
		dto.setMonthsMandatory(record.getMonthsMandatory());
		dto.setDaysMandatory(record.getDaysMandatory());
		dto.setHoursMandatory(record.getHoursMandatory());
		dto.setMinutesMandatory(record.getMinutesMandatory());
		dto.setSecondsMandatory(record.getSecondsMandatory());
		dto.setValueFormula(record.getValueFormula());
		dto.setPossibleValuesProvider(record.getPossibleValuesProvider());
		dto.setPossibleValuesProviderDescription(record.getPossibleValuesProviderDesc());

		final var validatorIds = loadValidatorIds(projectId, record.getFieldModelId());
		dto.setValidatorIds(validatorIds);

		final var workflowIds = loadWorkflowIds(projectId, record.getFieldModelId());
		dto.setWorkflowIds(workflowIds);

		final var possibleValues = loadPossibleValues(projectId, record.getFieldModelId());
		dto.setPossibleValues(possibleValues);

		return dto;
	}

	private List<UUID> loadValidatorIds(final UUID projectId, final UUID fieldModelId) {
		return dslContext.select(FIELD_MODEL_VALIDATOR.VALIDATOR_ID)
			.from(FIELD_MODEL_VALIDATOR)
			.where(FIELD_MODEL_VALIDATOR.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID.eq(fieldModelId))
			.fetch(FIELD_MODEL_VALIDATOR.VALIDATOR_ID);
	}

	private List<UUID> loadWorkflowIds(final UUID projectId, final UUID fieldModelId) {
		return dslContext.select(FIELD_MODEL_WORKFLOW.WORKFLOW_ID)
			.from(FIELD_MODEL_WORKFLOW)
			.where(FIELD_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID.eq(fieldModelId))
			.fetch(FIELD_MODEL_WORKFLOW.WORKFLOW_ID);
	}

	private List<PossibleValueDTO> loadPossibleValues(final UUID projectId, final UUID fieldModelId) {
		final var possibleValueRecords = dslContext.selectFrom(FIELD_POSSIBLE_VALUE)
			.where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId))
			.and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId))
			.fetch();

		return possibleValueRecords.stream()
			.map(this::mapPossibleValueToDTO)
			.collect(Collectors.toList());
	}

	private PossibleValueDTO mapPossibleValueToDTO(final FieldPossibleValueRecord record) {
		final var dto = new PossibleValueDTO();
		dto.setPossibleValueId(record.getPossibleValueId());
		dto.setId(record.getCode());
		dto.setFieldModelId(record.getFieldModelId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setSpecify(record.getSpecify());
		dto.setExportLabel(record.getExportLabel());
		dto.setSortOrder(record.getSortOrder());

		return dto;
	}
}
