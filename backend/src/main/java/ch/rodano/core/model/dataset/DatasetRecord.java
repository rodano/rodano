package ch.rodano.core.model.dataset;

public class DatasetRecord {

	protected boolean removed;

	protected String id;
	protected Long scopeFk;
	protected Long eventFk;
	protected String datasetModelId;

	protected DatasetRecord() {
		removed = false;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
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

	public Long getEventFk() {
		return eventFk;
	}

	public void setEventFk(final Long eventFk) {
		this.eventFk = eventFk;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

}
