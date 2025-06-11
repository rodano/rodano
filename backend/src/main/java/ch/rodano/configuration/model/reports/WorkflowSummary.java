package ch.rodano.configuration.model.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.utils.DisplayableUtils;

public class WorkflowSummary implements Node, Comparable<WorkflowSummary> {
	private static final long serialVersionUID = 5262106614046834382L;

	private Study study;
	private String id;

	//workflow
	private WorkflowableEntity workflowEntity;
	private List<String> workflowIds;
	private List<WorkflowSummaryColumn> columns;
	//scope model used as leaf scope in report
	private String leafScopeModelId;

	//filters
	private SortedSet<String> filterEventModelIds;
	private boolean filterExpectedEvents;

	//display
	private SortedMap<String, String> title;

	private boolean displayLegend;
	private boolean displayIcon;
	private boolean displayColumnExport;

	public WorkflowSummary() {
		columns = new ArrayList<>();
		filterEventModelIds = new TreeSet<>();
		title = new TreeMap<>();
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public WorkflowableEntity getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(final WorkflowableEntity workflowEntity) {
		this.workflowEntity = workflowEntity;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public final List<WorkflowSummaryColumn> getColumns() {
		return columns;
	}

	public final void setColumns(final List<WorkflowSummaryColumn> columns) {
		this.columns = columns;
	}

	public final String getLeafScopeModelId() {
		return leafScopeModelId;
	}

	public final void setLeafScopeModelId(final String leafScopeModelId) {
		this.leafScopeModelId = leafScopeModelId;
	}

	public SortedSet<String> getFilterEventModelIds() {
		return filterEventModelIds;
	}

	public void setFilterEventModelIds(final SortedSet<String> filterEventIds) {
		this.filterEventModelIds = filterEventIds;
	}

	public boolean isFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final boolean filterExpectedVisits) {
		this.filterExpectedEvents = filterExpectedVisits;
	}

	public final SortedMap<String, String> getTitle() {
		return title;
	}

	public final void setTitle(final SortedMap<String, String> title) {
		this.title = title;
	}

	public final boolean getDisplayColumnExport() {
		return displayColumnExport;
	}

	public final void setDisplayColumnExport(final boolean displayColumnExport) {
		this.displayColumnExport = displayColumnExport;
	}

	public final boolean getDisplayIcon() {
		return displayIcon;
	}

	public final void setDisplayIcon(final boolean displayIcon) {
		this.displayIcon = displayIcon;
	}

	public final boolean getDisplayLegend() {
		return displayLegend;
	}

	public final void setDisplayLegend(final boolean displayLegend) {
		this.displayLegend = displayLegend;
	}

	@JsonIgnore
	public List<Workflow> getWorkflows() {
		return workflowIds.stream().map(study::getWorkflow).toList();
	}

	@JsonIgnore
	public boolean isValid() {
		final var workflows = getWorkflows();
		//multiple workflows is only supported for non aggregate workflows
		return workflows.size() == 1 || workflows.stream().allMatch(Predicate.not(Workflow::isAggregator));
	}

	@JsonIgnore
	public final String getLocalizedTitle(final String... languages) {
		return DisplayableUtils.getLocalizedMap(title, languages);
	}

	@JsonIgnore
	public ScopeModel getLeafScopeModel() {
		return study.getScopeModel(leafScopeModelId);
	}

	@JsonIgnore
	public List<EventModel> getFilterEventModels() {
		return study.getNodesFromIds(Entity.EVENT_MODEL, filterEventModelIds);
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW_SUMMARY;
	}

	@Override
	public int compareTo(final WorkflowSummary o) {
		return id.compareTo(o.id);
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
