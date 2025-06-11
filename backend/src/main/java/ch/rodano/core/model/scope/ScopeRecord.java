package ch.rodano.core.model.scope;

import java.time.ZonedDateTime;

public class ScopeRecord {

	protected boolean deleted;

	protected String id;
	protected String code;
	protected String shortname;
	protected String longname;
	protected ZonedDateTime startDate;
	protected ZonedDateTime stopDate;

	protected String scopeModelId;

	protected ScopeData data;

	// TODO to replace this by the scope model configuration property
	@Deprecated
	protected Boolean virtual;

	protected String color;

	protected Integer expectedNumber;
	protected Integer maxNumber;
	protected Boolean locked;

	protected ScopeRecord() {
		deleted = false;
		virtual = false;
		locked = false;
		data = new ScopeData();
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final String getCode() {
		return code;
	}

	public final void setCode(final String code) {
		this.code = code;
	}

	public final String getShortname() {
		return shortname;
	}

	public final void setShortname(final String shortname) {
		this.shortname = shortname;
	}

	public final String getLongname() {
		return longname;
	}

	public final void setLongname(final String longname) {
		this.longname = longname;
	}

	public final String getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final String scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public final Boolean getVirtual() {
		return virtual;
	}

	public final void setVirtual(final Boolean virtual) {
		this.virtual = virtual;
	}

	public final String getColor() {
		return color;
	}

	public final void setColor(final String color) {
		this.color = color;
	}

	public ZonedDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(final ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	public ZonedDateTime getStopDate() {
		return stopDate;
	}

	public void setStopDate(final ZonedDateTime stopDate) {
		this.stopDate = stopDate;
	}

	public ScopeData getData() {
		return data;
	}

	public void setData(final ScopeData data) {
		this.data = data;
	}

	public final Integer getExpectedNumber() {
		return expectedNumber;
	}

	public final void setExpectedNumber(final Integer expectedNumber) {
		this.expectedNumber = expectedNumber;
	}

	public final Integer getMaxNumber() {
		return maxNumber;
	}

	public final void setMaxNumber(final Integer maxNumber) {
		this.maxNumber = maxNumber;
	}

	public final Boolean getLocked() {
		return locked;
	}

	public final void setLocked(final Boolean locked) {
		this.locked = locked;
	}

}
