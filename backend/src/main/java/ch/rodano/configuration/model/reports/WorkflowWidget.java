package ch.rodano.configuration.model.reports;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableEntity;
import ch.rodano.configuration.utils.DisplayableUtils;

public class WorkflowWidget implements Serializable, Node, Comparable<WorkflowWidget> {
	@Serial
	private static final long serialVersionUID = 6618695584561912753L;

	private Study study;

	private String id;
	private Map<String, String> label = new HashMap<>();

	private boolean filterExpectedEvents;

	private WorkflowableEntity workflowEntity;
	private List<WorkflowStatesSelector> workflowStatesSelectors;

	private List<WorkflowWidgetColumn> columns = new ArrayList<>();

	@JsonBackReference
	public void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public Study getStudy() {
		return study;
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	@JsonIgnore
	public String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(label, languages);
	}

	@JsonManagedReference
	public final List<WorkflowWidgetColumn> getColumns() {
		return columns;
	}

	@JsonManagedReference
	public final void setColumns(final List<WorkflowWidgetColumn> columns) {
		this.columns = columns;
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
		return id.compareTo(o.id);
	}
}
