package ch.rodano.batch.writer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.batch.pojo.Workflow;
import ch.rodano.batch.pojo.WorkflowAction;
import ch.rodano.batch.pojo.WorkflowState;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowActionId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowStatePossibleAction.WORKFLOW_STATE_POSSIBLE_ACTION;

public class WorkflowWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Workflow> wrapped = (ProjectScoped<Workflow>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Workflow workflow = wrapped.getPayload();

				final var existing = resolveWorkflowId(tx, projectId, workflow.getId());
				final UUID workflowId = existing != null
					? existing
					: deterministic(projectId, "WORKFLOW", workflow.getId());

				tx.insertInto(WORKFLOW)
					.set(WORKFLOW.PROJECT_ID, projectId)
					.set(WORKFLOW.WORKFLOW_ID, workflowId)
					.set(WORKFLOW.CODE, workflow.getId())
					.set(WORKFLOW.ORDER_BY, workflow.getOrderBy())
					.set(WORKFLOW.MANDATORY, workflow.isMandatory())
					.set(WORKFLOW.IS_UNIQUE, workflow.isUnique())
					.set(WORKFLOW.ICON, workflow.getIcon())
					.set(WORKFLOW.SHORTNAME, toJson(workflow.getShortname()))
					.set(WORKFLOW.LONGNAME, toJson(workflow.getLongname()))
					.set(WORKFLOW.DESCRIPTION, toJson(workflow.getDescription()))
					.set(WORKFLOW.MESSAGE, toJson(workflow.getMessage()))
					.onDuplicateKeyUpdate()
					.set(WORKFLOW.ORDER_BY, workflow.getOrderBy())
					.set(WORKFLOW.MANDATORY, workflow.isMandatory())
					.set(WORKFLOW.IS_UNIQUE, workflow.isUnique())
					.set(WORKFLOW.ICON, workflow.getIcon())
					.set(WORKFLOW.SHORTNAME, toJson(workflow.getShortname()))
					.set(WORKFLOW.LONGNAME, toJson(workflow.getLongname()))
					.set(WORKFLOW.DESCRIPTION, toJson(workflow.getDescription()))
					.set(WORKFLOW.MESSAGE, toJson(workflow.getMessage()))
					.execute();

				if(workflow.getActions() != null && !workflow.getActions().isEmpty()) {
					for(WorkflowAction action : workflow.getActions()) {
						final var existingActionId = resolveWorkflowActionId(tx, projectId, workflowId, action.getId());
						final UUID actionId = existingActionId != null
							? existingActionId
							: deterministic(projectId, "WORKFLOW_ACTION", workflow.getId() + "|" + action.getId());

						tx.insertInto(WORKFLOW_ACTION)
							.set(WORKFLOW_ACTION.PROJECT_ID, projectId)
							.set(WORKFLOW_ACTION.WORKFLOW_ID, workflowId)
							.set(WORKFLOW_ACTION.WORKFLOW_ACTION_ID, actionId)
							.set(WORKFLOW_ACTION.CODE, action.getId())
							.set(WORKFLOW_ACTION.DOCUMENTABLE, action.isDocumentable())
							.set(WORKFLOW_ACTION.REQUIRE_SIGNATURE, action.isRequireSignature())
							.set(WORKFLOW_ACTION.DOCUMENTABLE_OPTIONS, toJson(action.getDocumentableOptions()))
							.set(WORKFLOW_ACTION.SHORTNAME, toJson(action.getShortname()))
							.set(WORKFLOW_ACTION.LONGNAME, toJson(action.getLongname()))
							.set(WORKFLOW_ACTION.DESCRIPTION, toJson(action.getDescription()))
							.set(WORKFLOW_ACTION.REQUIRED_SIGNATURE_TEXT, toJson(action.getRequiredSignatureText()))
							.set(WORKFLOW_ACTION.ICON, action.getIcon())
							.onDuplicateKeyUpdate()
							.set(WORKFLOW_ACTION.DOCUMENTABLE, action.isDocumentable())
							.set(WORKFLOW_ACTION.REQUIRE_SIGNATURE, action.isRequireSignature())
							.set(WORKFLOW_ACTION.DOCUMENTABLE_OPTIONS, toJson(action.getDocumentableOptions()))
							.set(WORKFLOW_ACTION.SHORTNAME, toJson(action.getShortname()))
							.set(WORKFLOW_ACTION.LONGNAME, toJson(action.getLongname()))
							.set(WORKFLOW_ACTION.DESCRIPTION, toJson(action.getDescription()))
							.set(WORKFLOW_ACTION.REQUIRED_SIGNATURE_TEXT, toJson(action.getRequiredSignatureText()))
							.set(WORKFLOW_ACTION.ICON, action.getIcon())
							.execute();

						final List<Rule> actionRules = action.getRules();
						if(actionRules != null && !actionRules.isEmpty()) {
							for(int idx = 0; idx < actionRules.size(); idx++) {
								final Rule rule = actionRules.get(idx);
								final UUID ruleId = deterministic(projectId, "WORKFLOW_ACTION_RULE", workflow.getId() + "|" + action.getId() + "|" + idx);

								tx.insertInto(RULE)
									.set(RULE.PROJECT_ID, projectId)
									.set(RULE.RULE_ID, ruleId)
									.set(RULE.ENTITY_TYPE, RuleEntityType.WORKFLOW_ACTION)
									.set(RULE.ENTITY_ID, actionId)
									.set(RULE.RULE_TYPE, action.getId())
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
				}

				UUID initialStateUuid = null;
				final Map<String, UUID> stateIdByCodeInThisWorkflow =
					tx.select(WORKFLOW_STATE.CODE, WORKFLOW_STATE.WORKFLOW_STATE_ID)
						.from(WORKFLOW_STATE)
						.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId)
							.and(WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId)))
						.fetchMap(WORKFLOW_STATE.CODE, WORKFLOW_STATE.WORKFLOW_STATE_ID);

				if(workflow.getStates() != null && !workflow.getStates().isEmpty()) {
					for(WorkflowState state : workflow.getStates()) {

						final var existingStateId = resolveWorkflowStateId(tx, projectId, workflowId, state.getId());
						final UUID stateId = existingStateId != null
							? existingStateId
							: deterministic(projectId, "WORKFLOW_STATE", workflow.getId() + "|" + state.getId());

						tx.insertInto(WORKFLOW_STATE)
							.set(WORKFLOW_STATE.PROJECT_ID, projectId)
							.set(WORKFLOW_STATE.WORKFLOW_ID, workflowId)
							.set(WORKFLOW_STATE.WORKFLOW_STATE_ID, stateId)
							.set(WORKFLOW_STATE.CODE, state.getId())
							.set(WORKFLOW_STATE.IMPORTANT, state.isImportant())
							.set(WORKFLOW_STATE.COLOR, state.getColor())
							.set(WORKFLOW_STATE.ICON, state.getIcon())
							.set(WORKFLOW_STATE.AGGREGATE_STATE_MATCHER, state.getAggregateStateMatcher())
							.set(WORKFLOW_STATE.SHORTNAME, toJson(state.getShortname()))
							.set(WORKFLOW_STATE.LONGNAME, toJson(state.getLongname()))
							.set(WORKFLOW_STATE.DESCRIPTION, toJson(state.getDescription()))
							.onDuplicateKeyUpdate()
							.set(WORKFLOW_STATE.IMPORTANT, state.isImportant())
							.set(WORKFLOW_STATE.COLOR, state.getColor())
							.set(WORKFLOW_STATE.ICON, state.getIcon())
							.set(WORKFLOW_STATE.AGGREGATE_STATE_MATCHER, state.getAggregateStateMatcher())
							.set(WORKFLOW_STATE.SHORTNAME, toJson(state.getShortname()))
							.set(WORKFLOW_STATE.LONGNAME, toJson(state.getLongname()))
							.set(WORKFLOW_STATE.DESCRIPTION, toJson(state.getDescription()))
							.execute();

						final String aggregateCode = trimOrNull(state.getAggregateStateId());
						if(aggregateCode != null) {
							final UUID resolvedAggId = resolveAggregateStateId(tx, projectId, aggregateCode, stateIdByCodeInThisWorkflow);
							if(resolvedAggId != null) {
								tx.update(WORKFLOW_STATE)
									.set(WORKFLOW_STATE.AGGREGATE_STATE_ID, resolvedAggId)
									.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId)
										.and(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(stateId)))
									.execute();
							}
						}

						if(state.getPossibleActionIds() != null && !state.getPossibleActionIds().isEmpty()) {
							for(String actionCode : state.getPossibleActionIds()) {
								final var existingActionId = resolveWorkflowActionId(tx, projectId, workflowId, actionCode);
								final UUID actionId = existingActionId != null
									? existingActionId
									: deterministic(projectId, "WORKFLOW_ACTION", workflow.getId() + "|" + actionCode);

								tx.insertInto(WORKFLOW_STATE_POSSIBLE_ACTION)
									.set(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID, projectId)
									.set(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID, stateId)
									.set(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_ACTION_ID, actionId)
									.onDuplicateKeyIgnore()
									.execute();
							}
						}

						if(Objects.equals(workflow.getInitialStateId(), state.getId())) {
							initialStateUuid = stateId;
						}
					}
				}

				if(initialStateUuid != null) {
					tx.update(WORKFLOW)
						.set(WORKFLOW.INITIAL_STATE_ID, initialStateUuid)
						.where(WORKFLOW.PROJECT_ID.eq(projectId)
							.and(WORKFLOW.WORKFLOW_ID.eq(workflowId)))
						.execute();
				}

				final List<Rule> workflowRules = workflow.getRules();
				if(workflowRules != null && !workflowRules.isEmpty()) {
					for(int idx = 0; idx < workflowRules.size(); idx++) {
						final Rule rule = workflowRules.get(idx);
						final UUID ruleId = deterministic(projectId, "WORKFLOW_RULE", workflow.getId() + "|" + idx);

						tx.insertInto(RULE)
							.set(RULE.PROJECT_ID, projectId)
							.set(RULE.RULE_ID, ruleId)
							.set(RULE.ENTITY_TYPE, RuleEntityType.WORKFLOW)
							.set(RULE.ENTITY_ID, workflowId)
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
		});
	}

	private static UUID resolveAggregateStateId(
		final DSLContext tx,
		final UUID projectId,
		final String aggregateCode,
		final Map<String, UUID> stateIdByCodeInThisWorkflow
	) {
		final UUID aggId = stateIdByCodeInThisWorkflow.get(aggregateCode);
		if(aggId != null) {
			return aggId;
		}

		final var candidates = tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId)
				.and(WORKFLOW_STATE.CODE.eq(aggregateCode)))
			.fetch(WORKFLOW_STATE.WORKFLOW_STATE_ID);

		if(candidates.isEmpty()) {
			return null;
		}
		if(candidates.size() == 1) {
			return candidates.getFirst();
		}
		return null;
	}

	private static String trimOrNull(final String s) {
		if(s == null) {
			return null;
		}
		final String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
