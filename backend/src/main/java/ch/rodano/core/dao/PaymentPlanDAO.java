package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.core.model.jooq.tables.records.PaymentPlanRecord;

import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

@Repository
public class PaymentPlanDAO implements BaseProjectDAO<PaymentPlan> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final PaymentStepDAO paymentStepDAO;

	public PaymentPlanDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final PaymentStepDAO paymentStepDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.paymentStepDAO = paymentStepDAO;
	}

	@Override
	public List<PaymentPlan> findByProject(final UUID projectId) {
		return dslContext.selectFrom(PAYMENT_PLAN)
			.where(PAYMENT_PLAN.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public PaymentPlan findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(PAYMENT_PLAN)
			.where(PAYMENT_PLAN.PROJECT_ID.eq(projectId))
			.and(PAYMENT_PLAN.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public PaymentPlan findById(final UUID id) {
		return dslContext.selectFrom(PAYMENT_PLAN)
			.where(PAYMENT_PLAN.PAYMENT_PLAN_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public PaymentPlan save(final PaymentPlan entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private PaymentPlan mapToModel(final PaymentPlanRecord record) {
		if(record == null) {
			return null;
		}

		final PaymentPlan model = new PaymentPlan();

		model.setId(record.getCode());
		model.setPaymentPlanId(record.getPaymentPlanId());

		model.setCurrency(record.getCurrency());
		model.setAllowBatchMerger(record.getAllowBatchMerger() != null ? record.getAllowBatchMerger() : false);
		model.setExtendedSteps(record.getExtendedSteps() != null ? record.getExtendedSteps() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		if(record.getInvoicedScopeModelId() != null) {
			model.setInvoicedScopeModel(getScopeModelCode(record.getInvoicedScopeModelId()));
		}

		if(record.getWorkflowId() != null) {
			model.setWorkflow(getWorkflowCode(record.getWorkflowId()));
		}

		model.setSteps(paymentStepDAO.findByPaymentPlan(record.getPaymentPlanId()));

		return model;
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}

	private String getWorkflowStateCode(final UUID stateId) {
		return dslContext.select(WORKFLOW_STATE.CODE)
			.from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(stateId))
			.fetchOne(WORKFLOW_STATE.CODE);
	}
}
