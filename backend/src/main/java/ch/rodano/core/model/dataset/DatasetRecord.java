package ch.rodano.core.model.dataset;

import java.util.UUID;

public class DatasetRecord {

	protected boolean deleted;

	protected UUID projectId;
	protected String id;
	protected Long scopeFk;
	protected Long eventFk;
	protected UUID datasetModelId;

	protected DatasetRecord() {
		deleted = false;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
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

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

}
