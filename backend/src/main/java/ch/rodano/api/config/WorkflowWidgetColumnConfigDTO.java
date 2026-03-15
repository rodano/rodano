package ch.rodano.api.config;

import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkflowWidgetColumnConfigDTO {

	@NotNull
	private UUID workflowWidgetColumnId;
	@NotBlank
	private String id;

	@NotNull
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	@NotNull
	private String type;
	private Integer width;

	private Integer sortOrder;

	public UUID getWorkflowWidgetColumnId() {
		return workflowWidgetColumnId;
	}

	public void setWorkflowWidgetColumnId(final UUID workflowWidgetColumnId) {
		this.workflowWidgetColumnId = workflowWidgetColumnId;
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

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(final Integer width) {
		this.width = width;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
