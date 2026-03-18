package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.DatasetModel;
import ch.rodano.batch.pojo.FieldModel;
import ch.rodano.batch.pojo.PossibleValue;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class DatasetModelWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetModelWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<DatasetModel> wrapped = (ProjectScoped<DatasetModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final DatasetModel datasetModel = wrapped.getPayload();

				final String code = datasetModel.getId();
				if(code == null || code.isBlank()) {
					LOGGER.warn("Skipping dataset model with null code");
					continue;
				}

				final var existing = resolveDatasetModelId(tx, projectId, datasetModel.getId());
				final UUID datasetModelId = existing != null
					? existing
					: deterministic(projectId, "DATASET_MODEL", code);

				tx.insertInto(DATASET_MODEL)
					.set(DATASET_MODEL.DATASET_MODEL_ID, datasetModelId)
					.set(DATASET_MODEL.PROJECT_ID, projectId)
					.set(DATASET_MODEL.CODE, code)
					.set(DATASET_MODEL.MULTIPLE, datasetModel.isMultiple())
					.set(DATASET_MODEL.MASTER, datasetModel.isMaster())
					.set(DATASET_MODEL.EXPORTABLE, datasetModel.isExportable())
					.set(DATASET_MODEL.EXPORT_ORDER, datasetModel.getExportOrder())
					.set(DATASET_MODEL.COLLAPSED_LABEL_PATTERN, datasetModel.getCollapsedLabelPattern())
					.set(DATASET_MODEL.EXPANDED_LABEL_PATTERN, datasetModel.getExpandedLabelPattern())
					.set(DATASET_MODEL.SHORTNAME, toJson(datasetModel.getShortname()))
					.set(DATASET_MODEL.LONGNAME, toJson(datasetModel.getLongname()))
					.set(DATASET_MODEL.DESCRIPTION, toJson(datasetModel.getDescription()))
					.onDuplicateKeyUpdate()
					.set(DATASET_MODEL.MULTIPLE, datasetModel.isMultiple())
					.set(DATASET_MODEL.MASTER, datasetModel.isMaster())
					.set(DATASET_MODEL.EXPORTABLE, datasetModel.isExportable())
					.set(DATASET_MODEL.EXPORT_ORDER, datasetModel.getExportOrder())
					.set(DATASET_MODEL.COLLAPSED_LABEL_PATTERN, datasetModel.getCollapsedLabelPattern())
					.set(DATASET_MODEL.EXPANDED_LABEL_PATTERN, datasetModel.getExpandedLabelPattern())
					.set(DATASET_MODEL.SHORTNAME, toJson(datasetModel.getShortname()))
					.set(DATASET_MODEL.LONGNAME, toJson(datasetModel.getLongname()))
					.set(DATASET_MODEL.DESCRIPTION, toJson(datasetModel.getDescription()))
					.execute();

				putDatasetRules(tx, projectId, datasetModelId, code, datasetModel.getDeleteRules(), "DELETE", "DATASET_DELETE");
				putDatasetRules(tx, projectId, datasetModelId, code, datasetModel.getRestoreRules(), "RESTORE", "DATASET_RESTORE");

				if(datasetModel.getFieldModels() != null && !datasetModel.getFieldModels().isEmpty()) {
					for(FieldModel fieldModel : datasetModel.getFieldModels()) {
						writeFieldModels(tx, projectId, datasetModel, datasetModelId, fieldModel);
					}
				}
			}
		});
	}

	private void putDatasetRules(final DSLContext tx,
								 final UUID projectId,
								 final UUID datasetModelId,
								 final String datasetCode,
								 final List<Rule> rules,
								 final String ruleType,
								 final String saltPrefix) {

		if(rules == null || rules.isEmpty()) {
			return;
		}

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);

			final UUID ruleId = deterministic(projectId, saltPrefix, datasetCode + "|" + idx);

			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, RuleEntityType.DATASET_MODEL)
				.set(RULE.ENTITY_ID, datasetModelId)
				.set(RULE.RULE_TYPE, ruleType)
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.onDuplicateKeyUpdate()
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.execute();

			if(rule.getConstraint() != null) {
				insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint(), RuleConstraintConstraintType.RULE);
			}
			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}

	private void writeFieldModels(final DSLContext tx,
								  final UUID projectId,
								  final DatasetModel datasetModel,
								  final UUID datasetModelId,
								  final FieldModel fieldModel) {

		final String datasetCode = datasetModel.getId();
		final String fieldCode = fieldModel.getId();

		if(fieldCode == null || fieldCode.isBlank()) {
			LOGGER.warn("Skipping field with blank code in dataset {}", datasetCode);
			return;
		}
		final var existing = resolveFieldModelId(tx, projectId, datasetModelId, fieldCode);
		final UUID fieldModelId = existing != null
			? existing
			: deterministic(projectId, "FIELD_MODEL", datasetCode + "|" + fieldCode);

		tx.insertInto(FIELD_MODEL)
			.set(FIELD_MODEL.FIELD_MODEL_ID, fieldModelId)
			.set(FIELD_MODEL.PROJECT_ID, projectId)
			.set(FIELD_MODEL.DATASET_MODEL_ID, datasetModelId)
			.set(FIELD_MODEL.CODE, fieldCode)
			.set(FIELD_MODEL.TYPE, fieldModel.getType())
			.set(FIELD_MODEL.DATA_TYPE, fieldModel.getDataType())
			.set(FIELD_MODEL.PLUGIN, fieldModel.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, fieldModel.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, fieldModel.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, fieldModel.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, fieldModel.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, fieldModel.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, fieldModel.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, fieldModel.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, fieldModel.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, fieldModel.getMinValue())
			.set(FIELD_MODEL.MAX_VALUE, fieldModel.getMaxValue())
			.set(FIELD_MODEL.MIN_YEAR, fieldModel.getMinYear())
			.set(FIELD_MODEL.MAX_YEAR, fieldModel.getMaxYear())
			.set(FIELD_MODEL.DICTIONARY, fieldModel.getDictionary())
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
			.set(FIELD_MODEL.MATCHER, fieldModel.getMatcher())
			.set(FIELD_MODEL.SHORTNAME, toJson(fieldModel.getShortname()))
			.set(FIELD_MODEL.LONGNAME, toJson(fieldModel.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, toJson(fieldModel.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, toJson(fieldModel.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, toJson(fieldModel.getAdvancedHelp()))
			.onDuplicateKeyUpdate()
			.set(FIELD_MODEL.PLUGIN, fieldModel.isPlugin())
			.set(FIELD_MODEL.SEARCHABLE, fieldModel.isSearchable())
			.set(FIELD_MODEL.READ_ONLY, fieldModel.isReadOnly())
			.set(FIELD_MODEL.EXPORTABLE, fieldModel.isExportable())
			.set(FIELD_MODEL.ALLOW_DATE_IN_FUTURE, fieldModel.isAllowDateInFuture())
			.set(FIELD_MODEL.EXPORT_ORDER, fieldModel.getExportOrder())
			.set(FIELD_MODEL.MAX_LENGTH, fieldModel.getMaxLength())
			.set(FIELD_MODEL.MAX_INTEGER_DIGITS, fieldModel.getMaxIntegerDigits())
			.set(FIELD_MODEL.MAX_DECIMAL_DIGITS, fieldModel.getMaxDecimalDigits())
			.set(FIELD_MODEL.MIN_VALUE, fieldModel.getMinValue())
			.set(FIELD_MODEL.MAX_VALUE, fieldModel.getMaxValue())
			.set(FIELD_MODEL.MIN_YEAR, fieldModel.getMinYear())
			.set(FIELD_MODEL.MAX_YEAR, fieldModel.getMaxYear())
			.set(FIELD_MODEL.DICTIONARY, fieldModel.getDictionary())
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
			.set(FIELD_MODEL.MATCHER, fieldModel.getMatcher())
			.set(FIELD_MODEL.SHORTNAME, toJson(fieldModel.getShortname()))
			.set(FIELD_MODEL.LONGNAME, toJson(fieldModel.getLongname()))
			.set(FIELD_MODEL.DESCRIPTION, toJson(fieldModel.getDescription()))
			.set(FIELD_MODEL.MATCHER_MESSAGE, toJson(fieldModel.getMatcherMessage()))
			.set(FIELD_MODEL.ADVANCED_HELP, toJson(fieldModel.getAdvancedHelp()))
			.execute();

		upsertPossibleValues(tx, projectId, fieldModelId, fieldModel.getPossibleValues());

		if(fieldModel.getConstraint() != null) {
			insertConstraintForOwner(tx, projectId, "FIELD_MODEL", fieldModelId, fieldModel.getConstraint(), RuleConstraintConstraintType.VISIBILITY);
		}

		if(fieldModel.getValueConstraint() != null) {
			insertConstraintForOwner(tx, projectId, "FIELD_MODEL", fieldModelId, fieldModel.getValueConstraint(), RuleConstraintConstraintType.VALUE_FORMULA);
		}

		putFieldRules(tx, projectId, datasetModel, fieldModel, fieldModelId);
	}

	private void upsertPossibleValues(final DSLContext tx,
									  final UUID projectId,
									  final UUID fieldModelId,
									  final List<PossibleValue> values) {

		if(values == null || values.isEmpty()) {
			return;
		}
		for(int idx = 0; idx < values.size(); idx++) {
			final PossibleValue pv = values.get(idx);
			final UUID possibleValueId = deterministic(projectId, "FIELD_POSSIBLE_VALUE", fieldModelId + "|" + pv.getId());
			tx.insertInto(FIELD_POSSIBLE_VALUE)
				.set(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID, possibleValueId)
				.set(FIELD_POSSIBLE_VALUE.PROJECT_ID, projectId)
				.set(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID, fieldModelId)
				.set(FIELD_POSSIBLE_VALUE.CODE, pv.getId())
				.set(FIELD_POSSIBLE_VALUE.SPECIFY, pv.getSpecify())
				.set(FIELD_POSSIBLE_VALUE.EXPORT_LABEL, pv.getExportLabel())
				.set(FIELD_POSSIBLE_VALUE.SORT_ORDER, idx)
				.set(FIELD_POSSIBLE_VALUE.SHORTNAME, toJson(pv.getShortname()))
				.onDuplicateKeyUpdate()
				.set(FIELD_POSSIBLE_VALUE.SPECIFY, pv.getSpecify())
				.set(FIELD_POSSIBLE_VALUE.EXPORT_LABEL, pv.getExportLabel())
				.set(FIELD_POSSIBLE_VALUE.SORT_ORDER, idx)
				.set(FIELD_POSSIBLE_VALUE.SHORTNAME, toJson(pv.getShortname()))
				.execute();
		}
	}

	private void putFieldRules(final DSLContext tx,
							   final UUID projectId,
							   final DatasetModel datasetModel,
							   final FieldModel fieldModel,
							   final UUID fieldModelId) {

		final List<Rule> rules = fieldModel.getRules();
		if(rules == null || rules.isEmpty()) {
			return;
		}
		final String datasetCode = datasetModel.getId();
		final String fieldCode = fieldModel.getId();

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, "FIELD_MODEL_RULE", datasetCode + "|" + fieldCode + "|" + idx);
			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, RuleEntityType.FIELD_MODEL)
				.set(RULE.ENTITY_ID, fieldModelId)
				.set(RULE.RULE_TYPE, DSL.val((String) null))
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.onDuplicateKeyUpdate()
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, toJson(rule.getTags()))
				.execute();

			if(rule.getConstraint() != null) {
				insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint(), RuleConstraintConstraintType.RULE);
			}

			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}
}
