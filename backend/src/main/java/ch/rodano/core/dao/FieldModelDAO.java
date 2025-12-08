package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.FieldModelRecord;

import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModelValidator.FIELD_MODEL_VALIDATOR;
import static ch.rodano.core.model.jooq.tables.FieldModelWorkflow.FIELD_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

@Repository
public class FieldModelDAO implements BaseProjectDAO<FieldModel> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final PossibleValueDAO possibleValueDAO;
	private final RuleDAO ruleDAO;

	public FieldModelDAO(final DSLContext dslContext,
						 final MappingHelper mappingHelper,
						 final PossibleValueDAO possibleValueDAO,
						 final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.possibleValueDAO = possibleValueDAO;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<FieldModel> findByProject(final UUID projectId) {
		return dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(FIELD_MODEL.EXPORT_ORDER)
			.fetch(this::mapToModel);
	}

	@Override
	public FieldModel findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.PROJECT_ID.eq(projectId))
			.and(FIELD_MODEL.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public FieldModel findById(final UUID id) {
		return dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.FIELD_MODEL_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public FieldModel save(final FieldModel entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {
	}

	public List<FieldModel> findByDatasetModel(final UUID datasetModelId) {
		return dslContext.selectFrom(FIELD_MODEL)
			.where(FIELD_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.orderBy(FIELD_MODEL.EXPORT_ORDER)
			.fetch(this::mapToModel);
	}

	private FieldModel mapToModel(final FieldModelRecord record) {
		if(record == null) {
			return null;
		}

		final FieldModel model = new FieldModel();

		model.setFieldModelId(record.getFieldModelId());
		model.setId(record.getCode());

		final FieldModelType type = mappingHelper.parseEnum(FieldModelType.class, record.getType(), record.getCode());
		model.setType(type != null ? type : FieldModelType.STRING);

		model.setDataType(mappingHelper.parseEnum(OperandType.class, record.getDataType(), record.getCode()));

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setAdvancedHelp(mappingHelper.parseJsonToMap(record.getAdvancedHelp()));
		model.setMatcherMessage(mappingHelper.parseJsonToMap(record.getMatcherMessage()));

		model.setPlugin(record.getPlugin() != null ? record.getPlugin() : false);
		model.setSearchable(record.getSearchable() != null ? record.getSearchable() : false);
		model.setReadOnly(record.getReadOnly() != null ? record.getReadOnly() : false);
		model.setExportable(record.getExportable() != null ? record.getExportable() : false);
		model.setAllowDateInFuture(record.getAllowDateInFuture() != null ? record.getAllowDateInFuture() : false);

		model.setExportOrder(record.getExportOrder() != null ? record.getExportOrder() : 0);
		model.setMaxLength(record.getMaxLength());
		model.setMaxIntegerDigits(record.getMaxIntegerDigits() != null ? record.getMaxIntegerDigits() : 0);
		model.setMaxDecimalDigits(record.getMaxDecimalDigits() != null ? record.getMaxDecimalDigits() : 0);
		model.setMinYear(record.getMinYear());

		model.setMinValue(record.getMinValue() != null ? record.getMinValue().doubleValue() : null);
		model.setMaxValue(record.getMaxValue() != null ? record.getMaxValue().doubleValue() : null);

		model.setDictionary(record.getDictionary());
		model.setMatcher(record.getMatcher());
		model.setInlineHelp(record.getInlineHelp());
		model.setValueFormula(record.getValueFormula());
		model.setPossibleValuesProvider(record.getPossibleValuesProvider());
		model.setPossibleValuesProviderDescription(record.getPossibleValuesProviderDesc());

		model.setWithYears(record.getWithYears() != null ? record.getWithYears() : false);
		model.setWithMonths(record.getWithMonths() != null ? record.getWithMonths() : false);
		model.setWithDays(record.getWithDays() != null ? record.getWithDays() : false);
		model.setWithHours(record.getWithHours() != null ? record.getWithHours() : false);
		model.setWithMinutes(record.getWithMinutes() != null ? record.getWithMinutes() : false);
		model.setWithSeconds(record.getWithSeconds() != null ? record.getWithSeconds() : false);

		model.setYearsMandatory(record.getYearsMandatory() != null ? record.getYearsMandatory() : false);
		model.setMonthsMandatory(record.getMonthsMandatory() != null ? record.getMonthsMandatory() : false);
		model.setDaysMandatory(record.getDaysMandatory() != null ? record.getDaysMandatory() : false);
		model.setHoursMandatory(record.getHoursMandatory() != null ? record.getHoursMandatory() : false);
		model.setMinutesMandatory(record.getMinutesMandatory() != null ? record.getMinutesMandatory() : false);
		model.setSecondsMandatory(record.getSecondsMandatory() != null ? record.getSecondsMandatory() : false);

		if(record.getType() != null && isChoiceType(record.getType())) {
			model.setPossibleValues(possibleValueDAO.findByFieldModel(record.getFieldModelId()));
		}

		model.setValidatorIds(loadValidatorIds(record.getFieldModelId()));
		model.setWorkflowIds(loadWorkflowIds(record.getFieldModelId()));

		model.setRules(ruleDAO.findByEntity(RuleEntityType.FIELD_MODEL, record.getFieldModelId()));

		return model;
	}

	private boolean isChoiceType(final String type) {
		return "SELECT".equals(type) ||
			"RADIO".equals(type) ||
			"CHECKBOX_GROUP".equals(type) ||
			"MULTISELECT".equals(type);
	}

	private List<String> loadValidatorIds(final UUID fieldModelId) {
		return dslContext.select(VALIDATOR.CODE)
			.from(FIELD_MODEL_VALIDATOR)
			.join(VALIDATOR).on(VALIDATOR.VALIDATOR_ID.eq(FIELD_MODEL_VALIDATOR.VALIDATOR_ID))
			.where(FIELD_MODEL_VALIDATOR.FIELD_MODEL_ID.eq(fieldModelId))
			.fetch(VALIDATOR.CODE);
	}

	private List<String> loadWorkflowIds(final UUID fieldModelId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(FIELD_MODEL_WORKFLOW)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(FIELD_MODEL_WORKFLOW.WORKFLOW_ID))
			.where(FIELD_MODEL_WORKFLOW.FIELD_MODEL_ID.eq(fieldModelId))
			.fetch(WORKFLOW.CODE);
	}
}
