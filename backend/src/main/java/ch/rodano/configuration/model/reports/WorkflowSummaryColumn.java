package ch.rodano.configuration.model.reports;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;

import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;

public class WorkflowSummaryColumn implements Node {
	@Serial
	private static final long serialVersionUID = 6897340405982829336L;

	private UUID summaryColumnId;

	private SortedMap<String, String> label;
	private SortedMap<String, String> description;
	private boolean total;
	private Set<String> stateIds;
	private boolean percent;
	private String nonNullColor;
	private String nonNullBackgroundColor;
	private WorkflowSummary workflowSummary;

	public WorkflowSummaryColumn() {
		label = new TreeMap<>();
		description = new TreeMap<>();
	}

	public UUID getSummaryColumnId() {
		if(this.summaryColumnId == null && this.workflowSummary != null) {
			final int colOrder = this.workflowSummary.getColumns().indexOf(this);
			if(colOrder >= 0) {
				this.summaryColumnId = deterministic(
					this.workflowSummary.getStudy().getProjectId(),
					"WORKFLOW_SUMMARY_COLUMN",
					this.workflowSummary.getId() + "|" + colOrder
				);
			}
		}
		return summaryColumnId;
	}

	public void setSummaryColumnId(final UUID summaryColumnId) {
		this.summaryColumnId = summaryColumnId;
	}

	public final SortedMap<String, String> getLabel() {
		return label;
	}

	public final void setLabel(final SortedMap<String, String> labels) {
		label = labels;
	}

	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final boolean isTotal() {
		return total;
	}

	public final void setTotal(final boolean total) {
		this.total = total;
	}

	public final Set<String> getStateIds() {
		return stateIds;
	}

	public final void setStateIds(final Set<String> stateIds) {
		this.stateIds = stateIds;
	}

	public final boolean isPercent() {
		return percent;
	}

	public final void setPercent(final boolean percent) {
		this.percent = percent;
	}

	public final String getNonNullColor() {
		return nonNullColor;
	}

	public final void setNonNullColor(final String nonNullColor) {
		this.nonNullColor = nonNullColor;
	}

	public final String getNonNullBackgroundColor() {
		return nonNullBackgroundColor;
	}

	public final void setNonNullBackgroundColor(final String nonNullBackgroundColor) {
		this.nonNullBackgroundColor = nonNullBackgroundColor;
	}

	public final String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(label, languages);
	}

	public final String getLocalizedDescription(final String... languages) {
		return DisplayableUtils.getLocalizedMap(description, languages);
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW_SUMMARY_COLUMN;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@JsonBackReference
	public WorkflowSummary getWorkflowSummary() {
		return workflowSummary;
	}

	@JsonBackReference
	public void setWorkflowSummary(final WorkflowSummary workflowSummary) {
		this.workflowSummary = workflowSummary;
	}

	@JsonIgnore
	public Set<UUID> getStateUuids() {
		if(this.workflowSummary == null || this.stateIds == null || this.stateIds.isEmpty()) {
			return Collections.emptySet();
		}

		final Study study = workflowSummary.getStudy();
		final List<String> workflowIds = workflowSummary.getWorkflowIds();

		if(workflowIds == null || workflowIds.isEmpty()) {
			return Collections.emptySet();
		}

		// Generate state UUIDs with workflow prefix
		return workflowIds.stream()
			.flatMap(workflowCode -> this.stateIds.stream()
				.map(stateCode -> deterministic(study.getProjectId(), "WORKFLOW_STATE", workflowCode + "|" + stateCode)))
			.collect(Collectors.toSet());
	}
}
