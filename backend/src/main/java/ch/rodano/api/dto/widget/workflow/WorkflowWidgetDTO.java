package ch.rodano.api.dto.widget.workflow;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.reports.WorkflowStatesSelector;
import ch.rodano.configuration.model.reports.WorkflowWidget;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;

@Schema(description = "Widget that shows workflows based on specified criteria")
public class WorkflowWidgetDTO {

	@Schema(description = "Widget ID")
	@NotBlank
	private String id;
	@NotNull
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	@Schema(description = "Are the workflows related to the expected events filtered out from the widget?")
	@NotNull
	private boolean filterExpectedEvents;

	@Schema(description = "Workflow entity to which the widget's workflows are tied to")
	@NotNull
	private WorkflowableEntity workflowEntity;
	@Schema(description = "Object that specifies the workflow selection criteria")
	@NotNull
	private List<WorkflowStatesSelector> workflowStatesSelectors;

	@Schema(description = "Widget columns configuration")
	@NotEmpty
	private List<WorkflowWidgetColumnDTO> columns;

	public WorkflowWidgetDTO(final WorkflowWidget workflowWidget) {
		this.id = workflowWidget.getId();
		this.shortname = workflowWidget.getShortname();
		this.longname = workflowWidget.getLongname();
		this.description = workflowWidget.getDescription();
		this.filterExpectedEvents = workflowWidget.isFilterExpectedEvents();
		this.workflowEntity = workflowWidget.getWorkflowEntity();
		this.workflowStatesSelectors = workflowWidget.getWorkflowStatesSelectors();
		this.columns = workflowWidget.getColumns().stream()
			.map(WorkflowWidgetColumnDTO::new)
			.toList();
	}

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

	public boolean isFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final boolean filterExpectedEvents) {
		this.filterExpectedEvents = filterExpectedEvents;
	}

	public WorkflowableEntity getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(final WorkflowableEntity workflowEntity) {
		this.workflowEntity = workflowEntity;
	}

	public List<WorkflowStatesSelector> getWorkflowStatesSelectors() {
		return workflowStatesSelectors;
	}

	public void setWorkflowStatesSelectors(final List<WorkflowStatesSelector> workflowStatesSelectors) {
		this.workflowStatesSelectors = workflowStatesSelectors;
	}

	public List<WorkflowWidgetColumnDTO> getColumns() {
		return columns;
	}

	public void setColumns(final List<WorkflowWidgetColumnDTO> columns) {
		this.columns = columns;
	}
}
