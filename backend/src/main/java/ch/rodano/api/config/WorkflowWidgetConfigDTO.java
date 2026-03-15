package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkflowWidgetConfigDTO {

	@NotNull
	private UUID workflowWidgetId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	@NotNull
	private String workflowEntity;
	private boolean filterExpectedEvents;

	@NotNull
	private List<WorkflowWidgetColumnConfigDTO> columns;
	@NotNull
	private List<UUID> workflowStateIds;

	public UUID getWorkflowWidgetId() {
		return workflowWidgetId;
	}

	public void setWorkflowWidgetId(final UUID workflowWidgetId) {
		this.workflowWidgetId = workflowWidgetId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
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

	public List<WorkflowWidgetColumnConfigDTO> getColumns() {
		return columns;
	}

	public void setColumns(final List<WorkflowWidgetColumnConfigDTO> columns) {
		this.columns = columns;
	}

	public List<UUID> getWorkflowStateIds() {
		return workflowStateIds;
	}

	public void setWorkflowStateIds(final List<UUID> workflowStateIds) {
		this.workflowStateIds = workflowStateIds;
	}
}
