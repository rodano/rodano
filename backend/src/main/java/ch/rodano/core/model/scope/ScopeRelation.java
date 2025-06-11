package ch.rodano.core.model.scope;

import java.time.ZonedDateTime;

import ch.rodano.core.model.common.HardDeletableObject;
import ch.rodano.core.model.common.IdentifiableObject;
import ch.rodano.core.model.common.TimestampableObject;

public class ScopeRelation implements IdentifiableObject, TimestampableObject, HardDeletableObject, Comparable<ScopeRelation> {

	protected Long pk;
	protected ZonedDateTime creationTime;
	protected ZonedDateTime lastUpdateTime;
	private Long scopeFk;
	private Long parentFk;
	private ZonedDateTime startDate;
	private ZonedDateTime endDate;
	private Boolean defaultRelation;

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

	public ZonedDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(final ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	public ZonedDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(final ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	public Boolean getDefault() {
		return defaultRelation;
	}

	public void setDefault(final Boolean defaultRelation) {
		this.defaultRelation = defaultRelation;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public Long getParentFk() {
		return parentFk;
	}

	public void setParentFk(final Long parentFk) {
		this.parentFk = parentFk;
	}

	@Override
	public int compareTo(final ScopeRelation o) {
		return startDate.compareTo(o.startDate);
	}
}
