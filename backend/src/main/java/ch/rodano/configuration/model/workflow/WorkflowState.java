package ch.rodano.configuration.model.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class WorkflowState implements Serializable, SuperDisplayable, Node, Comparable<WorkflowState> {
	private static final long serialVersionUID = 8285922897928637570L;

	public static final Comparator<WorkflowState> COMPARATOR_INDEX = (o1, o2) -> {
		if(o1.getWorkflow().equals(o2.getWorkflow())) {
			return o1.getWorkflow().getStates().indexOf(o1) - o1.getWorkflow().getStates().indexOf(o2);
		}
		return o1.getWorkflow().compareTo(o2.getWorkflow());
	};

	public static final Comparator<WorkflowState> COMPARATOR_IMPORTANCE = (o1, o2) -> {
		if(o1.isImportant() && !o2.isImportant()) {
			return -1;
		}
		if(o2.isImportant() && !o1.isImportant()) {
			return 1;
		}
		return o1.getId().compareTo(o2.getId());
	};

	private String id;

	private Workflow workflow;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String aggregateStateId;
	private StateMatcher aggregateStateMatcher;

	private List<String> possibleActionIds;

	private boolean important;
	private String icon;
	private String color;

	public WorkflowState() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		possibleActionIds = new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
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

	@JsonBackReference
	public final void setWorkflow(final Workflow workflow) {
		this.workflow = workflow;
	}

	@JsonBackReference
	public final Workflow getWorkflow() {
		return workflow;
	}

	public final boolean isImportant() {
		return important;
	}

	public final void setImportant(final boolean important) {
		this.important = important;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
	}

	public final String getColor() {
		return color;
	}

	public final void setColor(final String color) {
		this.color = color;
	}

	public final String getAggregateStateId() {
		return aggregateStateId;
	}

	public final void setAggregateStateId(final String basedOnStateId) {
		this.aggregateStateId = basedOnStateId;
	}

	public final StateMatcher getAggregateStateMatcher() {
		return aggregateStateMatcher;
	}

	public final void setAggregateStateMatcher(final StateMatcher stateMatcher) {
		this.aggregateStateMatcher = stateMatcher;
	}

	public List<String> getPossibleActionIds() {
		return possibleActionIds;
	}

	public void setPossibleActionIds(final List<String> possibleActionIds) {
		this.possibleActionIds = possibleActionIds;
	}

	@JsonIgnore
	public final List<Action> getPossibleActions() {
		return possibleActionIds.stream().map(workflow::getAction).toList();
	}

	@JsonIgnore
	public static List<String> getIdsFromStates(final Collection<WorkflowState> states) {
		return states.stream().filter(Objects::nonNull).map(WorkflowState::getId).toList();
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW_STATE;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		if(Entity.ACTION.equals(entity)) {
			return new ArrayList<>(getPossibleActions());
		}
		return Collections.emptyList();
	}

	@Override
	public final int compareTo(final WorkflowState otherWorkflowState) {
		return COMPARATOR_INDEX.compare(this, otherWorkflowState);
	}
}
