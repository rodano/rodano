package ch.rodano.batch.writer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.PaymentPlan;
import ch.rodano.batch.pojo.PaymentStep;
import ch.rodano.batch.pojo.PaymentStepDistribution;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.PaymentStep.PAYMENT_STEP;
import static ch.rodano.core.model.jooq.tables.PaymentStepDistribution.PAYMENT_STEP_DISTRIBUTION;

public class PaymentPlanWriter extends BaseWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<PaymentPlan> wrapped = (ProjectScoped<PaymentPlan>) raw;
				final UUID projectId = wrapped.getProjectId();
				final PaymentPlan plan = wrapped.getPayload();

				final String planCode = plan.getId();
				final UUID planId = deterministic(projectId, "PAYMENT_PLAN", planCode);

				final UUID invoicedScopeModelId = resolveScopeModelId(tx, projectId, plan.getInvoicedScopeModel());
				final UUID workflowId = resolveWorkflowId(tx, projectId, plan.getWorkflow());

				tx.insertInto(PAYMENT_PLAN)
					.set(PAYMENT_PLAN.PROJECT_ID, projectId)
					.set(PAYMENT_PLAN.PAYMENT_PLAN_ID, planId)
					.set(PAYMENT_PLAN.CODE, planCode)
					.set(PAYMENT_PLAN.CURRENCY, plan.getCurrency())
					.set(PAYMENT_PLAN.INVOICED_SCOPE_MODEL_ID, invoicedScopeModelId)
					.set(PAYMENT_PLAN.WORKFLOW_ID, workflowId)
					.set(PAYMENT_PLAN.ALLOW_BATCH_MERGER, plan.getAllowBatchMerger())
					.set(PAYMENT_PLAN.EXTENDED_STEPS, plan.getExtendedSteps())
					.set(PAYMENT_PLAN.SHORTNAME, toJson(plan.getShortname()))
					.set(PAYMENT_PLAN.LONGNAME, toJson(plan.getLongname()))
					.set(PAYMENT_PLAN.DESCRIPTION, toJson(plan.getDescription()))
					.onDuplicateKeyUpdate()
					.set(PAYMENT_PLAN.CURRENCY, plan.getCurrency())
					.set(PAYMENT_PLAN.INVOICED_SCOPE_MODEL_ID, invoicedScopeModelId)
					.set(PAYMENT_PLAN.WORKFLOW_ID, workflowId)
					.set(PAYMENT_PLAN.ALLOW_BATCH_MERGER, plan.getAllowBatchMerger())
					.set(PAYMENT_PLAN.EXTENDED_STEPS, plan.getExtendedSteps())
					.set(PAYMENT_PLAN.SHORTNAME, toJson(plan.getShortname()))
					.set(PAYMENT_PLAN.LONGNAME, toJson(plan.getLongname()))
					.set(PAYMENT_PLAN.DESCRIPTION, toJson(plan.getDescription()))
					.execute();

				if(plan.getSteps() != null && !plan.getSteps().isEmpty()) {
					int stepOrder = 0;
					for(PaymentStep step : plan.getSteps()) {
						final UUID stepId = deterministic(projectId, "PAYMENT_STEP", planCode + "|" + step.getId());

						final UUID eventModelId = resolveEventModelId(tx, projectId, step.getWorkflowable());

						tx.insertInto(PAYMENT_STEP)
							.set(PAYMENT_STEP.PROJECT_ID, projectId)
							.set(PAYMENT_STEP.PAYMENT_STEP_ID, stepId)
							.set(PAYMENT_STEP.PAYMENT_PLAN_ID, planId)
							.set(PAYMENT_STEP.CODE, step.getId())
							.set(PAYMENT_STEP.REPEATABLE, step.getRepeatable())
							.set(PAYMENT_STEP.EVENT_MODEL_ID, eventModelId)
							.set(PAYMENT_STEP.SORT_ORDER, stepOrder)
							.set(PAYMENT_STEP.SHORTNAME, toJson(plan.getShortname()))
							.set(PAYMENT_STEP.LONGNAME, toJson(plan.getLongname()))
							.set(PAYMENT_STEP.DESCRIPTION, toJson(plan.getDescription()))
							.onDuplicateKeyUpdate()
							.set(PAYMENT_STEP.REPEATABLE, step.getRepeatable())
							.set(PAYMENT_STEP.EVENT_MODEL_ID, eventModelId)
							.set(PAYMENT_STEP.SORT_ORDER, stepOrder)
							.set(PAYMENT_STEP.SHORTNAME, toJson(plan.getShortname()))
							.set(PAYMENT_STEP.LONGNAME, toJson(plan.getLongname()))
							.set(PAYMENT_STEP.DESCRIPTION, toJson(plan.getDescription()))
							.execute();

						stepOrder++;

						if(step.getDistributions() != null && !step.getDistributions().isEmpty()) {
							for(PaymentStepDistribution distribution : step.getDistributions()) {
								final UUID scopeModelId = resolveScopeModelId(tx, projectId, distribution.getScopeModelId());
								final UUID profileId = resolveProfileId(tx, projectId, distribution.getProfileId());
								final BigDecimal value = distribution.getValue() == null ? null : new BigDecimal(String.valueOf(distribution.getValue()));

								tx.insertInto(PAYMENT_STEP_DISTRIBUTION)
									.set(PAYMENT_STEP_DISTRIBUTION.PROJECT_ID, projectId)
									.set(PAYMENT_STEP_DISTRIBUTION.PAYMENT_STEP_ID, stepId)
									.set(PAYMENT_STEP_DISTRIBUTION.SCOPE_MODEL_ID, scopeModelId)
									.set(PAYMENT_STEP_DISTRIBUTION.PROFILE_ID, profileId)
									.set(PAYMENT_STEP_DISTRIBUTION.VALUE, value)
									.onDuplicateKeyUpdate()
									.set(PAYMENT_STEP_DISTRIBUTION.VALUE, value)
									.execute();
							}
						}
					}
				}
			}
		});
	}
}
