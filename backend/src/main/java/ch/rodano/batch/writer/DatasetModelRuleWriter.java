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
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class DatasetModelRuleWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetModelRuleWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<DatasetModel> wrapped = (ProjectScoped<DatasetModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final DatasetModel datasetModel = wrapped.getPayload();

				final String datasetCode = datasetModel.getId();
				if(datasetCode == null || datasetCode.isBlank()) {
					continue;
				}

				final UUID datasetModelId = resolveDatasetModelId(tx, projectId, datasetCode);
				if(datasetModelId == null) {
					LOGGER.warn("Dataset model not found: {}", datasetCode);
					continue;
				}

				putRules(tx, projectId, datasetModelId, datasetCode,
					datasetModel.getDeleteRules(), "DELETE", "DATASET_DELETE", RuleEntityType.DATASET_MODEL);
				putRules(tx, projectId, datasetModelId, datasetCode,
					datasetModel.getRestoreRules(), "RESTORE", "DATASET_RESTORE", RuleEntityType.DATASET_MODEL);

				if(datasetModel.getFieldModels() != null) {
					for(FieldModel fieldModel : datasetModel.getFieldModels()) {
						final String fieldCode = fieldModel.getId();
						if(fieldCode == null || fieldCode.isBlank()) {
							continue;
						}

						final UUID fieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, fieldCode);
						if(fieldModelId == null) {
							LOGGER.warn("Field model not found: {}|{}", datasetCode, fieldCode);
							continue;
						}

						if(fieldModel.getConstraint() != null) {
							insertConstraintForOwner(tx, projectId, "FIELD_MODEL", fieldModelId,
								fieldModel.getConstraint(), RuleConstraintConstraintType.VISIBILITY);
						}

						if(fieldModel.getValueConstraint() != null) {
							insertConstraintForOwner(tx, projectId, "FIELD_MODEL", fieldModelId,
								fieldModel.getValueConstraint(), RuleConstraintConstraintType.VALUE_FORMULA);
						}

						if(fieldModel.getRules() != null && !fieldModel.getRules().isEmpty()) {
							for(int idx = 0; idx < fieldModel.getRules().size(); idx++) {
								final Rule rule = fieldModel.getRules().get(idx);
								final UUID ruleId = deterministic(projectId, "FIELD_MODEL_RULE",
									datasetCode + "|" + fieldCode + "|" + idx);

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
									insertConstraintForOwner(tx, projectId, "RULE", ruleId,
										rule.getConstraint(), RuleConstraintConstraintType.RULE);
								}
								if(rule.getActions() != null && !rule.getActions().isEmpty()) {
									insertRuleActions(tx, projectId, ruleId, rule.getActions());
								}
							}
						}
					}
				}
			}
		});
	}

	private static void putRules(final DSLContext tx,
	                             final UUID projectId,
	                             final UUID entityId,
	                             final String entityCode,
	                             final List<Rule> rules,
	                             final String ruleType,
	                             final String saltPrefix,
	                             final RuleEntityType entityType) {
		if(rules == null || rules.isEmpty()) {
			return;
		}
		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, saltPrefix, entityCode + "|" + idx);

			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, entityType)
				.set(RULE.ENTITY_ID, entityId)
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
				insertConstraintForOwner(tx, projectId, "RULE", ruleId,
					rule.getConstraint(), RuleConstraintConstraintType.RULE);
			}
			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}
}
