package ch.rodano.core.model.form;

import java.util.UUID;

public class FormRecord {

	protected boolean deleted;

	protected UUID projectId;
	protected Long scopeFk;
	protected Long eventFk;
	protected UUID formModelId;

	protected FormRecord() {
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

	public Long getScopeFk() {
		return scopeFk;
	}

	public void setScopeFk(final Long scopeFk) {
		this.scopeFk = scopeFk;
	}

	public Long getEventFk() {
		return eventFk;
	}

	public void setEventFk(final Long eventFk) {
		this.eventFk = eventFk;
	}

	public UUID getFormModelId() {
		return formModelId;
	}

	public void setFormModelId(final UUID formModelId) {
		this.formModelId = formModelId;
	}
}
