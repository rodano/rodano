package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.EventModel;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.batch.pojo.ScopeModel;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class ScopeModelRuleWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScopeModelRuleWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<ScopeModel> wrapped = (ProjectScoped<ScopeModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final ScopeModel scopeModel = wrapped.getPayload();
				final String scopeModelCode = scopeModel.getId();

				final UUID scopeModelId = resolveScopeModelId(tx, projectId, scopeModelCode);
				if(scopeModelId == null) {
					LOGGER.warn("Scope model not found: {}", scopeModelCode);
					continue;
				}

				// Scope model rules
				putRules(tx, projectId, scopeModelId, scopeModelCode,
					scopeModel.getCreateRules(), "CREATE", "SCOPE_CREATE", RuleEntityType.SCOPE_MODEL);
				putRules(tx, projectId, scopeModelId, scopeModelCode,
					scopeModel.getRemoveRules(), "REMOVE", "SCOPE_REMOVE", RuleEntityType.SCOPE_MODEL);
				putRules(tx, projectId, scopeModelId, scopeModelCode,
					scopeModel.getRestoreRules(), "RESTORE", "SCOPE_RESTORE", RuleEntityType.SCOPE_MODEL);

				// Event model rules
				if(scopeModel.getEventModels() != null) {
					for(EventModel eventModel : scopeModel.getEventModels()) {
						final String eventModelCode = eventModel.getId();
						final UUID eventModelId = resolveEventModelId(tx, projectId, eventModelCode);
						if(eventModelId == null) {
							LOGGER.warn("Event model not found: {}|{}", scopeModelCode, eventModelCode);
							continue;
						}

						putRules(tx, projectId, eventModelId,
							scopeModelCode + "|" + eventModelCode,
							eventModel.getCreateRules(), "CREATE", "EVENT_CREATE", RuleEntityType.EVENT_MODEL);
						putRules(tx, projectId, eventModelId,
							scopeModelCode + "|" + eventModelCode,
							eventModel.getRemoveRules(), "REMOVE", "EVENT_REMOVE", RuleEntityType.EVENT_MODEL);
						putRules(tx, projectId, eventModelId,
							scopeModelCode + "|" + eventModelCode,
							eventModel.getRestoreRules(), "RESTORE", "EVENT_RESTORE", RuleEntityType.EVENT_MODEL);
					}
				}
			}
		});
	}

	private static void putRules(final DSLContext tx,
	                             final UUID projectId,
	                             final UUID entityId,
	                             final String entityKey,
	                             final List<Rule> rules,
	                             final String ruleType,
	                             final String saltPrefix,
	                             final RuleEntityType entityType) {
		if(rules == null || rules.isEmpty()) {
			return;
		}

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, saltPrefix, entityKey + "|" + idx);

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
