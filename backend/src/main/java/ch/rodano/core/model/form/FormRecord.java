package ch.rodano.core.model.form;

public class FormRecord {

	protected boolean deleted;

	protected Long scopeFk;
	protected Long eventFk;
	protected String formModelId;

	protected FormRecord() {
		deleted = false;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
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

	public String getFormModelId() {
		return formModelId;
	}

	public void setFormModelId(final String formModelId) {
		this.formModelId = formModelId;
	}
}
