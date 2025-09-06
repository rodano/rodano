package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowWidget {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private String workflowEntity;
	private Boolean filterExpectedEvents;

	private List<WorkflowStatesSelector> workflowStatesSelectors;
	private List<WorkflowWidgetColumn> columns;

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

	public String getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(final String workflowEntity) {
		this.workflowEntity = workflowEntity;
	}

	public Boolean getFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final Boolean filterExpectedEvents) {
		this.filterExpectedEvents = filterExpectedEvents;
	}

	public List<WorkflowStatesSelector> getWorkflowStatesSelectors() {
		return workflowStatesSelectors;
	}

	public void setWorkflowStatesSelectors(final List<WorkflowStatesSelector> workflowStatesSelectors) {
		this.workflowStatesSelectors = workflowStatesSelectors;
	}

	public List<WorkflowWidgetColumn> getColumns() {
		return columns;
	}

	public void setColumns(final List<WorkflowWidgetColumn> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "WorkflowWidget{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", workflowEntity='" + workflowEntity + '\'' +
			", filterExpectedEvents=" + filterExpectedEvents +
			", workflowStatesSelectors=" + workflowStatesSelectors +
			", columns=" + columns +
			'}';
	}
}
