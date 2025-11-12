package ch.rodano.core.model.field;

import java.util.UUID;

public class FieldRecord {

	protected UUID projectId;
	protected Long datasetFk;
	protected UUID datasetModelId;
	protected UUID fieldModelId;
	protected String value;

	protected FieldRecord() {
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public Long getDatasetFk() {
		return datasetFk;
	}

	public void setDatasetFk(final Long datasetFk) {
		this.datasetFk = datasetFk;
	}

	public UUID getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final UUID datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public UUID getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final UUID fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
