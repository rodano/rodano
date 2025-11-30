package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.payment.PaymentStep;
import ch.rodano.core.model.jooq.tables.records.PaymentStepRecord;

import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.PaymentStep.PAYMENT_STEP;

@Repository
public class PaymentStepDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final PaymentDistributionDAO paymentDistributionDAO;

	public PaymentStepDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final PaymentDistributionDAO paymentDistributionDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.paymentDistributionDAO = paymentDistributionDAO;
	}

	public List<PaymentStep> findByPaymentPlan(final UUID paymentPlanId) {
		return dslContext.selectFrom(PAYMENT_STEP)
			.where(PAYMENT_STEP.PAYMENT_PLAN_ID.eq(paymentPlanId))
			.orderBy(PAYMENT_STEP.SORT_ORDER)
			.fetch(this::mapToModel);
	}

	private PaymentStep mapToModel(final PaymentStepRecord record) {
		if(record == null) {
			return null;
		}

		final PaymentStep model = new PaymentStep();

		model.setId(record.getCode());
		model.setPaymentStepId(record.getPaymentStepId());

		model.setRepeatable(record.getRepeatable() != null ? record.getRepeatable() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		if(record.getEventModelId() != null) {
			model.setWorkflowable(getEventModelCode(record.getEventModelId()));
		}

		model.setDistributions(paymentDistributionDAO.findByPaymentStep(record.getPaymentStepId()));

		return model;
	}

	private String getEventModelCode(final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL.CODE)
			.from(EVENT_MODEL)
			.where(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetchOne(EVENT_MODEL.CODE);
	}
}
