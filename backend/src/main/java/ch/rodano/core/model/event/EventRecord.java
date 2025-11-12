package ch.rodano.core.model.event;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventRecord {

	protected boolean deleted;

	protected UUID projectId;
	protected String id;
	protected Long scopeFk;
	protected UUID scopeModelId;
	protected Integer eventGroupNumber;
	protected UUID eventModelId;
	protected ZonedDateTime expectedDate;
	protected ZonedDateTime date;
	protected ZonedDateTime endDate;
	protected Boolean notDone;
	protected Boolean blocking;
	protected Boolean locked;

	protected EventRecord() {
		deleted = false;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public UUID getScopeModelId() {
		return scopeModelId;
	}

	public void setScopeModelId(final UUID scopeModelId) {
		this.scopeModelId = scopeModelId;
	}

	public Integer getEventGroupNumber() {
		return eventGroupNumber;
	}

	public void setEventGroupNumber(final Integer eventGroupNumber) {
		this.eventGroupNumber = eventGroupNumber;
	}

	public UUID getEventModelId() {
		return eventModelId;
	}

	public void setEventModelId(final UUID eventModelId) {
		this.eventModelId = eventModelId;
	}

	public ZonedDateTime getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(final ZonedDateTime expectedDate) {
		this.expectedDate = expectedDate;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(final ZonedDateTime date) {
		this.date = date;
	}

	public ZonedDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(final ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	public Boolean getNotDone() {
		return notDone;
	}

	public void setNotDone(final Boolean notDone) {
		this.notDone = notDone;
	}

	public Boolean getBlocking() {
		return blocking;
	}

	public void setBlocking(final Boolean blocking) {
		this.blocking = blocking;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(final Boolean locked) {
		this.locked = locked;
	}
}
