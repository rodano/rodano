package ch.rodano.api.config;

import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class WorkflowSummaryColumnDTO {

	@NotNull
	private UUID summaryColumnId;
	private UUID workflowSummaryId;

	@NotNull
	private SortedMap<String, String> label;
	private SortedMap<String, String> description;

	private boolean total;
	private boolean percent;
	private String nonNullColor;
	private String nonNullBgColor;

	private Integer sortOrder;

	@NotNull
	private List<UUID> workflowStateIds;

	public UUID getSummaryColumnId() {
		return summaryColumnId;
	}

	public void setSummaryColumnId(final UUID summaryColumnId) {
		this.summaryColumnId = summaryColumnId;
	}

	public UUID getWorkflowSummaryId() {
		return workflowSummaryId;
	}

	public void setWorkflowSummaryId(final UUID workflowSummaryId) {
		this.workflowSummaryId = workflowSummaryId;
	}

	public SortedMap<String, String> getLabel() {
		return label;
	}

	public void setLabel(final SortedMap<String, String> label) {
		this.label = label;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public boolean isTotal() {
		return total;
	}

	public void setTotal(final boolean total) {
		this.total = total;
	}

	public boolean isPercent() {
		return percent;
	}

	public void setPercent(final boolean percent) {
		this.percent = percent;
	}

	public String getNonNullColor() {
		return nonNullColor;
	}

	public void setNonNullColor(final String nonNullColor) {
		this.nonNullColor = nonNullColor;
	}

	public String getNonNullBgColor() {
		return nonNullBgColor;
	}

	public void setNonNullBgColor(final String nonNullBgColor) {
		this.nonNullBgColor = nonNullBgColor;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<UUID> getWorkflowStateIds() {
		return workflowStateIds;
	}

	public void setWorkflowStateIds(final List<UUID> workflowStateIds) {
		this.workflowStateIds = workflowStateIds;
	}
}
