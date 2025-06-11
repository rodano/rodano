package ch.rodano.configuration.model.reports;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class WorkflowWidget implements SuperDisplayable, Serializable, Node, Comparable<WorkflowWidget> {
	private static final long serialVersionUID = 6618695584561912753L;

	private Study study;

	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private boolean filterExpectedEvents;

	private WorkflowableEntity workflowEntity;
	private List<WorkflowStatesSelector> workflowStatesSelectors;

	private List<WorkflowWidgetColumn> columns;

	public WorkflowWidget() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		columns = new ArrayList<>();
	}

	@JsonBackReference
	public void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public Study getStudy() {
		return study;
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final Map<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@JsonManagedReference
	public final List<WorkflowWidgetColumn> getColumns() {
		return columns;
	}

	@JsonManagedReference
	public final void setColumns(final List<WorkflowWidgetColumn> columns) {
		this.columns = columns;
	}

	@JsonIgnore
	public final WorkflowWidgetColumn getColumn(final String columnId) {
		return columns.stream()
			.filter(c -> c.getId().equals(columnId))
			.findFirst()
			.orElseThrow(() -> new NoNodeException(this, Entity.WORKFLOW_WIDGET_COLUMN, id));
	}

	public boolean isFilterExpectedEvents() {
		return filterExpectedEvents;
	}

	public void setFilterExpectedEvents(final boolean filterExpectedVisits) {
		this.filterExpectedEvents = filterExpectedVisits;
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

	public void setWorkflowStatesSelectors(final List<WorkflowStatesSelector> workflowStatesFilters) {
		this.workflowStatesSelectors = workflowStatesFilters;
	}

	@JsonIgnore
	public List<Workflow> getWorkflows() {
		return workflowStatesSelectors.stream().map(WorkflowStatesSelector::getWorkflowId).map(study::getWorkflow).toList();
	}

	@JsonIgnore
	public boolean isValid() {
		final var workflows = getWorkflows();
		//multiple workflows is only supported for non aggregate workflows
		return workflows.size() == 1 || workflows.stream().allMatch(Predicate.not(Workflow::isAggregator));
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW_WIDGET;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case WORKFLOW_WIDGET_COLUMN:
				return Collections.unmodifiableList(columns);
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public final int compareTo(final WorkflowWidget o) {
		return getId().compareTo(o.getId());
	}
}
