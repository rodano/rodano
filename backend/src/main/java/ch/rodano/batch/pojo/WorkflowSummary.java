package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowSummary {

	private String id;
	private Map<String, String> title;

	private Boolean filterExpectedEvents;
	private Boolean displayLegend;
	private Boolean displayColumnExport;
	private String workflowEntity;
	private String leafScopeModelId;

	private List<String> workflowIds;
	private List<String> filterEventModelIds;

	private List<WorkflowSummaryColumn> columns;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getTitle() {
		return title;
	}

	public void setTitle(final Map<String, String> title) {
		this.title = title;
	}

	public Boolean getFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final Boolean filterExpectedEvents) {
		this.filterExpectedEvents = filterExpectedEvents;
	}

	public Boolean getDisplayLegend() {
		return displayLegend;
	}

	public void setDisplayLegend(final Boolean displayLegend) {
		this.displayLegend = displayLegend;
	}

	public Boolean getDisplayColumnExport() {
		return displayColumnExport;
	}

	public void setDisplayColumnExport(final Boolean displayColumnExport) {
		this.displayColumnExport = displayColumnExport;
	}

	public String getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(final String workflowEntity) {
		this.workflowEntity = workflowEntity;
	}

	public String getLeafScopeModelId() {
		return leafScopeModelId;
	}

	public void setLeafScopeModelId(final String leafScopeModelId) {
		this.leafScopeModelId = leafScopeModelId;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public List<String> getFilterEventModelIds() {
		return filterEventModelIds;
	}

	public void setFilterEventModelIds(final List<String> filterEventModelIds) {
		this.filterEventModelIds = filterEventModelIds;
	}

	public List<WorkflowSummaryColumn> getColumns() {
		return columns;
	}

	public void setColumns(final List<WorkflowSummaryColumn> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "WorkflowSummary{" +
			"id='" + id + '\'' +
			", title=" + title +
			", filterExpectedEvents=" + filterExpectedEvents +
			", displayLegend=" + displayLegend +
			", displayColumnExport=" + displayColumnExport +
			", workflowEntity='" + workflowEntity + '\'' +
			", leafScopeModelId='" + leafScopeModelId + '\'' +
			", workflowIds=" + workflowIds +
			", filterEventModelIds=" + filterEventModelIds +
			", columns=" + columns +
			'}';
	}
}
