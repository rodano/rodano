package ch.rodano.core.services.dao.configurator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.FieldModelDTO;
import ch.rodano.api.config.PossibleValueDTO;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.jooq.enums.FieldModelDataType;
import ch.rodano.core.model.jooq.tables.records.FieldModelRecord;
import ch.rodano.core.model.jooq.tables.records.FieldPossibleValueRecord;

import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModelValidator.FIELD_MODEL_VALIDATOR;
import static ch.rodano.core.model.jooq.tables.FieldModelWorkflow.FIELD_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;

@Repository
public class FieldModelDAOServiceImpl implements FieldModelDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public FieldModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FieldModelDTO> getFieldModels(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getFieldModelsFull(projectId);
			case VIEW_SUMMARY -> getFieldModelsSummary(projectId);
			default -> getFieldModelsSummary(projectId);
		};
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModels", key = "#projectId.toString() + ':summary'")
	public List<FieldModelDTO> getFieldModelsSummary(final UUID projectId) {
		final var fieldModelRecords = dslContext
			.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(FIELD_MODEL.EXPORT_ORDER, FIELD_MODEL.CODE)
			.fetch();

		if(fieldModelRecords.isEmpty()) {
			return List.of();
		}

		return fieldModelRecords.map(record -> mapToDTO(
			record, Map.of(), Map.of(), Map.of())
		);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModels", key = "#projectId.toString() + ':full'")
	public List<FieldModelDTO> getFieldModelsFull(final UUID projectId) {
		final var fieldModelRecords = dslContext
			.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(FIELD_MODEL.EXPORT_ORDER, FIELD_MODEL.CODE)
			.fetch();

		if(fieldModelRecords.isEmpty()) {
			return List.of();
		}

		final var fieldModelIds = fieldModelRecords.map(FieldModelRecord::getFieldModelId);

		final var validatorMap = loadValidatorIds(projectId, fieldModelIds);
		final var workflowMap = loadWorkflowIds(projectId, fieldModelIds);
		final var possibleValueMap = loadPossibleValues(projectId, fieldModelIds);

		return fieldModelRecords.map(record -> mapToDTO(
			record, validatorMap, workflowMap, possibleValueMap
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "fieldModel", key = "#projectId.toString() + ':' + #fieldModelId.toString()")
	public FieldModelDTO getFieldModel(final UUID projectId, final UUID fieldModelId) {
		final var record = dslContext
			.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var fieldModelIds = List.of(fieldModelId);
		final var validatorMap = loadValidatorIds(projectId, fieldModelIds);
		final var workflowMap = loadWorkflowIds(projectId, fieldModelIds);
		final var possibleValueMap = loadPossibleValues(projectId, fieldModelIds);

		return mapToDTO(record, validatorMap, workflowMap, possibleValueMap);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "datasetModels", allEntries = true)
	})
	public FieldModelDTO createFieldModel(final UUID projectId, final FieldModelDTO dto) {
		final var fieldModelId = dto.getFieldModelId() != null ? dto.getFieldModelId() : UUID.randomUUID();

		dslContext.insertInto(FIELD_MODEL)
			.set(FIELD_MODEL.FIELD_MODEL_ID, fieldModelId)
			.set(FIELD_MODEL.PROJECT_ID, projectId)
			.set(FIELD_MODEL.CODE, dto.getId())
			.set(FIELD_MODEL.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(FIELD_MODEL.TYPE, ch.rodano.core.model.jooq.enums.FieldModelType.valueOf(dto.getType().name()))
			.set(FIELD_MODEL.DATA_TYPE, FieldModelDataType.valueOf(dto.getDataType().name()))
			.set(FIELD_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(FIELD_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, jsonMapperService.toJson(dto.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, jsonMapperService.toJson(dto.getAdvancedHelp()))
			.set(FIELD_MODEL.PLUGIN, dto.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, dto.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, dto.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, dto.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, dto.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, dto.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, dto.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, dto.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, dto.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, dto.getMinValue() != null ? BigDecimal.valueOf(dto.getMinValue()) : null)
			.set(FIELD_MODEL.MAX_VALUE, dto.getMaxValue() != null ? BigDecimal.valueOf(dto.getMaxValue()) : null)
			.set(FIELD_MODEL.MIN_YEAR, dto.getMinYear())
			.set(FIELD_MODEL.DICTIONARY, dto.getDictionary())
			.set(FIELD_MODEL.MATCHER, dto.getMatcher())
			.set(FIELD_MODEL.INLINE_HELP, dto.getInlineHelp())
			.set(FIELD_MODEL.WITH_YEARS, dto.isWithYears())
			.set(FIELD_MODEL.WITH_MONTHS, dto.isWithMonths())
			.set(FIELD_MODEL.WITH_DAYS, dto.isWithDays())
			.set(FIELD_MODEL.WITH_HOURS, dto.isWithHours())
			.set(FIELD_MODEL.WITH_MINUTES, dto.isWithMinutes())
			.set(FIELD_MODEL.WITH_SECONDS, dto.isWithSeconds())
			.set(FIELD_MODEL.YEARS_MANDATORY, dto.isYearsMandatory())
			.set(FIELD_MODEL.MONTHS_MANDATORY, dto.isMonthsMandatory())
			.set(FIELD_MODEL.DAYS_MANDATORY, dto.isDaysMandatory())
			.set(FIELD_MODEL.HOURS_MANDATORY, dto.isHoursMandatory())
			.set(FIELD_MODEL.MINUTES_MANDATORY, dto.isMinutesMandatory())
			.set(FIELD_MODEL.SECONDS_MANDATORY, dto.isSecondsMandatory())
			.set(FIELD_MODEL.VALUE_FORMULA, dto.getValueFormula())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER, dto.getPossibleValuesProvider())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER_DESC, dto.getPossibleValuesProviderDescription())
			.execute();

		replaceRelations(projectId, fieldModelId, dto);
		return getFieldModel(projectId, fieldModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "fieldModel", key = "#projectId.toString() + ':' + #fieldModelId.toString()"),
		@CacheEvict(value = "datasetModels", allEntries = true)
	})
	public FieldModelDTO updateFieldModel(final UUID projectId, final UUID fieldModelId, final FieldModelDTO dto) {
		dslContext.update(FIELD_MODEL)
			.set(FIELD_MODEL.CODE, dto.getId())
			.set(FIELD_MODEL.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(FIELD_MODEL.TYPE, ch.rodano.core.model.jooq.enums.FieldModelType.valueOf(dto.getType().name()))
			.set(FIELD_MODEL.DATA_TYPE, FieldModelDataType.valueOf(dto.getDataType().name()))
			.set(FIELD_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(FIELD_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, jsonMapperService.toJson(dto.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, jsonMapperService.toJson(dto.getAdvancedHelp()))
			.set(FIELD_MODEL.PLUGIN, dto.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, dto.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, dto.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, dto.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, dto.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, dto.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, dto.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, dto.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, dto.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, dto.getMinValue() != null ? BigDecimal.valueOf(dto.getMinValue()) : null)
			.set(FIELD_MODEL.MAX_VALUE, dto.getMaxValue() != null ? BigDecimal.valueOf(dto.getMaxValue()) : null)
			.set(FIELD_MODEL.MIN_YEAR, dto.getMinYear())
			.set(FIELD_MODEL.DICTIONARY, dto.getDictionary())
			.set(FIELD_MODEL.MATCHER, dto.getMatcher())
			.set(FIELD_MODEL.INLINE_HELP, dto.getInlineHelp())
			.set(FIELD_MODEL.WITH_YEARS, dto.isWithYears())
			.set(FIELD_MODEL.WITH_MONTHS, dto.isWithMonths())
			.set(FIELD_MODEL.WITH_DAYS, dto.isWithDays())
			.set(FIELD_MODEL.WITH_HOURS, dto.isWithHours())
			.set(FIELD_MODEL.WITH_MINUTES, dto.isWithMinutes())
			.set(FIELD_MODEL.WITH_SECONDS, dto.isWithSeconds())
			.set(FIELD_MODEL.YEARS_MANDATORY, dto.isYearsMandatory())
			.set(FIELD_MODEL.MONTHS_MANDATORY, dto.isMonthsMandatory())
			.set(FIELD_MODEL.DAYS_MANDATORY, dto.isDaysMandatory())
			.set(FIELD_MODEL.HOURS_MANDATORY, dto.isHoursMandatory())
			.set(FIELD_MODEL.MINUTES_MANDATORY, dto.isMinutesMandatory())
			.set(FIELD_MODEL.SECONDS_MANDATORY, dto.isSecondsMandatory())
			.set(FIELD_MODEL.VALUE_FORMULA, dto.getValueFormula())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER, dto.getPossibleValuesProvider())
			.set(FIELD_MODEL.POSSIBLE_VALUES_PROVIDER_DESC, dto.getPossibleValuesProviderDescription())
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();

		replaceRelations(projectId, fieldModelId, dto);
		return getFieldModel(projectId, fieldModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "fieldModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "fieldModel", key = "#projectId.toString() + ':' + #fieldModelId.toString()"),
		@CacheEvict(value = "datasetModels", allEntries = true)
	})
	public void deleteFieldModel(final UUID projectId, final UUID fieldModelId) {
		dslContext.deleteFrom(FIELD_POSSIBLE_VALUE).where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId)).and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId)).execute();
		dslContext.deleteFrom(FIELD_MODEL_VALIDATOR).where(FIELD_MODEL_VALIDATOR.PROJECT_ID.eq(projectId)).and(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID.eq(fieldModelId)).execute();
		dslContext.deleteFrom(FIELD_MODEL_WORKFLOW).where(FIELD_MODEL_WORKFLOW.PROJECT_ID.eq(projectId)).and(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID.eq(fieldModelId)).execute();

		dslContext.deleteFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.execute();
	}

	private Map<UUID, List<PossibleValueDTO>> loadPossibleValues(final UUID projectId, final List<UUID> fieldModelIds) {
		if(fieldModelIds.isEmpty()) {
			return Map.of();
		}

		final var records = dslContext.selectFrom(FIELD_POSSIBLE_VALUE)
			.where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId))
			.and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.in(fieldModelIds))
			.orderBy(FIELD_POSSIBLE_VALUE.CODE)
			.fetch();

		return records.stream()
			.collect(Collectors.groupingBy(
				FieldPossibleValueRecord::getFieldModelId,
				Collectors.mapping(this::mapPossibleValueToDTO, Collectors.toList())
			));
	}

	private Map<UUID, List<UUID>> loadValidatorIds(final UUID projectId, final List<UUID> fieldModelIds) {
		return fetchGroupedIds(
			projectId,
			fieldModelIds,
			FIELD_MODEL_VALIDATOR,
			FIELD_MODEL_VALIDATOR.PROJECT_ID,
			FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID,
			FIELD_MODEL_VALIDATOR.VALIDATOR_ID
		);
	}

	private Map<UUID, List<UUID>> loadWorkflowIds(final UUID projectId, final List<UUID> fieldModelIds) {
		return fetchGroupedIds(
			projectId,
			fieldModelIds,
			FIELD_MODEL_WORKFLOW,
			FIELD_MODEL_WORKFLOW.PROJECT_ID,
			FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID,
			FIELD_MODEL_WORKFLOW.WORKFLOW_ID
		);
	}

	private <R extends Record, T extends Table<R>> Map<UUID, List<UUID>> fetchGroupedIds(
		final UUID projectId,
		final List<UUID> fieldModelIds,
		final T table,
		final TableField<R, UUID> projectIdField,
		final TableField<R, UUID> fieldModelIdField,
		final TableField<R, UUID> valueField
	) {
		if(fieldModelIds == null || fieldModelIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(fieldModelIdField, valueField)
			.from(table)
			.where(projectIdField.eq(projectId))
			.and(fieldModelIdField.in(fieldModelIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}

		result.replaceAll((_, v) -> {
			final var copy = new ArrayList<>(v);
			Collections.sort(copy);
			return copy;
		});

		return result;
	}

	private FieldModelDTO mapToDTO(
		final FieldModelRecord record,
		final Map<UUID, List<UUID>> validatorMap,
		final Map<UUID, List<UUID>> workflowMap,
		final Map<UUID, List<PossibleValueDTO>> possibleValueMap
	) {
		final var dto = new FieldModelDTO();
		dto.setFieldModelId(record.getFieldModelId());
		dto.setId(record.getCode());
		dto.setType(FieldModelType.valueOf(record.getType().name()));
		dto.setDataType(OperandType.valueOf(record.getDataType().name()));
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

		final var fieldModelId = record.getFieldModelId();
		dto.setValidatorIds(validatorMap.getOrDefault(fieldModelId, List.of()));
		dto.setWorkflowIds(workflowMap.getOrDefault(fieldModelId, List.of()));
		dto.setPossibleValues(possibleValueMap.getOrDefault(fieldModelId, List.of()));

		return dto;
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

	private void replaceRelations(final UUID projectId, final UUID fieldModelId, final FieldModelDTO dto) {
		dslContext.deleteFrom(FIELD_POSSIBLE_VALUE).where(FIELD_POSSIBLE_VALUE.PROJECT_ID.eq(projectId)).and(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId)).execute();
		dslContext.deleteFrom(FIELD_MODEL_VALIDATOR).where(FIELD_MODEL_VALIDATOR.PROJECT_ID.eq(projectId)).and(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID.eq(fieldModelId)).execute();
		dslContext.deleteFrom(FIELD_MODEL_WORKFLOW).where(FIELD_MODEL_WORKFLOW.PROJECT_ID.eq(projectId)).and(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID.eq(fieldModelId)).execute();

		insertPossibleValues(projectId, fieldModelId, dto.getPossibleValues());
		batchInsert(projectId, fieldModelId, dto);
	}

	private void insertPossibleValues(final UUID projectId, final UUID fieldModelId, final List<PossibleValueDTO> values) {
		if(values == null || values.isEmpty()) {
			return;
		}

		final List<Query> queries = new ArrayList<>();
		for(final var val : values) {
			queries.add(dslContext.insertInto(FIELD_POSSIBLE_VALUE)
				.set(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID, val.getPossibleValueId() != null ? val.getPossibleValueId() : UUID.randomUUID())
				.set(FIELD_POSSIBLE_VALUE.PROJECT_ID, projectId)
				.set(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID, fieldModelId)
				.set(FIELD_POSSIBLE_VALUE.CODE, val.getId())
				.set(FIELD_POSSIBLE_VALUE.SHORTNAME, jsonMapperService.toJson(val.getShortname()))
				.set(FIELD_POSSIBLE_VALUE.SPECIFY, val.isSpecify())
				.set(FIELD_POSSIBLE_VALUE.EXPORT_LABEL, val.getExportLabel()));
		}
		dslContext.batch(queries).execute();
	}

	private void batchInsert(final UUID projectId, final UUID fieldModelId, final FieldModelDTO dto) {
		if(dto.getWorkflowIds() != null) {
			for(final var workflowId : dto.getWorkflowIds()) {
				dslContext.insertInto(FIELD_MODEL_WORKFLOW)
					.set(FIELD_MODEL_WORKFLOW.PROJECT_ID, projectId)
					.set(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID, fieldModelId)
					.set(FIELD_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
					.execute();
			}
		}

		if(dto.getValidatorIds() != null) {
			for(final var validatorId : dto.getValidatorIds()) {
				dslContext.insertInto(FIELD_MODEL_VALIDATOR)
					.set(FIELD_MODEL_VALIDATOR.PROJECT_ID, projectId)
					.set(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID, fieldModelId)
					.set(FIELD_MODEL_VALIDATOR.VALIDATOR_ID, validatorId)
					.execute();
			}
		}
	}
}
