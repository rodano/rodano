package ch.rodano.configuration.model.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Workflow implements SuperDisplayable, Serializable, Attributable<Workflow>, Node {
	private static final long serialVersionUID = -2909356306271744756L;

	private static final Comparator<Workflow> DEFAULT_COMPARATOR = Comparator.comparing(Workflow::getId);

	public static Comparator<Workflow> getWorkflowableComparator(final WorkflowableModel workflowable) {
		final var workflows = workflowable.getWorkflowIds();
		return (w1, w2) -> {
			final var w1Index = workflows.indexOf(w1.getId());
			final var w2Index = workflows.indexOf(w2.getId());
			if(w1Index != -1 && w2Index != -1) {
				return w1Index - w2Index;
			}
			return DEFAULT_COMPARATOR.compare(w1, w2);
		};
	}

	private String id;
	private Study study;

	private Integer orderBy;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private List<WorkflowState> states;
	private SortedSet<Action> actions;
	private List<Rule> rules;

	private String initialStateId;
	private boolean mandatory;
	private String actionId;
	private boolean unique;

	private String aggregateWorkflowId;

	private SortedMap<String, String> message;
	private String icon;

	public Workflow() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		states = new ArrayList<>();
		actions = new TreeSet<>();
		mandatory = true;
		message = new TreeMap<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	public final Integer getOrderBy() {
		return orderBy;
	}

	public final void setOrderBy(final Integer orderBy) {
		this.orderBy = orderBy;
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

	public void setLongname(final SortedMap<String, String> longname) {
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
	public List<WorkflowState> getStates() {
		return states;
	}

	@JsonManagedReference
	public void setStates(final List<WorkflowState> states) {
		this.states = states;
	}

	@JsonManagedReference
	public void setActions(final SortedSet<Action> actions) {
		this.actions = Collections.unmodifiableSortedSet(actions);
	}

	@JsonManagedReference
	public SortedSet<Action> getActions() {
		return actions;
	}

	public final List<Rule> getRules() {
		return rules;
	}

	public final void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@JsonIgnore
	public Action getAction(final String containedActionId) {
		return getActions().stream()
			.filter(a -> a.getId().equals(containedActionId))
			.findFirst()
			.orElseThrow(() -> new NoNodeException(this, Entity.ACTION, containedActionId));
	}

	public final String getInitialStateId() {
		return initialStateId;
	}

	public final void setInitialStateId(final String initialStateId) {
		this.initialStateId = initialStateId;
	}

	public final boolean isMandatory() {
		return mandatory;
	}

	public final void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public final String getActionId() {
		return actionId;
	}

	public final void setActionId(final String actionId) {
		this.actionId = actionId;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@JsonIgnore
	public final boolean isAggregator() {
		return StringUtils.isNotBlank(aggregateWorkflowId);
	}

	public final String getAggregateWorkflowId() {
		return aggregateWorkflowId;
	}

	public final void setAggregateWorkflowId(final String basedOnWorkflowId) {
		this.aggregateWorkflowId = basedOnWorkflowId;
	}

	public final SortedMap<String, String> getMessage() {
		return message;
	}

	public final void setMessage(final SortedMap<String, String> message) {
		this.message = message;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW;
	}

	@JsonIgnore
	public WorkflowState getState(final String stateId) {
		return getStates().stream()
			.filter(s -> s.getId().equals(stateId))
			.findFirst()
			.orElseThrow(() -> new NoNodeException(this, Entity.WORKFLOW_STATE, stateId));
	}

	@JsonIgnore
	public WorkflowState getInitialState() {
		if(StringUtils.isNotBlank(initialStateId)) {
			return getState(initialStateId);
		}
		throw new NoRespectForConfigurationException(String.format("No initial state for workflow [%s]", id));
	}

	@JsonIgnore
	public List<WorkflowState> getStatesImportant() {
		return states.stream().filter(WorkflowState::isImportant).toList();
	}

	@JsonIgnore
	public List<WorkflowState> getStatesHavingMatcher(final StateMatcher matcher) {
		return states.stream().filter(s -> matcher.equals(s.getAggregateStateMatcher())).toList();
	}

	@JsonIgnore
	public boolean hasCreationAction() {
		return StringUtils.isNotBlank(actionId);
	}

	@JsonIgnore
	public Action getAction() {
		return getAction(actionId);
	}

	@JsonIgnore
	public Map<Entity, List<WorkflowableModel>> getWorkflowableModelsByEntity() {
		final Predicate<WorkflowableModel> hasWorkflow = w -> w.getWorkflowIds().contains(id);
		return Map.of(
			Entity.SCOPE_MODEL, study.getScopeModels().stream().filter(hasWorkflow).collect(Collectors.toList()),
			Entity.EVENT_MODEL, study.getEventModels().stream().filter(hasWorkflow).collect(Collectors.toList()),
			Entity.FORM_MODEL, study.getFormModels().stream().filter(hasWorkflow).collect(Collectors.toList()),
			Entity.FIELD_MODEL, study.getFieldModels().stream().filter(hasWorkflow).collect(Collectors.toList())
			);
	}

	@JsonIgnore
	public List<WorkflowableModel> getWorkflowableModels() {
		final List<WorkflowableModel> workflowables = new ArrayList<>();
		getWorkflowableModelsByEntity().values().forEach(workflowables::addAll);
		return workflowables;
	}

	@JsonIgnore
	public List<Entity> getWorkflowableEntities() {
		return getWorkflowableModelsByEntity().entrySet().stream().filter(e -> !e.getValue().isEmpty()).map(Entry::getKey).toList();
	}

	@JsonIgnore
	public List<PaymentPlan> getPaymentPlans() {
		return study.getPaymentPlans().stream().filter(plan -> plan.getWorkflow().equals(id)).toList();
	}

	@Override
	@JsonIgnore
	public int compareTo(final Workflow otherWorkflow) {
		return DEFAULT_COMPARATOR.compare(this, otherWorkflow);
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case WORKFLOW_STATE:
				return Collections.unmodifiableList(states);
			case ACTION:
				return Collections.unmodifiableSet(actions);
			default:
				return Collections.emptyList();
		}
	}
}
