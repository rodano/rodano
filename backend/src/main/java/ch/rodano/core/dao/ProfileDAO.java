package ch.rodano.core.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.profile.ProfileRight;
import ch.rodano.configuration.model.rights.Right;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.jooq.tables.records.ProfileRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Menu.MENU;
import static ch.rodano.core.model.jooq.tables.PaymentPlan.PAYMENT_PLAN;
import static ch.rodano.core.model.jooq.tables.Profile.PROFILE;
import static ch.rodano.core.model.jooq.tables.ProfileCategoryGrants.PROFILE_CATEGORY_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileDatasetModelRights.PROFILE_DATASET_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileEventModelRights.PROFILE_EVENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileFeatureGrants.PROFILE_FEATURE_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileFormModelRights.PROFILE_FORM_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileMenuGrants.PROFILE_MENU_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfilePaymentModelRights.PROFILE_PAYMENT_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileProfileRights.PROFILE_PROFILE_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileReportGrants.PROFILE_REPORT_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileScopeModelRights.PROFILE_SCOPE_MODEL_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileTimelineGraphGrants.PROFILE_TIMELINE_GRAPH_GRANTS;
import static ch.rodano.core.model.jooq.tables.ProfileWorkflowActionRights.PROFILE_WORKFLOW_ACTION_RIGHTS;
import static ch.rodano.core.model.jooq.tables.ProfileWorkflowRights.PROFILE_WORKFLOW_RIGHTS;
import static ch.rodano.core.model.jooq.tables.Report.REPORT;
import static ch.rodano.core.model.jooq.tables.ResourceCategory.RESOURCE_CATEGORY;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;

@Repository
public class ProfileDAO implements BaseProjectDAO<Profile> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public ProfileDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public List<Profile> findByProject(final UUID projectId) {
		return dslContext.selectFrom(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.orderBy(PROFILE.ORDER_BY, PROFILE.CODE)
			.fetch(this::mapToModel);
	}

	@Override
	public Profile findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(PROFILE)
			.where(PROFILE.PROJECT_ID.eq(projectId))
			.and(PROFILE.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Profile findById(final UUID id) {
		return dslContext.selectFrom(PROFILE)
			.where(PROFILE.PROFILE_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public Profile save(final Profile entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private Profile mapToModel(final ProfileRecord record) {
		if(record == null) {
			return null;
		}

		final Profile model = new Profile();

		model.setId(record.getCode());
		model.setProfileId(record.getProfileId());

		model.setOrderBy(record.getOrderBy());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		if(record.getWorkflowOfInterestId() != null) {
			model.setWorkflowIdOfInterest(getWorkflowCode(record.getWorkflowOfInterestId()));
		}

		model.setGrantedFeatureIds(loadFeatureGrants(record.getProfileId()));
		model.setGrantedReportIds(loadReportGrants(record.getProfileId()));
		model.setGrantedMenuIds(loadMenuGrants(record.getProfileId()));
		model.setGrantedCategoryIds(loadResourceCategoryGrants(record.getProfileId()));
		model.setGrantedTimelineGraphIds(loadTimelineGraphGrants(record.getProfileId()));

		model.setGrantedProfileIdRights(loadProfileRights(record.getProfileId()));
		model.setGrantedScopeModelIdRights(loadScopeModelRights(record.getProfileId()));
		model.setGrantedPaymentIdRights(loadPaymentPlanRights(record.getProfileId()));
		model.setGrantedDatasetModelIdRights(loadDatasetModelRights(record.getProfileId()));
		model.setGrantedEventModelIdRights(loadEventModelRights(record.getProfileId()));
		model.setGrantedFormModelIdRights(loadFormModelRights(record.getProfileId()));

		model.setGrantedWorkflowIds(loadWorkflowGrants(record.getProjectId(), record.getProfileId()));

		return model;
	}

	private SortedSet<String> loadFeatureGrants(final UUID profileId) {
		final var codes = dslContext.select(FEATURE.CODE)
			.from(PROFILE_FEATURE_GRANTS)
			.join(FEATURE).on(FEATURE.FEATURE_ID.eq(PROFILE_FEATURE_GRANTS.FEATURE_ID))
			.where(PROFILE_FEATURE_GRANTS.PROFILE_ID.eq(profileId))
			.fetch(FEATURE.CODE);
		return new TreeSet<>(codes);
	}

	private SortedSet<String> loadReportGrants(final UUID profileId) {
		final var codes = dslContext.select(REPORT.CODE)
			.from(PROFILE_REPORT_GRANTS)
			.join(REPORT).on(REPORT.REPORT_ID.eq(PROFILE_REPORT_GRANTS.REPORT_ID))
			.where(PROFILE_REPORT_GRANTS.PROFILE_ID.eq(profileId))
			.fetch(REPORT.CODE);
		return new TreeSet<>(codes);
	}

	private SortedSet<String> loadMenuGrants(final UUID profileId) {
		final var codes = dslContext.select(MENU.CODE)
			.from(PROFILE_MENU_GRANTS)
			.join(MENU).on(MENU.MENU_ID.eq(PROFILE_MENU_GRANTS.MENU_ID))
			.where(PROFILE_MENU_GRANTS.PROFILE_ID.eq(profileId))
			.fetch(MENU.CODE);
		return new TreeSet<>(codes);
	}

	private SortedSet<String> loadResourceCategoryGrants(final UUID profileId) {
		final var codes = dslContext.select(RESOURCE_CATEGORY.CODE)
			.from(PROFILE_CATEGORY_GRANTS)
			.join(RESOURCE_CATEGORY).on(RESOURCE_CATEGORY.CATEGORY_ID.eq(PROFILE_CATEGORY_GRANTS.CATEGORY_ID))
			.where(PROFILE_CATEGORY_GRANTS.PROFILE_ID.eq(profileId))
			.fetch(RESOURCE_CATEGORY.CODE);
		return new TreeSet<>(codes);
	}

	private SortedSet<String> loadTimelineGraphGrants(final UUID profileId) {
		final var codes = dslContext.select(TIMELINE_GRAPH.CODE)
			.from(PROFILE_TIMELINE_GRAPH_GRANTS)
			.join(TIMELINE_GRAPH).on(TIMELINE_GRAPH.TIMELINE_GRAPH_ID.eq(PROFILE_TIMELINE_GRAPH_GRANTS.TIMELINE_GRAPH_ID))
			.where(PROFILE_TIMELINE_GRAPH_GRANTS.PROFILE_ID.eq(profileId))
			.fetch(TIMELINE_GRAPH.CODE);
		return new TreeSet<>(codes);
	}

	private Map<String, Set<Rights>> loadProfileRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_PROFILE_RIGHTS,
			PROFILE,
			PROFILE.PROFILE_ID,
			PROFILE_PROFILE_RIGHTS.TARGET_PROFILE_ID,
			PROFILE.CODE,
			PROFILE_PROFILE_RIGHTS.CAN_READ,
			PROFILE_PROFILE_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private Map<String, Set<Rights>> loadScopeModelRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_SCOPE_MODEL_RIGHTS,
			SCOPE_MODEL,
			SCOPE_MODEL.SCOPE_MODEL_ID,
			PROFILE_SCOPE_MODEL_RIGHTS.SCOPE_MODEL_ID,
			SCOPE_MODEL.CODE,
			PROFILE_SCOPE_MODEL_RIGHTS.CAN_READ,
			PROFILE_SCOPE_MODEL_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private Map<String, Set<Rights>> loadPaymentPlanRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_PAYMENT_MODEL_RIGHTS,
			PAYMENT_PLAN,
			PAYMENT_PLAN.PAYMENT_PLAN_ID,
			PROFILE_PAYMENT_MODEL_RIGHTS.PAYMENT_PLAN_ID,
			PAYMENT_PLAN.CODE,
			PROFILE_PAYMENT_MODEL_RIGHTS.CAN_READ,
			PROFILE_PAYMENT_MODEL_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private Map<String, Set<Rights>> loadDatasetModelRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_DATASET_MODEL_RIGHTS,
			DATASET_MODEL,
			DATASET_MODEL.DATASET_MODEL_ID,
			PROFILE_DATASET_MODEL_RIGHTS.DATASET_MODEL_ID,
			DATASET_MODEL.CODE,
			PROFILE_DATASET_MODEL_RIGHTS.CAN_READ,
			PROFILE_DATASET_MODEL_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private Map<String, Set<Rights>> loadEventModelRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_EVENT_MODEL_RIGHTS,
			EVENT_MODEL,
			EVENT_MODEL.EVENT_MODEL_ID,
			PROFILE_EVENT_MODEL_RIGHTS.EVENT_MODEL_ID,
			EVENT_MODEL.CODE,
			PROFILE_EVENT_MODEL_RIGHTS.CAN_READ,
			PROFILE_EVENT_MODEL_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private Map<String, Set<Rights>> loadFormModelRights(final UUID profileId) {
		return loadRightsMap(
			PROFILE_FORM_MODEL_RIGHTS,
			FORM_MODEL,
			FORM_MODEL.FORM_MODEL_ID,
			PROFILE_FORM_MODEL_RIGHTS.FORM_MODEL_ID,
			FORM_MODEL.CODE,
			PROFILE_FORM_MODEL_RIGHTS.CAN_READ,
			PROFILE_FORM_MODEL_RIGHTS.CAN_WRITE,
			profileId
		);
	}

	private <T extends Table<?>, U extends TableField<?, UUID>, V extends TableField<?, UUID>,
		W extends TableField<?, String>, X extends TableField<?, Boolean>, Y extends TableField<?, Boolean>>
	Map<String, Set<Rights>> loadRightsMap(final T rightsTable,
										   final Table<?> entityTable,
										   final U entityIdField,
										   final V joinField,
										   final W codeField,
										   final X canReadField,
										   final Y canWriteField,
										   final UUID profileId) {
		final Map<String, Set<Rights>> result = new TreeMap<>();

		final var records = dslContext.select(codeField, canReadField, canWriteField)
			.from(rightsTable)
			.join(entityTable).on(entityIdField.eq(joinField))
			.where(rightsTable.field("profile_id", UUID.class).eq(profileId))
			.fetch();

		for(var record : records) {
			final String code = record.value1();
			final Set<Rights> rights = new TreeSet<>();
			if(Boolean.TRUE.equals(record.value2())) {
				rights.add(Rights.READ);
			}
			if(Boolean.TRUE.equals(record.value3())) {
				rights.add(Rights.WRITE);
			}
			result.put(code, rights);
		}

		return result;
	}

	private SortedMap<String, Right> loadWorkflowGrants(final UUID projectId, final UUID profileId) {
		final SortedMap<String, Right> result = new TreeMap<>();

		final var records = dslContext.select(
				WORKFLOW.CODE,
				PROFILE_WORKFLOW_RIGHTS.HAS_RIGHT
			)
			.from(PROFILE_WORKFLOW_RIGHTS)
			.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(PROFILE_WORKFLOW_RIGHTS.WORKFLOW_ID))
			.where(PROFILE_WORKFLOW_RIGHTS.PROFILE_ID.eq(profileId))
			.fetch();

		for(var record : records) {
			final String workflowCode = record.value1();
			final Boolean hasRight = record.value2();

			final Right right = new Right();
			right.setRight(Boolean.TRUE.equals(hasRight));

			final UUID workflowId = getWorkflowId(projectId, workflowCode);
			final SortedMap<String, ProfileRight> childRights = loadWorkflowActionRights(profileId, workflowId);
			right.setChildRights(childRights);

			result.put(workflowCode, right);
		}

		return result;
	}

	private SortedMap<String, ProfileRight> loadWorkflowActionRights(final UUID profileId, final UUID workflowId) {
		final SortedMap<String, ProfileRight> result = new TreeMap<>();

		final var records = dslContext.select(
				WORKFLOW_ACTION.CODE,
				PROFILE_WORKFLOW_ACTION_RIGHTS.GRANTED_BY_SYSTEM
			)
			.from(PROFILE_WORKFLOW_ACTION_RIGHTS)
			.join(WORKFLOW_ACTION).on(WORKFLOW_ACTION.WORKFLOW_ACTION_ID.eq(PROFILE_WORKFLOW_ACTION_RIGHTS.WORKFLOW_ACTION_ID))
			.where(PROFILE_WORKFLOW_ACTION_RIGHTS.PROFILE_ID.eq(profileId))
			.and(WORKFLOW_ACTION.WORKFLOW_ID.eq(workflowId))
			.fetch();

		for(var record : records) {
			final String actionCode = record.value1();
			final Boolean grantedBySystem = record.value2();

			final ProfileRight profileRight = new ProfileRight();
			profileRight.setSystem(Boolean.TRUE.equals(grantedBySystem));

			result.put(actionCode, profileRight);
		}

		return result;
	}

	private UUID getWorkflowId(final UUID projectId, final String workflowCode) {
		return dslContext.select(WORKFLOW.WORKFLOW_ID)
			.from(WORKFLOW)
			.where(WORKFLOW.CODE.eq(workflowCode)
				.and(WORKFLOW.PROJECT_ID.eq(projectId)))
			.fetchOne(WORKFLOW.WORKFLOW_ID);
	}

	private String getWorkflowCode(final UUID workflowId) {
		return dslContext.select(WORKFLOW.CODE)
			.from(WORKFLOW)
			.where(WORKFLOW.WORKFLOW_ID.eq(workflowId))
			.fetchOne(WORKFLOW.CODE);
	}
}
