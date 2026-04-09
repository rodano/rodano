package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Project;

import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

public class StatusParameterBackfillWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatusParameterBackfillWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Project> wrapped = (ProjectScoped<Project>) raw;
				final UUID projectId = wrapped.getProjectId();

				final var params = tx
					.select(
						RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID,
						RULE_ACTION_PARAMETER.VALUE,
						RULE_ACTION.RULE_ID,
						RULE_ACTION.CONDITION_ID,
						RULE_ACTION.RULE_ACTION_ID
					)
					.from(RULE_ACTION_PARAMETER)
					.join(RULE_ACTION).on(RULE_ACTION.RULE_ACTION_ID.eq(RULE_ACTION_PARAMETER.RULE_ACTION_ID))
					.where(RULE_ACTION_PARAMETER.PROJECT_ID.eq(projectId))
					.and(RULE_ACTION_PARAMETER.CODE.eq("STATUS"))
					.and(RULE_ACTION_PARAMETER.VALUE.isNotNull())
					.and(RULE_ACTION_PARAMETER.VALUE.notLike("________-____-____-____-____________"))
					.fetch();

				for(final var param : params) {
					final UUID paramId = param.value1();
					final String stateCode = param.value2();
					final UUID ruleId = param.value3();
					final UUID conditionId = param.value4();

					UUID resolvedStateUuid = null;

					if(conditionId != null) {
						resolvedStateUuid = resolveViaCondition(tx, projectId, conditionId, stateCode);
					}

					if(resolvedStateUuid == null) {
						resolvedStateUuid = resolveViaWorkflowAction(tx, projectId, ruleId, stateCode);
					}

					if(resolvedStateUuid == null) {
						resolvedStateUuid = resolveUnique(tx, projectId, stateCode);
					}

					if(resolvedStateUuid != null) {
						tx.update(RULE_ACTION_PARAMETER)
							.set(RULE_ACTION_PARAMETER.VALUE, resolvedStateUuid.toString())
							.where(RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID.eq(paramId))
							.execute();
						LOGGER.info("Resolved STATUS '{}' -> {}", stateCode, resolvedStateUuid);
					}
					else {
						LOGGER.warn("Could not resolve STATUS '{}' for param {}", stateCode, paramId);
					}
				}
			}
		});
	}

	/**
	 * Resolve state UUID via condition chain:
	 * condition -> criterion with property=ID, operator=EQUALS -> value is workflow code
	 * then find state by code in that workflow
	 */
	private static UUID resolveViaCondition(final DSLContext tx, final UUID projectId,
	                                        final UUID conditionId, final String stateCode) {
		final var workflowCodes = tx
			.select(RULE_CRITERION_VALUE.VALUE_TEXT)
			.from(RULE_CONDITION)
			.join(RULE_CRITERION).on(RULE_CRITERION.CONDITION_ID.eq(RULE_CONDITION.CONDITION_ID))
			.join(RULE_CRITERION_VALUE).on(RULE_CRITERION_VALUE.CRITERION_ID.eq(RULE_CRITERION.CRITERION_ID))
			.where(RULE_CONDITION.PROJECT_ID.eq(projectId))
			.and(RULE_CONDITION.PARENT_CONDITION_ID.eq(conditionId))
			.and(RULE_CRITERION.PROPERTY.eq("ID"))
			.fetch(RULE_CRITERION_VALUE.VALUE_TEXT);

		for(final String workflowCode : workflowCodes) {
			final UUID stateUuid = tx
				.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
				.from(WORKFLOW_STATE)
				.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(WORKFLOW_STATE.WORKFLOW_ID))
				.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
				.and(WORKFLOW.CODE.eq(workflowCode))
				.and(WORKFLOW_STATE.CODE.eq(stateCode))
				.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);

			if(stateUuid != null) {
				return stateUuid;
			}
		}
		return null;
	}

	/**
	 * Resolve state UUID via workflow action entity:
	 * rule -> entity_id (workflow_action_id) -> workflow_id -> state by code
	 */
	private static UUID resolveViaWorkflowAction(final DSLContext tx, final UUID projectId,
	                                             final UUID ruleId, final String stateCode) {
		final UUID workflowId = tx
			.select(WORKFLOW_ACTION.WORKFLOW_ID)
			.from(RULE)
			.join(WORKFLOW_ACTION).on(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(RULE.ENTITY_ID))
			.where(RULE.RULE_ID.eq(ruleId))
			.fetchOne(WORKFLOW_ACTION.WORKFLOW_ID);

		if(workflowId == null) {
			return null;
		}

		return tx
			.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
			.and(WORKFLOW_STATE.CODE.eq(stateCode))
			.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);
	}

	/**
	 * Fallback: resolve if only one workflow state with this code exists in the project
	 */
	private static UUID resolveUnique(final DSLContext tx, final UUID projectId, final String stateCode) {
		final var candidates = tx
			.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.CODE.eq(stateCode))
			.fetch(WORKFLOW_STATE.WORKFLOW_STATE_ID);

		return candidates.size() == 1 ? candidates.getFirst() : null;
	}
}
