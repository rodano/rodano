package ch.rodano.api.workflow;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.common.PaginatedSearch;
import ch.rodano.core.model.workflow.WorkflowStatusSortBy;

public class WorkflowStatusSearch extends PaginatedSearch<WorkflowStatusSearch> {
	public static final WorkflowStatusSortBy DEFAULT_SORT_BY = WorkflowStatusSortBy.workflowId;
	public static final boolean DEFAULT_SORT_ASCENDING = true;

	@Schema(description = "Ancestor scope pks")
	private List<Long> ancestorScopePks;

	@Schema(description = "Workflow ids")
	private List<String> workflowIds;

	@Schema(description = "State ids")
	private Optional<List<String>> stateIds = Optional.empty();

	@Schema(description = "Scope pks")
	private Optional<List<Long>> scopePks = Optional.empty();

	@Schema(description = "Event pks")
	private Optional<List<Long>> eventPks = Optional.empty();

	@Schema(description = "Text search")
	private Optional<String> fullText = Optional.empty();

	private Optional<Boolean> filterExpectedEvents = Optional.empty();

	public WorkflowStatusSortBy sortBy = DEFAULT_SORT_BY;

	public WorkflowStatusSearch() {
		super();
		sortAscending = DEFAULT_SORT_ASCENDING;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public WorkflowStatusSearch setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
		return this;
	}

	public Optional<List<String>> getStateIds() {
		return stateIds;
	}

	public WorkflowStatusSearch setStateIds(final Optional<List<String>> stateIds) {
		this.stateIds = stateIds;
		return this;
	}

	public Optional<List<Long>> getScopePks() {
		return scopePks;
	}

	public WorkflowStatusSearch setScopePks(final Optional<List<Long>> scopePks) {
		this.scopePks = scopePks;
		return this;
	}

	public Optional<List<Long>> getEventPks() {
		return eventPks;
	}

	public WorkflowStatusSearch setEventPks(final Optional<List<Long>> eventPks) {
		this.eventPks = eventPks;
		return this;
	}

	public Optional<String> getFullText() {
		return fullText;
	}

	public WorkflowStatusSearch setFullText(final Optional<String> fullText) {
		this.fullText = fullText;
		return this;
	}

	public Optional<Boolean> getFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public WorkflowStatusSearch setFilterExpectedEvents(final Optional<Boolean> filterExpectedEvents) {
		this.filterExpectedEvents = filterExpectedEvents;
		return this;
	}

	public WorkflowStatusSortBy getSortBy() {
		return sortBy;
	}

	public WorkflowStatusSearch setSortBy(final WorkflowStatusSortBy sortBy) {
		this.sortBy = sortBy;
		return this;
	}

	public List<Long> getAncestorScopePks() {
		return ancestorScopePks;
	}

	public WorkflowStatusSearch setAncestorScopePks(final List<Long> ancestorScopePks) {
		this.ancestorScopePks = ancestorScopePks;
		return this;
	}
}
