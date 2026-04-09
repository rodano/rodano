package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.batch.pojo.Workflow;
import ch.rodano.batch.pojo.WorkflowAction;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowActionId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;

public class WorkflowRuleWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Workflow> wrapped = (ProjectScoped<Workflow>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Workflow workflow = wrapped.getPayload();

				final UUID workflowId = resolveWorkflowId(tx, projectId, workflow.getId());

				if(workflow.getRules() != null && !workflow.getRules().isEmpty()) {
					for(int idx = 0; idx < workflow.getRules().size(); idx++) {
						final Rule rule = workflow.getRules().get(idx);
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
							insertConstraintForOwner(tx, projectId, "RULE", ruleId,
								rule.getConstraint(), RuleConstraintConstraintType.RULE, workflowId);
						}
						if(rule.getActions() != null && !rule.getActions().isEmpty()) {
							insertRuleActions(tx, projectId, ruleId, rule.getActions(), workflowId);
						}
					}
				}

				if(workflow.getActions() != null && !workflow.getActions().isEmpty()) {
					for(WorkflowAction action : workflow.getActions()) {
						final UUID actionId = resolveWorkflowActionId(tx, projectId, workflowId, action.getId());

						if(action.getRules() != null && !action.getRules().isEmpty()) {
							for(int idx = 0; idx < action.getRules().size(); idx++) {
								final Rule rule = action.getRules().get(idx);
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
									insertConstraintForOwner(tx, projectId, "RULE", ruleId,
										rule.getConstraint(), RuleConstraintConstraintType.RULE, workflowId);
								}
								if(rule.getActions() != null && !rule.getActions().isEmpty()) {
									insertRuleActions(tx, projectId, ruleId, rule.getActions(), workflowId);
								}
							}
						}
					}
				}
			}
		});
	}
}
