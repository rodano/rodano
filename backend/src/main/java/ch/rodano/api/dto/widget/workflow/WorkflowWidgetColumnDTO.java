package ch.rodano.api.dto.widget.workflow;

import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.reports.WorkflowWidgetColumn;
import ch.rodano.configuration.model.reports.WorkflowWidgetColumnType;

@Schema(description = "Workflow widget column configuration")
public class WorkflowWidgetColumnDTO {
	@Schema(description = "Column ID")
	@NotBlank
	private String id;
	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	@Schema(description = "Type of column")
	@NotNull
	private WorkflowWidgetColumnType type;
	@Schema(description = "Width of the column in px")
	@NotNull
	private int width;

	public WorkflowWidgetColumnDTO(final WorkflowWidgetColumn workflowWidgetColumn) {
		this.id = workflowWidgetColumn.getId();
		this.shortname = workflowWidgetColumn.getShortname();
		this.longname = workflowWidgetColumn.getLongname();
		this.description = workflowWidgetColumn.getDescription();
		this.type = workflowWidgetColumn.getType();
		this.width = workflowWidgetColumn.getWidth();
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

	public WorkflowWidgetColumnType getType() {
		return type;
	}

	public void setType(final WorkflowWidgetColumnType type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}
}
