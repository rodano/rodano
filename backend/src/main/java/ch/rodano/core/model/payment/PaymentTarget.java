package ch.rodano.core.model.payment;

import java.time.ZonedDateTime;

import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class PaymentTarget implements DeletableObject, TimestampableObject {

	protected Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;

	protected boolean deleted;

	private Long paymentFk;
	private String payableId;
	private Double value;

	public PaymentTarget() {
		deleted = false;
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
