package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.payment.PaymentDistribution;
import ch.rodano.core.model.jooq.tables.records.PaymentStepDistributionRecord;

import static ch.rodano.core.model.jooq.tables.PaymentStepDistribution.PAYMENT_STEP_DISTRIBUTION;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Repository
public class PaymentDistributionDAO {

	private final DSLContext dslContext;

	public PaymentDistributionDAO(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<PaymentDistribution> findByPaymentStep(final UUID paymentStepId) {
		return dslContext.selectFrom(PAYMENT_STEP_DISTRIBUTION)
			.where(PAYMENT_STEP_DISTRIBUTION.PAYMENT_STEP_ID.eq(paymentStepId))
			.fetch(this::mapToModel);
	}

	private PaymentDistribution mapToModel(final PaymentStepDistributionRecord record) {
		if(record == null) {
			return null;
		}

		final PaymentDistribution model = record.into(PaymentDistribution.class);

		model.setPaymentDistributionId(record.getPaymentStepDistributionId());

		if(record.getValue() != null) {
			model.setValue(record.getValue().doubleValue());
		}

		if(record.getScopeModelId() != null) {
			model.setScopeModelId(getScopeModelCode(record.getScopeModelId()));
		}

		if(record.getProfileId() != null) {
			model.setProfileId(getProfileCode(record.getProfileId()));
		}

		return model;
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}

	private String getProfileCode(final UUID profileId) {
		return dslContext.select(PROFILE.CODE)
			.from(PROFILE)
			.where(PROFILE.PROFILE_ID.eq(profileId))
			.fetchOne(PROFILE.CODE);
	}
}
