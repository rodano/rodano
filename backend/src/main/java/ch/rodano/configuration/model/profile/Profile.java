package ch.rodano.configuration.model.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.configuration.model.payment.PayableModel;
import ch.rodano.configuration.model.policy.PrivacyPolicy;
import ch.rodano.configuration.model.reports.Report;
import ch.rodano.configuration.model.resource.ResourceCategory;
import ch.rodano.configuration.model.rights.Assignable;
import ch.rodano.configuration.model.rights.Attributable;
import ch.rodano.configuration.model.rights.ProfileRightAssignable;
import ch.rodano.configuration.model.rights.Right;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.configuration.model.workflow.Workflow;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Profile implements SuperDisplayable, Payable, PayableModel, Node, RightAssignable<Profile> {
	private static final long serialVersionUID = -213015124204792366L;

	private static final Comparator<Profile> COMPARATOR_ID = Comparator.comparing(Profile::getId);

	private static final Comparator<Profile> COMPARATOR_GLOBAL_ORDER = (p1, p2) -> {
		if(p1.getOrderBy() != null && p2.getOrderBy() != null) {
			if(!p1.getOrderBy().equals(p2.getOrderBy())) {
				return p1.getOrderBy().compareTo(p2.getOrderBy());
			}

			return COMPARATOR_ID.compare(p1, p2);
		}

		if(p1.getOrderBy() != null) {
			return -1;
		}

		if(p2.getOrderBy() != null) {
			return 1;
		}

		return COMPARATOR_ID.compare(p1, p2);
	};

	private String id;
	private Study study;

	private Integer orderBy;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	@Deprecated
	private String workflowIdOfInterest;

	//assignables
	private SortedSet<String> grantedFeatureIds;
	private SortedSet<String> grantedReportIds;
	private SortedSet<String> grantedMenuIds;
	private SortedSet<String> grantedCategoryIds;
	private SortedSet<String> grantedTimelineGraphIds;

	//right assignables
	private Map<String, Set<Rights>> grantedProfileIdRights;
	private Map<String, Set<Rights>> grantedScopeModelIdRights;
	private Map<String, Set<Rights>> grantedPaymentIdRights;
	private Map<String, Set<Rights>> grantedDatasetModelIdRights;
	private Map<String, Set<Rights>> grantedEventModelIdRights;
	private Map<String, Set<Rights>> grantedFormModelIdRights;

	//attributables
	private SortedMap<String, Right> grantedWorkflowIds;

	public Profile() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		grantedFeatureIds = new TreeSet<>();
		grantedReportIds = new TreeSet<>();
		grantedMenuIds = new TreeSet<>();
		grantedFormModelIdRights = new TreeMap<>();
		grantedCategoryIds = new TreeSet<>();
		grantedTimelineGraphIds = new TreeSet<>();
		grantedProfileIdRights = new TreeMap<>();
		grantedScopeModelIdRights = new TreeMap<>();
		grantedPaymentIdRights = new TreeMap<>();
		grantedDatasetModelIdRights = new TreeMap<>();
		grantedEventModelIdRights = new TreeMap<>();
		grantedWorkflowIds = new TreeMap<>();
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
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public String getWorkflowIdOfInterest() {
		return workflowIdOfInterest;
	}

	public void setWorkflowIdOfInterest(final String workflowIdOfInterest) {
		this.workflowIdOfInterest = workflowIdOfInterest;
	}

	public final Integer getOrderBy() {
		return orderBy;
	}

	public final void setOrderBy(final Integer orderBy) {
		this.orderBy = orderBy;
	}

	public final void setGrantedFeatureIds(final SortedSet<String> grantedFeatureIds) {
		this.grantedFeatureIds = grantedFeatureIds;
	}

	public final SortedSet<String> getGrantedFeatureIds() {
		return grantedFeatureIds;
	}

	public final void setGrantedMenuIds(final SortedSet<String> grantedMenuIds) {
		this.grantedMenuIds = grantedMenuIds;
	}

	public final SortedSet<String> getGrantedMenuIds() {
		return grantedMenuIds;
	}

	public final void setGrantedCategoryIds(final SortedSet<String> grantedCategoryIds) {
		this.grantedCategoryIds = grantedCategoryIds;
	}

	public final SortedSet<String> getGrantedCategoryIds() {
		return grantedCategoryIds;
	}

	public final SortedSet<String> getGrantedTimelineGraphIds() {
		return grantedTimelineGraphIds;
	}

	public final void setGrantedTimelineGraphIds(final SortedSet<String> grantedTimelineGraphIds) {
		this.grantedTimelineGraphIds = grantedTimelineGraphIds;
	}

	public final void setGrantedReportIds(final SortedSet<String> grantedReportIds) {
		this.grantedReportIds = grantedReportIds;
	}

	public final SortedSet<String> getGrantedReportIds() {
		return grantedReportIds;
	}

	public Map<String, Set<Rights>> getGrantedPaymentIdRights() {
		return grantedPaymentIdRights;
	}

	public void setGrantedPaymentIdRights(final Map<String, Set<Rights>> grantedPaymentIdRights) {
		this.grantedPaymentIdRights = grantedPaymentIdRights;
	}

	public final Map<String, Set<Rights>> getGrantedProfileIdRights() {
		return grantedProfileIdRights;
	}

	public final void setGrantedProfileIdRights(final Map<String, Set<Rights>> grantedProfileIdRights) {
		this.grantedProfileIdRights = grantedProfileIdRights;
	}

	public final Map<String, Set<Rights>> getGrantedScopeModelIdRights() {
		return grantedScopeModelIdRights;
	}

	public final void setGrantedScopeModelIdRights(final Map<String, Set<Rights>> grantedScopeModelIdRights) {
		this.grantedScopeModelIdRights = grantedScopeModelIdRights;
	}

	public final Map<String, Set<Rights>> getGrantedDatasetModelIdRights() {
		return grantedDatasetModelIdRights;
	}

	public final void setGrantedDatasetModelIdRights(final Map<String, Set<Rights>> grantedDatasetModelIdRights) {
		this.grantedDatasetModelIdRights = grantedDatasetModelIdRights;
	}

	public Map<String, Set<Rights>> getGrantedFormModelIdRights() {
		return grantedFormModelIdRights;
	}

	public void setGrantedFormModelIdRights(final Map<String, Set<Rights>> grantedFormModelIdRights) {
		this.grantedFormModelIdRights = grantedFormModelIdRights;
	}

	public Map<String, Set<Rights>> getGrantedEventModelIdRights() {
		return grantedEventModelIdRights;
	}

	public void setGrantedEventModelIdRights(final Map<String, Set<Rights>> grantedEventIdRights) {
		this.grantedEventModelIdRights = grantedEventIdRights;
	}

	public final SortedMap<String, Right> getGrantedWorkflowIds() {
		return grantedWorkflowIds;
	}

	public final void setGrantedWorkflowIds(final SortedMap<String, Right> grantedWorkflowIds) {
		this.grantedWorkflowIds = grantedWorkflowIds;
	}

	@JsonIgnore
	public final List<Feature> getFeatures() {
		return study.getNodesFromIds(Entity.FEATURE, grantedFeatureIds);
	}

	@JsonIgnore
	public final List<TimelineGraph> getGraphConfigs() {
		return study.getNodesFromIds(Entity.TIMELINE_GRAPH, grantedTimelineGraphIds);
	}

	@JsonIgnore
	public final List<ResourceCategory> getResourceCategories() {
		return study.getNodesFromIds(Entity.RESOURCE_CATEGORY, grantedCategoryIds);
	}

	@JsonIgnore
	public final List<Report> getReports() {
		return study.getNodesFromIds(Entity.REPORT, grantedReportIds);
	}


	private <T> List<T> getNodes(final Entity entity, final Map<String, Set<Rights>> rights, final Rights right) {
		final var nodeIds = rights.entrySet().stream()
			.filter(e -> e.getValue().contains(right))
			.map(Entry::getKey)
			.toList();
		return study.getNodesFromIds(entity, nodeIds);
	}

	@JsonIgnore
	public final List<ScopeModel> getScopeModels(final Rights right) {
		return getNodes(Entity.SCOPE_MODEL, grantedScopeModelIdRights, right);
	}

	@JsonIgnore
	public final List<EventModel> getEventModels(final Rights right) {
		return getNodes(Entity.EVENT_MODEL, grantedEventModelIdRights, right);
	}

	@JsonIgnore
	public final List<DatasetModel> getDatasetModels(final Rights right) {
		return getNodes(Entity.DATASET_MODEL, grantedDatasetModelIdRights, right);
	}

	@JsonIgnore
	public final List<FormModel> getFormModels(final Rights right) {
		return getNodes(Entity.FORM_MODEL, grantedFormModelIdRights, right);
	}

	@JsonIgnore
	public final List<Profile> getProfiles(final Rights right) {
		return getNodes(Entity.PROFILE, grantedProfileIdRights, right);
	}

	@JsonIgnore
	public final List<Workflow> getPayments(final Rights right) {
		return getNodes(Entity.PAYMENT_PLAN, grantedPaymentIdRights, right);
	}

	@JsonIgnore
	public final List<Workflow> getWorkflows() {
		return study.getNodesFromIds(Entity.WORKFLOW, grantedWorkflowIds.keySet());
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
		return Entity.PROFILE;
	}

	@JsonIgnore
	public Set<Feature> getOptionalFeatures() {
		return getFeatures().stream().filter(Feature::isOptional).collect(Collectors.toCollection(TreeSet::new));
	}

	//assignable
	@JsonIgnore
	public SortedSet<String> getAssignableIds(final Entity entity) {
		switch(entity) {
			case FEATURE:
				return getGrantedFeatureIds();
			case REPORT:
				return getGrantedReportIds();
			case MENU:
				return getGrantedMenuIds();
			case RESOURCE_CATEGORY:
				return getGrantedCategoryIds();
			case TIMELINE_GRAPH:
				return getGrantedTimelineGraphIds();
			default:
				throw new UnsupportedOperationException(String.format("%s is not an assignable entity", entity.getId()));
		}
	}

	@JsonIgnore
	public void addAssignableToProfileItem(final Assignable<?> item) {
		getAssignableIds(item.getEntity()).add(item.getId());
	}

	@JsonIgnore
	public void removeAssignableToProfileItem(final Assignable<?> item) {
		getAssignableIds(item.getEntity()).remove(item.getId());
	}

	//right assignable
	@JsonIgnore
	public Map<String, Set<Rights>> getEnumRightMatrixIds(final Entity entity) {
		switch(entity) {
			case DATASET_MODEL:
				return grantedDatasetModelIdRights;
			case FORM_MODEL:
				return grantedFormModelIdRights;
			case PROFILE:
				return grantedProfileIdRights;
			case SCOPE_MODEL:
				return grantedScopeModelIdRights;
			case PAYMENT_PLAN:
				return grantedPaymentIdRights;
			case EVENT_MODEL:
				return grantedEventModelIdRights;
			default:
				throw new UnsupportedOperationException(String.format("%s is not a right assignable entity", entity.getId()));
		}
	}

	@JsonIgnore
	public void addRightToItem(final RightAssignable<?> item, final Rights right) {
		final var assignables = getEnumRightMatrixIds(item.getEntity());
		if(!assignables.containsKey(item.getId())) {
			assignables.put(item.getId(), new TreeSet<>());
		}
		assignables.get(item.getId()).add(right);
	}

	@JsonIgnore
	public void removeRightFromItem(final RightAssignable<?> item, final Rights right) {
		final var assignables = getEnumRightMatrixIds(item.getEntity());
		if(assignables.containsKey(item.getId())) {
			assignables.get(item.getId()).remove(right);
		}
	}

	//attributable
	@JsonIgnore
	public SortedMap<String, Right> getAttributables(final Entity entity) {
		switch(entity) {
			case WORKFLOW:
				return getGrantedWorkflowIds();
			default:
				throw new UnsupportedOperationException(String.format("%s is not an attributable entity", entity.getId()));
		}
	}

	@JsonIgnore
	public boolean hasFeature(final String featureId) {
		return grantedFeatureIds.stream().anyMatch(f -> f.equals(featureId));
	}

	@JsonIgnore
	public boolean hasRight(final FeatureStatic feature) {
		return grantedFeatureIds.stream().anyMatch(f -> f.equals(feature.getId()));
	}

	//right on assignable
	@JsonIgnore
	public boolean hasRight(final Assignable<?> assignable) {
		return getAssignableIds(assignable.getEntity()).contains(assignable.getId());
	}

	@JsonIgnore
	public boolean hasRight(final Entity entity, final String nodeId) {
		return getAssignableIds(entity).contains(nodeId);
	}

	//right on right assignable
	@JsonIgnore
	public boolean hasRight(final RightAssignable<?> rightAssignable, final Rights right) {
		final var assignables = getEnumRightMatrixIds(rightAssignable.getEntity());
		return assignables.containsKey(rightAssignable.getId()) && assignables.get(rightAssignable.getId()).contains(right);
	}

	@JsonIgnore
	public boolean hasRight(final Entity entity, final String assignableId, final Rights right) {
		final var assignables = getEnumRightMatrixIds(entity);
		return assignables.containsKey(assignableId) && assignables.get(assignableId).contains(right);
	}

	@JsonIgnore
	public boolean hasRight(final Entity entity, final Rights right) {
		return getEnumRightMatrixIds(entity).entrySet().stream().anyMatch(e -> e.getValue().contains(right));
	}

	//rights on profile right assignable
	@JsonIgnore
	public boolean hasRight(final ProfileRightAssignable<?> profileRightAssignable, final Optional<Profile> creator) {
		if(!getGrantedWorkflowIds().containsKey(profileRightAssignable.getParentId())) {
			return false;
		}

		final var assignables = getGrantedWorkflowIds().get(profileRightAssignable.getParentId());
		if(!assignables.getChildRights().containsKey(profileRightAssignable.getId())) {
			return false;
		}

		final var profileRight = assignables.getChildRights().get(profileRightAssignable.getId());
		//workflow created by system
		if(creator.isEmpty()) {
			return profileRight.isSystem();
		}

		//workflow created by a user
		return profileRight.getProfileIds().contains(creator.get().getId());
	}

	//right on attributable
	@JsonIgnore
	public boolean hasRight(final Attributable<?> attributable) {
		final var attributables = getAttributables(attributable.getEntity());
		return attributables.containsKey(attributable.getId()) && attributables.get(attributable.getId()).isRight();
	}

	@Override
	@JsonIgnore
	public final PayableModel getPayableModel() {
		return this;
	}

	@Override
	@JsonIgnore
	public final String getPayableModelId() {
		return getId();
	}

	@Override
	@JsonIgnore
	public final int compareTo(final Profile profile) {
		return COMPARATOR_GLOBAL_ORDER.compare(this, profile);
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@JsonIgnore
	public List<PrivacyPolicy> getPrivacyPolicies() {
		return study.getPrivacyPolicies().stream().filter(p -> p.getProfileIds().contains(id)).toList();
	}
}
