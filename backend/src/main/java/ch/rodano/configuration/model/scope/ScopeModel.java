package ch.rodano.configuration.model.scope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.cms.CMSLayout;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventGroup;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.payment.PayableModel;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.reports.WorkflowStatesSelector;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.configuration.model.workflow.WorkflowableModel;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class ScopeModel implements Serializable, SuperDisplayable, WorkflowableModel, PayableModel, Node, RightAssignable<ScopeModel> {
	private static final long serialVersionUID = -3652751984945692998L;

	public static final Comparator<ScopeModel> DEFAULT_COMPARATOR = Comparator.comparing(ScopeModel::getId);

	public static final Comparator<ScopeModel> COMPARATOR_DEPTH = (s1, s2) -> {
		final var s1Depth = s1.getDepth();
		final var s2Depth = s2.getDepth();
		if(s1Depth == s2Depth) {
			return s1.getId().compareTo(s2.getId());
		}
		return s1Depth - s2Depth;
	};

	private Study study;
	private String id;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;
	private SortedMap<String, String> pluralShortname;

	private String defaultParentId;
	private List<String> parentIds;
	private boolean virtual;

	private Integer maxNumber;
	private Integer expectedNumber;
	private String scopeFormat;

	private String defaultProfileId;

	private List<EventGroup> eventGroups;
	private List<EventModel> eventModels;

	private List<String> datasetModelIds;
	private List<String> formModelIds;
	private List<String> workflowIds;

	private List<WorkflowStatesSelector> workflowStatesSelectors;

	private CMSLayout layout;

	private List<Rule> createRules;
	private List<Rule> removeRules;
	private List<Rule> restoreRules;

	public ScopeModel() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		pluralShortname = new TreeMap<>();
		parentIds = new ArrayList<>();
		eventGroups = new ArrayList<>();
		eventModels = new ArrayList<>();
		workflowIds = new ArrayList<>();
		datasetModelIds = new ArrayList<>();
		formModelIds = new ArrayList<>();
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
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
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
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

	public final SortedMap<String, String> getPluralShortname() {
		return pluralShortname;
	}

	public final void setPluralShortname(final SortedMap<String, String> pluralShortname) {
		this.pluralShortname = pluralShortname;
	}

	@JsonIgnore
	public final String getLocalizedPluralShortname(final String... languages) {
		final var plural = DisplayableUtils.getLocalizedMap(pluralShortname, languages);
		if(plural == null || plural.isEmpty()) {
			return getLocalizedShortname(languages);
		}
		return plural;
	}

	@JsonIgnore
	public String getIdAndLocalizedShortname(final String... languages) {
		final var localizedShortname = getLocalizedShortname(languages);
		if(id.equals(localizedShortname)) {
			return localizedShortname;
		}
		return String.format("%s (%s)", id, localizedShortname);
	}

	public final String getDefaultParentId() {
		return defaultParentId;
	}

	public final void setDefaultParentId(final String defaultParentId) {
		this.defaultParentId = defaultParentId;
	}

	public final List<String> getParentIds() {
		return parentIds;
	}

	public final void setParentIds(final List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public final void setVirtual(final boolean virtual) {
		this.virtual = virtual;
	}

	public final boolean isVirtual() {
		return virtual;
	}

	public final Integer getMaxNumber() {
		return maxNumber;
	}

	public final void setMaxNumber(final Integer maxNumber) {
		this.maxNumber = maxNumber;
	}

	public final Integer getExpectedNumber() {
		return expectedNumber;
	}

	public final void setExpectedNumber(final Integer exptectedNumber) {
		expectedNumber = exptectedNumber;
	}

	@JsonManagedReference
	public void setEventModels(final List<EventModel> eventModels) {
		this.eventModels = eventModels;
	}

	@JsonManagedReference
	public List<EventModel> getEventModels() {
		return eventModels;
	}

	@JsonIgnore
	public EventModel getEventModel(final String eventModelId) {
		return eventModels.stream()
			.filter(e -> e.getId().equalsIgnoreCase(eventModelId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.EVENT_MODEL, eventModelId));
	}

	@JsonManagedReference
	public final void setEventGroups(final List<EventGroup> eventGroups) {
		this.eventGroups = eventGroups;
	}

	@JsonManagedReference
	public final List<EventGroup> getEventGroups() {
		return eventGroups;
	}

	@JsonIgnore
	public EventGroup getEventGroup(final String eventGroupId) {
		return eventGroups.stream()
			.filter(e -> e.getId().equalsIgnoreCase(eventGroupId))
			.findAny()
			.orElseThrow(() -> new NoNodeException(this, Entity.EVENT_GROUP, eventGroupId));
	}

	public final List<String> getDatasetModelIds() {
		return datasetModelIds;
	}

	public final void setDatasetModelIds(final List<String> datasetModelIds) {
		this.datasetModelIds = datasetModelIds;
	}

	public final List<String> getFormModelIds() {
		return formModelIds;
	}

	public final void setFormModelIds(final List<String> formModelIds) {
		this.formModelIds = formModelIds;
	}

	public final String getDefaultProfileId() {
		return defaultProfileId;
	}

	public final void setDefaultProfileId(final String defaultProfileId) {
		this.defaultProfileId = defaultProfileId;
	}

	public List<WorkflowStatesSelector> getWorkflowStatesSelectors() {
		return workflowStatesSelectors;
	}

	public void setWorkflowStatesSelectors(final List<WorkflowStatesSelector> workflowStatesSelectors) {
		this.workflowStatesSelectors = workflowStatesSelectors;
	}

	public final CMSLayout getLayout() {
		return layout;
	}

	public final void setLayout(final CMSLayout layout) {
		this.layout = layout;
	}

	public final String getScopeFormat() {
		return scopeFormat;
	}

	public final void setScopeFormat(final String scopeFormat) {
		this.scopeFormat = scopeFormat;
	}

	public List<Rule> getCreateRules() {
		return createRules;
	}

	public void setCreateRules(final List<Rule> createRules) {
		this.createRules = createRules;
	}

	public List<Rule> getRemoveRules() {
		return removeRules;
	}

	public void setRemoveRules(final List<Rule> removeRules) {
		this.removeRules = removeRules;
	}

	public List<Rule> getRestoreRules() {
		return restoreRules;
	}

	public void setRestoreRules(final List<Rule> restoreRules) {
		this.restoreRules = restoreRules;
	}

	@JsonIgnore
	public final boolean hasLayout() {
		return layout != null;
	}

	@JsonIgnore
	public ScopeModel getDefaultParent() {
		if(isRoot()) {
			throw new NoNodeException(Entity.SCOPE_MODEL, String.format("No default parent for root scope model %s", getId()));
		}
		return getStudy().getScopeModel(defaultParentId);
	}

	@JsonIgnore
	public List<ScopeModel> getDefaultAncestors() {
		if(isRoot()) {
			return new ArrayList<>();
		}
		final List<ScopeModel> ancestors = new ArrayList<>();
		final var defaultParent = getDefaultParent();
		ancestors.add(defaultParent);
		ancestors.addAll(defaultParent.getDefaultAncestors());
		return ancestors;
	}

	@JsonIgnore
	public List<ScopeModel> getScopeModelParents() {
		return getParentIds().stream().map(study::getScopeModel).toList();
	}

	@JsonIgnore
	public List<ScopeModel> getScopeModelAncestors() {
		final List<ScopeModel> ancestors = new ArrayList<>();
		getScopeModelParents().forEach(parent -> {
			ancestors.add(parent);
			ancestors.addAll(parent.getScopeModelAncestors());
		});
		return ancestors;
	}

	@JsonIgnore
	public List<ScopeModel> getChildrenScopeModel() {
		return getStudy().getScopeModels().stream()
			.filter(s -> s.getParentIds().contains(getId()))
			.toList();
	}

	@JsonIgnore
	public final List<ScopeModel> getDescendantsScopeModel() {
		final List<ScopeModel> descendants = new ArrayList<>();
		for(final var child : getChildrenScopeModel()) {
			descendants.add(child);
			descendants.addAll(child.getDescendantsScopeModel());
		}
		return descendants;
	}

	@JsonIgnore
	public final List<ScopeModel> getBranch(final ScopeModel model) {
		final List<ScopeModel> models = new ArrayList<>();
		models.add(this);

		if(getParentIds().isEmpty()) {
			throw new NoRespectForConfigurationException(String.format("%s and %s are not on the same branch", models.get(0).getId(), model.getId()));
		}

		if(getParentIds().contains(model.getId())) {
			models.add(model);
		}
		else {
			for(final var parent : getScopeModelParents()) {
				for(final var branch : parent.getBranch(model)) {
					if(!models.contains(branch)) {
						models.add(branch);
					}
				}
			}
		}
		return models;
	}

	@Override
	@JsonIgnore
	public final int compareTo(final ScopeModel otherScopeModel) {
		return DEFAULT_COMPARATOR.compare(this, otherScopeModel);
	}

	@JsonIgnore
	public final boolean isRoot() {
		return parentIds.isEmpty();
	}

	@JsonIgnore
	public final boolean isLeaf() {
		return getStudy().getScopeModels().stream().noneMatch(s -> s.parentIds.contains(id));
	}

	@JsonIgnore
	public final int getDepth() {
		if(isRoot()) {
			return 0;
		}
		return 1 + getDefaultParent().getDepth();
	}

	@JsonIgnore
	public final boolean isAncestorOf(final ScopeModel potentialDescendant) {
		return potentialDescendant.isDescendantOf(this);
	}

	@JsonIgnore
	public final boolean isDescendantOf(final ScopeModel potentialAncestor) {
		if(isChildOf(potentialAncestor)) {
			return true;
		}
		for(final var s : getScopeModelParents()) {
			if(s.isDescendantOf(potentialAncestor)) {
				return true;
			}
		}
		return false;
	}

	@JsonIgnore
	public final boolean isParentOf(final ScopeModel potentialChild) {
		return potentialChild.getParentIds().contains(getId());
	}

	@JsonIgnore
	public final boolean isChildOf(final ScopeModel potentialParent) {
		return getParentIds().contains(potentialParent.getId());
	}

	@JsonIgnore
	public final boolean isDescendantOf(final List<ScopeModel> potentialAncestors) {
		return potentialAncestors.stream()
			.anyMatch(this::isDescendantOf);
	}

	@JsonIgnore
	public List<DatasetModel> getDatasetModels() {
		return datasetModelIds.stream().map(study::getDatasetModel).toList();
	}

	@JsonIgnore
	public List<FormModel> getFormModels() {
		return study.getNodesFromIds(Entity.FORM_MODEL, formModelIds);
	}

	@Override
	public final List<String> getWorkflowIds() {
		return workflowIds;
	}

	public final void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	@Override
	@JsonIgnore
	public final List<Workflow> getWorkflows() {
		return getStudy().getNodesFromIds(Entity.WORKFLOW, getWorkflowIds());
	}

	@Override
	public final Entity getEntity() {
		return Entity.SCOPE_MODEL;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case SCOPE_MODEL:
				return Collections.unmodifiableList(getChildrenScopeModel());
			case EVENT_GROUP:
				return Collections.unmodifiableList(getEventGroups());
			case EVENT_MODEL:
				return Collections.unmodifiableList(getEventModels());
			default:
				return Collections.emptyList();
		}
	}

	@JsonIgnore
	public final List<EventModel> getInceptiveEventModels() {
		return getEventModels().stream()
			.filter(EventModel::isInceptive)
			.toList();
	}

	@JsonIgnore
	public Optional<Profile> getDefaultProfile() {
		return Optional.ofNullable(defaultProfileId).map(study::getProfile);
	}

	@JsonIgnore
	public void setDefaultProfile(final Profile profile) {
		setDefaultProfileId(profile == null ? null : profile.getId());
	}

	@JsonIgnore
	public SortedSet<TimelineGraph> getGraphConfigs() {
		return study.getTimelineGraphs().stream()
			.filter(conf -> conf.getScopeModelId().equals(getId()))
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@JsonIgnore
	public List<WorkflowState> getWorkflowStates() {
		final List<WorkflowState> states = new ArrayList<>();
		for(final WorkflowStatesSelector selector : workflowStatesSelectors) {
			final var workflow = study.getWorkflow(selector.getWorkflowId());
			selector.getStateIds().stream().map(workflow::getState).forEach(states::add);
		}
		return states;
	}

	@Override
	@JsonIgnore
	public String getAssignableDescription() {
		return getDefaultLocalizedShortname();
	}

	@JsonIgnore
	public final String getDefaultLocalizedShortname() {
		return getLocalizedShortname(study.getDefaultLanguage().getId());
	}
}
