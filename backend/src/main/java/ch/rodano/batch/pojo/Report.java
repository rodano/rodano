package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private String datasetModelId;
	private String workflowId;
	private List<String> fieldModelIds;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public List<String> getFieldModelIds() {
		return fieldModelIds;
	}

	public void setFieldModelIds(final List<String> fieldModelIds) {
		this.fieldModelIds = fieldModelIds;
	}

	@Override
	public String toString() {
		return "Report{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", datasetModelId='" + datasetModelId + '\'' +
			", workflowId='" + workflowId + '\'' +
			", fieldModelIds=" + fieldModelIds +
			'}';
	}
}
