package ch.rodano.core.model.payment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.TimestampableObject;

public final class PaymentBatch implements DeletableObject, TimestampableObject {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private Long pk;
	private ZonedDateTime creationTime;
	private ZonedDateTime lastUpdateTime;

	private boolean deleted;

	private String scopeId;
	private String planId;
	private PaymentBatchStatus status;
	private ZonedDateTime paymentDate;
	private ZonedDateTime closedDate;
	private ZonedDateTime printedDate;
	private String comment;

	private PaymentPlan plan;

	public PaymentBatch() {
		deleted = false;
	}

	public String getBatchId() {
		return getCreationTime() != null ? String.format("#%d / %s", getPk(), DATE_FORMATTER.format(getCreationTime())) : String.format("#%d", getPk());
	}

	public static PaymentBatch getInstance() {
		return new PaymentBatch();
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

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(final String scopeId) {
		this.scopeId = scopeId;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(final String planId) {
		this.planId = planId;
	}

	public PaymentBatchStatus getStatus() {
		return status;
	}

	public void setStatus(final PaymentBatchStatus status) {
		this.status = status;
	}

	public ZonedDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(final ZonedDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public ZonedDateTime getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(final ZonedDateTime closedDate) {
		this.closedDate = closedDate;
	}

	public ZonedDateTime getPrintedDate() {
		return printedDate;
	}

	public void setPrintedDate(final ZonedDateTime printedDate) {
		this.printedDate = printedDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public PaymentPlan getPlan() {
		return plan;
	}

	public void setPlan(final PaymentPlan plan) {
		this.plan = plan;
	}
}
