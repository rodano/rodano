package ch.rodano.core.model.payment;

import java.time.ZonedDateTime;

import ch.rodano.core.model.common.RemovableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class PaymentTarget implements RemovableObject, TimestampableObject {

	protected Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	protected boolean removed;

	private Long paymentFk;
	private String payableId;
	private Double value;

	public PaymentTarget() {
		removed = false;
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
	public boolean isRemoved() {
		return removed;
	}

	@Override
	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public Long getPaymentFk() {
		return paymentFk;
	}

	public void setPaymentFk(final Long paymentFk) {
		this.paymentFk = paymentFk;
	}

	public String getPayableId() {
		return payableId;
	}

	public void setPayableId(final String payableId) {
		this.payableId = payableId;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(final Double value) {
		this.value = value;
	}
}
