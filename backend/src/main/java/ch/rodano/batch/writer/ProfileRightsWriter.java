package ch.rodano.batch.writer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.Profile;
import ch.rodano.batch.pojo.WorkflowRights;

import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFeatureId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFormModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveMenuId;
import static ch.rodano.batch.helper.ModelResolvers.resolvePaymentId;
import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.batch.helper.ModelResolvers.resolveReportId;
import static ch.rodano.batch.helper.ModelResolvers.resolveResourceCategoryId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveTimelineGraphId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowActionId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
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

public class ProfileRightsWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProfileRightsWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<Profile> wrapped = (ProjectScoped<Profile>) raw;
				final UUID projectId = wrapped.getProjectId();
				final Profile profile = wrapped.getPayload();

				final String profileCode = profile.getId();
				final UUID existing = resolveProfileId(tx, projectId, profileCode);
				final UUID profileId = existing != null ? existing : deterministic(projectId, "PROFILE", profileCode);

				putRightsMapToProfiles(tx, projectId, profileId, profile.getGrantedProfileIdRights());

				putRightsMap(tx, projectId, profileId, profile.getGrantedDatasetModelIdRights(), "DATASET_MODEL", TargetKind.DATASET);
				putRightsMap(tx, projectId, profileId, profile.getGrantedEventModelIdRights(), "EVENT_MODEL", TargetKind.EVENT);
				putRightsMap(tx, projectId, profileId, profile.getGrantedFormModelIdRights(), "FORM_MODEL", TargetKind.FORM);
				putRightsMap(tx, projectId, profileId, profile.getGrantedScopeModelIdRights(), "SCOPE_MODEL", TargetKind.SCOPE);
				putRightsMap(tx, projectId, profileId, profile.getGrantedPaymentIdRights(), "PAYMENT_PLAN", TargetKind.PAYMENT);

				putWorkflowRights(tx, projectId, profileId, profileCode, profile.getGrantedWorkflowIds());

				putSimpleGrants(tx, projectId, profileId, profile.getGrantedFeatureIds(), "FEATURE", SimpleGrant.FEATURE);
				putSimpleGrants(tx, projectId, profileId, profile.getGrantedMenuIds(), "MENU", SimpleGrant.MENU);
				putSimpleGrants(tx, projectId, profileId, profile.getGrantedCategoryIds(), "RESOURCE_CATEGORY", SimpleGrant.CATEGORY);
				putSimpleGrants(tx, projectId, profileId, profile.getGrantedTimelineGraphIds(), "TIMELINE_GRAPH", SimpleGrant.TIMELINE);
				putSimpleGrants(tx, projectId, profileId, profile.getGrantedReportIds(), "REPORT", SimpleGrant.REPORT);
			}
		});
	}

	private enum TargetKind {DATASET, EVENT, FORM, SCOPE, PAYMENT}

	private static void putRightsMap(final DSLContext tx,
									 final UUID projectId,
									 final UUID profileId,
									 final Map<String, List<String>> rightsMap,
									 final String targetKind,
									 final TargetKind table) {

		if(rightsMap == null || rightsMap.isEmpty()) {
			return;
		}
		final Function<String, UUID> resolvedId = idResolver(tx, projectId, targetKind);
		rightsMap.forEach((code, rights) -> {
			final UUID targetId = resolvedId.apply(code);
			if(targetId == null) {
				return;
			}

			final boolean canRead = rights != null && rights.stream().anyMatch("READ"::equalsIgnoreCase);
			final boolean canWrite = rights != null && rights.stream().anyMatch("WRITE"::equalsIgnoreCase);

			switch(table) {
				case DATASET -> tx.insertInto(PROFILE_DATASET_MODEL_RIGHTS)
					.set(PROFILE_DATASET_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_DATASET_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_DATASET_MODEL_RIGHTS.DATASET_MODEL_ID, targetId)
					.set(PROFILE_DATASET_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_DATASET_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.onDuplicateKeyUpdate()
					.set(PROFILE_DATASET_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_DATASET_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.execute();

				case EVENT -> tx.insertInto(PROFILE_EVENT_MODEL_RIGHTS)
					.set(PROFILE_EVENT_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_EVENT_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_EVENT_MODEL_RIGHTS.EVENT_MODEL_ID, targetId)
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.onDuplicateKeyUpdate()
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_EVENT_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.execute();

				case FORM -> tx.insertInto(PROFILE_FORM_MODEL_RIGHTS)
					.set(PROFILE_FORM_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_FORM_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_FORM_MODEL_RIGHTS.FORM_MODEL_ID, targetId)
					.set(PROFILE_FORM_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_FORM_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.onDuplicateKeyUpdate()
					.set(PROFILE_FORM_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_FORM_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.execute();

				case SCOPE -> tx.insertInto(PROFILE_SCOPE_MODEL_RIGHTS)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.SCOPE_MODEL_ID, targetId)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.onDuplicateKeyUpdate()
					.set(PROFILE_SCOPE_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_SCOPE_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.execute();

				case PAYMENT -> tx.insertInto(PROFILE_PAYMENT_MODEL_RIGHTS)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.PROJECT_ID, projectId)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.PROFILE_ID, profileId)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.PAYMENT_PLAN_ID, targetId)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.onDuplicateKeyUpdate()
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.CAN_READ, canRead)
					.set(PROFILE_PAYMENT_MODEL_RIGHTS.CAN_WRITE, canWrite)
					.execute();

				default -> throw new IllegalArgumentException("Unknown target kind: " + targetKind);
			}
		});
	}

	private static void putRightsMapToProfiles(final DSLContext tx,
											   final UUID projectId,
											   final UUID profileId,
											   final Map<String, List<String>> map) {

		if(map == null || map.isEmpty()) {
			return;
		}
		map.forEach((otherProfileCode, rights) -> {
			final UUID existing = resolveProfileId(tx, projectId, otherProfileCode);
			final UUID targetProfileId = existing != null ? existing : deterministic(projectId, "PROFILE", otherProfileCode);
			final boolean canRead = rights != null && rights.stream().anyMatch("READ"::equalsIgnoreCase);
			final boolean canWrite = rights != null && rights.stream().anyMatch("WRITE"::equalsIgnoreCase);
			tx.insertInto(PROFILE_PROFILE_RIGHTS)
				.set(PROFILE_PROFILE_RIGHTS.PROJECT_ID, projectId)
				.set(PROFILE_PROFILE_RIGHTS.PROFILE_ID, profileId)
				.set(PROFILE_PROFILE_RIGHTS.TARGET_PROFILE_ID, targetProfileId)
				.set(PROFILE_PROFILE_RIGHTS.CAN_READ, canRead)
				.set(PROFILE_PROFILE_RIGHTS.CAN_WRITE, canWrite)
				.onDuplicateKeyUpdate()
				.set(PROFILE_PROFILE_RIGHTS.CAN_READ, canRead)
				.set(PROFILE_PROFILE_RIGHTS.CAN_WRITE, canWrite)
				.execute();
		});
	}

	private enum SimpleGrant {FEATURE, MENU, CATEGORY, TIMELINE, REPORT}

	private static void putSimpleGrants(final DSLContext tx,
										final UUID projectId,
										final UUID profileId,
										final List<String> codes,
										final String kind,
										final SimpleGrant table) {

		if(codes == null || codes.isEmpty()) {
			return;
		}

		final Function<String, UUID> resolvedId = idResolver(tx, projectId, kind);
		for(String code : codes) {
			final UUID targetId = resolvedId.apply(code);
			if(targetId == null) {
				LOGGER.warn("No possible value for project={}, profile={}, table={}, target={}", projectId, profileId, table.name(), code);
				continue;
			}

			switch(table) {
				case FEATURE -> tx.insertInto(PROFILE_FEATURE_GRANTS)
					.set(PROFILE_FEATURE_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_FEATURE_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_FEATURE_GRANTS.FEATURE_ID, targetId)
					.onDuplicateKeyIgnore()
					.execute();

				case MENU -> tx.insertInto(PROFILE_MENU_GRANTS)
					.set(PROFILE_MENU_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_MENU_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_MENU_GRANTS.MENU_ID, targetId)
					.onDuplicateKeyIgnore()
					.execute();

				case CATEGORY -> tx.insertInto(PROFILE_CATEGORY_GRANTS)
					.set(PROFILE_CATEGORY_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_CATEGORY_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_CATEGORY_GRANTS.CATEGORY_ID, targetId)
					.onDuplicateKeyIgnore()
					.execute();

				case TIMELINE -> tx.insertInto(PROFILE_TIMELINE_GRAPH_GRANTS)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_TIMELINE_GRAPH_GRANTS.TIMELINE_GRAPH_ID, targetId)
					.onDuplicateKeyIgnore()
					.execute();

				case REPORT -> tx.insertInto(PROFILE_REPORT_GRANTS)
					.set(PROFILE_REPORT_GRANTS.PROJECT_ID, projectId)
					.set(PROFILE_REPORT_GRANTS.PROFILE_ID, profileId)
					.set(PROFILE_REPORT_GRANTS.REPORT_ID, targetId)
					.onDuplicateKeyIgnore()
					.execute();

				default -> throw new IllegalArgumentException("Unknown target kind: " + kind);
			}
		}
	}

	private static void putWorkflowRights(final DSLContext tx,
										  final UUID projectId,
										  final UUID profileId,
										  final String profileCode,
										  final Map<String, WorkflowRights> workflows) {

		if(workflows == null || workflows.isEmpty()) {
			return;
		}
		workflows.forEach((workflowCode, right) -> {
			final UUID workflowId = resolveWorkflowId(tx, projectId, workflowCode);
			final boolean hasRight = right != null && right.getRight();
			tx.insertInto(PROFILE_WORKFLOW_RIGHTS)
				.set(PROFILE_WORKFLOW_RIGHTS.PROJECT_ID, projectId)
				.set(PROFILE_WORKFLOW_RIGHTS.PROFILE_ID, profileId)
				.set(PROFILE_WORKFLOW_RIGHTS.WORKFLOW_ID, workflowId)
				.set(PROFILE_WORKFLOW_RIGHTS.HAS_RIGHT, hasRight)
				.onDuplicateKeyUpdate()
				.set(PROFILE_WORKFLOW_RIGHTS.HAS_RIGHT, hasRight)
				.execute();

			if(right != null && right.getChildRights() != null && !right.getChildRights().isEmpty()) {
				right.getChildRights().forEach((actionCode, pr) -> {
					final boolean listedForThisProfile = pr == null || pr.getProfileIds() == null ||
						pr.getProfileIds().isEmpty() || pr.getProfileIds().stream().anyMatch(p -> p.equalsIgnoreCase(profileCode));

					if(!listedForThisProfile) {
						return;
					}
					final UUID workflowActionId = resolveWorkflowActionId(tx, projectId, workflowId, actionCode);
					if(workflowActionId == null) {
						return;
					}
					final boolean grantedBySystem = pr != null && pr.getSystem();
					tx.insertInto(PROFILE_WORKFLOW_ACTION_RIGHTS)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.PROJECT_ID, projectId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.PROFILE_ID, profileId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.WORKFLOW_ACTION_ID, workflowActionId)
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.GRANTED_BY_SYSTEM, grantedBySystem)
						.onDuplicateKeyUpdate()
						.set(PROFILE_WORKFLOW_ACTION_RIGHTS.GRANTED_BY_SYSTEM, grantedBySystem)
						.execute();
				});
			}
		});
	}

	private static Function<String, UUID> idResolver(final DSLContext tx, final UUID projectId, final String targetKind) {
		return switch(targetKind) {
			case "DATASET_MODEL" -> code -> resolveDatasetModelId(tx, projectId, code);
			case "EVENT_MODEL" -> code -> resolveEventModelId(tx, projectId, code);
			case "FORM_MODEL" -> code -> resolveFormModelId(tx, projectId, code);
			case "SCOPE_MODEL" -> code -> resolveScopeModelId(tx, projectId, code);
			case "PAYMENT_PLAN" -> code -> resolvePaymentId(tx, projectId, code);
			case "FEATURE" -> code -> resolveFeatureId(tx, projectId, code);
			case "MENU" -> code -> resolveMenuId(tx, projectId, code);
			case "RESOURCE_CATEGORY" -> code -> resolveResourceCategoryId(tx, projectId, code);
			case "TIMELINE_GRAPH" -> code -> resolveTimelineGraphId(tx, projectId, code);
			case "REPORT" -> code -> resolveReportId(tx, projectId, code);
			default -> code -> null;
		};
	}
}
