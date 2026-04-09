package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Cron;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class CronRuleWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Cron> wrapped = (ProjectScoped<Cron>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Cron cron = wrapped.getPayload();

				final String cronCode = cron.getId();
				if(cronCode == null || cronCode.isBlank()) {
					continue;
				}

				final UUID cronId = deterministic(projectId, "CRON", cronCode);

				final List<Rule> rules = cron.getRules();
				if(rules == null || rules.isEmpty()) {
					continue;
				}

				for(int idx = 0; idx < rules.size(); idx++) {
					final Rule rule = rules.get(idx);
					final UUID ruleId = deterministic(projectId, "CRON_RULE", cronCode + "|" + idx);

					tx.insertInto(RULE)
						.set(RULE.PROJECT_ID, projectId)
						.set(RULE.RULE_ID, ruleId)
						.set(RULE.ENTITY_TYPE, RuleEntityType.CRON)
						.set(RULE.ENTITY_ID, cronId)
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
		});
	}
}
