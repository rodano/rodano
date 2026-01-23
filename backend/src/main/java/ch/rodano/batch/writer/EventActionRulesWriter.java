package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.JsonWriter;
import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.EventAction;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class EventActionRulesWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<EventAction> wrapped = (ProjectScoped<EventAction>) raw;
				final UUID projectId = wrapped.getProjectId();
				final EventAction eventAction = wrapped.getPayload();

				if(eventAction == null || eventAction.isEmpty()) {
					continue;
				}

				eventAction.getEventActions().forEach((eventActionCode, rules) -> {
					if(eventActionCode == null || eventActionCode.isBlank() || rules == null || rules.isEmpty()) {
						return;
					}

					final UUID eventActionId = deterministic(projectId, "EVENT_ACTION", eventActionCode);

					for(int idx = 0; idx < rules.size(); idx++) {
						final Rule rule = rules.get(idx);
						final UUID ruleId = deterministic(projectId, "EVENT_ACTION_RULE", eventActionCode + "|" + idx);

						upsertRule(tx, projectId, ruleId, eventActionId, eventActionCode, rule);

						if(rule.getConstraint() != null) {
							insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint(), RuleConstraintConstraintType.RULE);
						}

						if(rule.getActions() != null && !rule.getActions().isEmpty()) {
							insertRuleActions(tx, projectId, ruleId, rule.getActions());
						}
					}
				});
			}
		});
	}

	private static void upsertRule(final DSLContext tx,
								   final UUID projectId,
								   final UUID ruleId,
								   final UUID entityId,
								   final String ruleType,
								   final Rule rule) {

		final String description = rule.getDescription();
		final String messageJson = JsonWriter.toJson(rule.getMessage());
		final String tagJson = JsonWriter.toJson(rule.getTags());
		tx.insertInto(RULE)
			.set(RULE.PROJECT_ID, projectId)
			.set(RULE.RULE_ID, ruleId)
			.set(RULE.ENTITY_TYPE, RuleEntityType.EVENT_ACTION)
			.set(RULE.ENTITY_ID, entityId)
			.set(RULE.RULE_TYPE, ruleType)
			.set(RULE.DESCRIPTION, description)
			.set(RULE.MESSAGE, messageJson)
			.set(RULE.TAG, tagJson)
			.onDuplicateKeyUpdate()
			.set(RULE.DESCRIPTION, description)
			.set(RULE.MESSAGE, messageJson)
			.set(RULE.TAG, tagJson)
			.execute();
	}
}
