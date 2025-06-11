package ch.rodano.configuration.model.event;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowableModel;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class EventModel implements Serializable, SuperDisplayable, WorkflowableModel, RightAssignable<EventModel>, Node, Comparable<EventModel> {
	private static final long serialVersionUID = -5437648376925328189L;

	private static Comparator<EventModel> DEFAULT_COMPARATOR = Comparator
		.comparing(EventModel::getNumber)
		.thenComparing(EventModel::getId);

	private ScopeModel scopeModel;

	private String id;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private String eventGroupId;
	private boolean inceptive;
	private int number;

	private boolean mandatory;
	private int maxOccurrence;
	private boolean preventAdd;

	private List<String> datasetModelIds;
	private List<String> formModelIds;
	private List<String> workflowIds;

	private Set<String> impliedEventModelIds;
	private Set<String> blockedEventModelIds;

	private Integer deadline;
	private ChronoUnit deadlineUnit;
	private List<String> deadlineReferenceEventModelIds;
	private DateAggregationFunction deadlineAggregationFunction;

	private Integer interval;
	private ChronoUnit intervalUnit;

	private String labelPattern;
	private String icon;

	private RuleConstraint constraint;

	private List<Rule> createRules;
	private List<Rule> removeRules;
	private List<Rule> restoreRules;

	public EventModel() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		datasetModelIds = new ArrayList<>();
		formModelIds = new ArrayList<>();
		workflowIds = new ArrayList<>();
		impliedEventModelIds = new TreeSet<>();
		blockedEventModelIds = new TreeSet<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public void setScopeModel(final ScopeModel scopeModel) {
		this.scopeModel = scopeModel;
	}

	@JsonBackReference
	public final ScopeModel getScopeModel() {
		return scopeModel;
	}

	@JsonIgnore
	public Study getStudy() {
		return scopeModel.getStudy();
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final Map<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final Map<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public String getEventGroupId() {
		return eventGroupId;
	}

	public void setEventGroupId(final String eventGroupId) {
		this.eventGroupId = eventGroupId;
	}

	public final boolean isInceptive() {
		return inceptive;
	}

	public final void setInceptive(final boolean inceptive) {
		this.inceptive = inceptive;
	}

	public Set<String> getImpliedEventModelIds() {
		return impliedEventModelIds;
	}

	public void setImpliedEventModelIds(final Set<String> impliedEventIds) {
		this.impliedEventModelIds = impliedEventIds;
	}

	@JsonIgnore
	public List<EventModel> getImpliedEventModels() {
		return impliedEventModelIds.stream().map(scopeModel::getEventModel).toList();
	}

	@JsonIgnore
	public List<EventModel> getImplyingEventModels() {
		return scopeModel.getEventModels().stream().filter(e -> e.getImpliedEventModelIds().contains(id)).toList();
	}

	public Set<String> getBlockedEventModelIds() {
		return blockedEventModelIds;
	}

	public void setBlockedEventModelIds(final Set<String> blockedEventIds) {
		this.blockedEventModelIds = blockedEventIds;
	}

	@JsonIgnore
	public List<EventModel> getBlockedEventModels() {
		return blockedEventModelIds.stream().map(scopeModel::getEventModel).toList();
	}

	@JsonIgnore
	public List<EventModel> getBlockingEventModels() {
		return scopeModel.getEventModels().stream().filter(e -> e.getBlockedEventModelIds().contains(id)).toList();
	}

	@JsonIgnore
	public final boolean hasEventGroup() {
		return StringUtils.isNotBlank(eventGroupId);
	}

	@JsonIgnore
	public final EventGroup getEventGroup() {
		return scopeModel.getEventGroup(eventGroupId);
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isPreventAdd() {
		return preventAdd;
	}

	public void setPreventAdd(final boolean preventAdd) {
		this.preventAdd = preventAdd;
	}

	public int getMaxOccurrence() {
		return maxOccurrence;
	}

	public void setMaxOccurrence(final int maxOccurrence) {
		this.maxOccurrence = maxOccurrence;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@JsonIgnore
	public final String getDefaultLocalizedLongname() {
		return getLocalizedLongname(getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(getStudy().getDefaultLanguage().getId());
	}

	public final int getNumber() {
		return number;
	}

	public final void setNumber(final int number) {
		this.number = number;
	}

	public final List<Rule> getCreateRules() {
		return createRules;
	}

	public final void setCreateRules(final List<Rule> createRules) {
		this.createRules = createRules;
	}

	public final List<Rule> getRestoreRules() {
		return restoreRules;
	}

	public final void setRestoreRules(final List<Rule> restoreRules) {
		this.restoreRules = restoreRules;
	}

	public final List<Rule> getRemoveRules() {
		return removeRules;
	}

	public final void setRemoveRules(final List<Rule> removeRules) {
		this.removeRules = removeRules;
	}

	public final Integer getDeadline() {
		return deadline;
	}

	public final void setDeadline(final Integer deadline) {
		this.deadline = deadline;
	}

	public final ChronoUnit getDeadlineUnit() {
		return deadlineUnit;
	}

	public final void setDeadlineUnit(final ChronoUnit deadlineUnit) {
		this.deadlineUnit = deadlineUnit;
	}

	public List<String> getDeadlineReferenceEventModelIds() {
		return deadlineReferenceEventModelIds;
	}

	public void setDeadlineReferenceEventModelIds(final List<String> deadlineReferenceEventIds) {
		this.deadlineReferenceEventModelIds = deadlineReferenceEventIds;
	}

	public DateAggregationFunction getDeadlineAggregationFunction() {
		return deadlineAggregationFunction;
	}

	public DateAggregationFunction getDeadlineAggregationFunctionOrDefault() {
		return deadlineAggregationFunction != null ? deadlineAggregationFunction : DateAggregationFunction.MAX;
	}

	public void setDeadlineAggregationFunction(final DateAggregationFunction deadlineAggregationFunction) {
		this.deadlineAggregationFunction = deadlineAggregationFunction;
	}

	@JsonIgnore
	public final boolean isPlanned() {
		return deadline != null && deadlineUnit != null && CollectionUtils.isNotEmpty(deadlineReferenceEventModelIds) && deadlineAggregationFunction != null;
	}

	@JsonIgnore
	public final Collection<EventModel> getDeadlineReferenceEventModels() {
		if(!isPlanned()) {
			throw new NoRespectForConfigurationException("There is no reference event for an event which has not been planned");
		}
		return deadlineReferenceEventModelIds.stream().map(scopeModel::getEventModel).toList();
	}

	public final Integer getInterval() {
		return interval;
	}

	public final void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public final ChronoUnit getIntervalUnit() {
		return intervalUnit;
	}

	public final void setIntervalUnit(final ChronoUnit intervalUnit) {
		this.intervalUnit = intervalUnit;
	}

	@JsonIgnore
	public final boolean hasInterval() {
		return interval != null && intervalUnit != null;
	}

	public final String getLabelPattern() {
		return labelPattern;
	}

	public final void setLabelPattern(final String labelPattern) {
		this.labelPattern = labelPattern;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	@JsonIgnore
	public final long getDeadlineInMilliSeconds() {
		if(isPlanned()) {
			return getDeadline() * getDeadlineUnit().getDuration().toMillis();
		}
		return 0;
	}

	public final List<String> getDatasetModelIds() {
		return datasetModelIds;
	}

	public final void setDatasetModelIds(final List<String> datasetModelIds) {
		this.datasetModelIds = datasetModelIds;
	}

	@JsonIgnore
	public List<DatasetModel> getDatasetModels() {
		return getStudy().getNodesFromIds(Entity.DATASET_MODEL, datasetModelIds);
	}

	public final List<String> getFormModelIds() {
		return formModelIds;
	}

	public final void setFormModelIds(final List<String> formModelIds) {
		this.formModelIds = formModelIds;
	}

	@JsonIgnore
	public List<FormModel> getFormModels() {
		return getStudy().getNodesFromIds(Entity.FORM_MODEL, formModelIds);
	}

	@JsonIgnore
	public boolean hasFamilyDatasetModels() {
		return getDatasetModels().stream().anyMatch(DatasetModel::isInFamily);
	}

	//workflows
	@Override
	public final List<String> getWorkflowIds() {
		return workflowIds;
	}

	public final void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	@JsonIgnore
	@Override
	public final List<Workflow> getWorkflows() {
		return getStudy().getNodesFromIds(Entity.WORKFLOW, workflowIds);
	}

	@JsonIgnore
	@Override
	public final int compareTo(final EventModel otherEventModel) {
		return DEFAULT_COMPARATOR.compare(this, otherEventModel);
	}

	@Override
	public final Entity getEntity() {
		return Entity.EVENT_MODEL;
	}

	@Override
	@JsonIgnore
	public final String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	//node
	@JsonIgnore
	@Override
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case DATASET_MODEL:
				return Collections.unmodifiableList(getDatasetModels());
			case FORM_MODEL:
				return Collections.unmodifiableList(getFormModels());
			case WORKFLOW:
				return Collections.unmodifiableList(getWorkflows());
			default:
				return Collections.emptyList();
		}
	}
}
