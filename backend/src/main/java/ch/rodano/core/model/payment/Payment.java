package ch.rodano.core.model.payment;

import java.time.ZonedDateTime;

import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.payment.PaymentStep;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.TimestampableObject;

public final class Payment implements DeletableObject, TimestampableObject, Comparable<Payment> {
	private static final String EXCLUDED = "Excluded";

	private Long pk;
	private ZonedDateTime creationTime;
	private ZonedDateTime lastUpdateTime;

	private boolean deleted;

	private Long paymentBatchFk;
	private Long workflowStatusFk;
	private String planId;
	private String stepId;
	private String status;
	private Double value;

	private PaymentPlan plan;

	public Payment() {
		deleted = false;
	}

	public PaymentStep getPaymentStep() {
		return getPlan().getStepFromId(getStepId());
	}

	public boolean isExcluded() {
		return EXCLUDED.equals(getStatus());
	}

	@Override
	public Long getPk() {
		return pk;
	}

	@Override
	public void setPk(final Long pk) {
		this.pk = pk;
	}

	@Override
	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@Override
	public boolean getDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public Long getPaymentBatchFk() {
		return paymentBatchFk;
	}

	public void setPaymentBatchFk(final Long paymentBatchFk) {
		this.paymentBatchFk = paymentBatchFk;
	}

	public Long getWorkflowStatusFk() {
		return workflowStatusFk;
	}

	public void setWorkflowStatusFk(final Long workflowStatusFk) {
		this.workflowStatusFk = workflowStatusFk;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(final String planId) {
		this.planId = planId;
	}

	public String getStepId() {
		return stepId;
	}

	public void setStepId(final String stepId) {
		this.stepId = stepId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(final Double value) {
		this.value = value;
	}

	public PaymentPlan getPlan() {
		return plan;
	}

	public void setPlan(final PaymentPlan plan) {
		this.plan = plan;
	}

	@Override
	public int compareTo(final Payment payment) {
		return pk.compareTo(payment.pk);
	}
}
