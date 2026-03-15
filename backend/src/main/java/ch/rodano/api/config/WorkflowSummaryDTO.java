package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkflowSummaryDTO {

	@NotNull
	private UUID workflowSummaryId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> title;

	@NotNull
	private String workflowEntity;
	private boolean filterExpectedEvents;
	private boolean displayLegend;
	private boolean displayColumnExport;

	private UUID leafScopeModelId;

	@NotNull
	private List<UUID> workflowIds;
	@NotNull
	private List<UUID> eventModelIds;
	@NotNull
	private List<WorkflowSummaryColumnDTO> columns;

	public UUID getWorkflowSummaryId() {
		return workflowSummaryId;
	}

	public void setWorkflowSummaryId(final UUID workflowSummaryId) {
		this.workflowSummaryId = workflowSummaryId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getTitle() {
		return title;
	}

	public void setTitle(final SortedMap<String, String> title) {
		this.title = title;
	}

	public String getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(final String workflowEntity) {
		this.workflowEntity = workflowEntity;
	}

	public boolean isFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final boolean filterExpectedEvents) {
		this.filterExpectedEvents = filterExpectedEvents;
	}

	public boolean isDisplayLegend() {
		return displayLegend;
	}

	public void setDisplayLegend(final boolean displayLegend) {
		this.displayLegend = displayLegend;
	}

	public boolean isDisplayColumnExport() {
		return displayColumnExport;
	}

	public void setDisplayColumnExport(final boolean displayColumnExport) {
		this.displayColumnExport = displayColumnExport;
	}

	public UUID getLeafScopeModelId() {
		return leafScopeModelId;
	}

	public void setLeafScopeModelId(final UUID leafScopeModelId) {
		this.leafScopeModelId = leafScopeModelId;
	}

	public List<UUID> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<UUID> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public List<UUID> getEventModelIds() {
		return eventModelIds;
	}

	public void setEventModelIds(final List<UUID> eventModelIds) {
		this.eventModelIds = eventModelIds;
	}

	public List<WorkflowSummaryColumnDTO> getColumns() {
		return columns;
	}

	public void setColumns(final List<WorkflowSummaryColumnDTO> columns) {
		this.columns = columns;
	}
}
