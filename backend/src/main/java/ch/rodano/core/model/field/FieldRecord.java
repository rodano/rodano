package ch.rodano.core.model.field;

public class FieldRecord {

	protected Long datasetFk;
	protected String datasetModelId;
	protected String fieldModelId;
	protected String value;

	protected FieldRecord() {}

	public Long getDatasetFk() {
		return datasetFk;
	}

	public void setDatasetFk(final Long datasetFk) {
		this.datasetFk = datasetFk;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getFieldModelId() {
		return fieldModelId;
	}

	public void setFieldModelId(final String fieldModelId) {
		this.fieldModelId = fieldModelId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
