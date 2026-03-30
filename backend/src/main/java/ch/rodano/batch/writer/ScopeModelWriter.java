package ch.rodano.batch.writer;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.JsonWriter;
import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.EventGroup;
import ch.rodano.batch.pojo.EventModel;
import ch.rodano.batch.pojo.Rule;
import ch.rodano.batch.pojo.ScopeModel;
import ch.rodano.batch.pojo.WorkflowStatesSelector;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventGroupId;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventGroupIdByScope;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveProfileId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowId;
import static ch.rodano.batch.helper.ModelResolvers.resolveWorkflowStateId;
import static ch.rodano.batch.helper.RuleHelper.insertConstraintForOwner;
import static ch.rodano.batch.helper.RuleHelper.insertRuleActions;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelDatasetModel.SCOPE_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelFormModel.SCOPE_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflow.SCOPE_MODEL_WORKFLOW;
import static ch.rodano.core.model.jooq.tables.ScopeModelWorkflowStateSelector.SCOPE_MODEL_WORKFLOW_STATE_SELECTOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;

public class ScopeModelWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScopeModelWriter.class);

	@Override
	public void writeItems(final List<Object> items) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : items) {
				@SuppressWarnings("unchecked") final ProjectScoped<ScopeModel> wrapped = (ProjectScoped<ScopeModel>) raw;
				final UUID projectId = wrapped.getProjectId();
				final ScopeModel scopeModel = wrapped.getPayload();
				final String scopeModelCode = scopeModel.getId();

				final UUID existingScopeModelId = resolveScopeModelId(tx, projectId, scopeModelCode);
				final UUID scopeModelId = existingScopeModelId != null
					? existingScopeModelId
					: deterministic(projectId, "SCOPE_MODEL", scopeModelCode);

				final UUID defaultProfileId = resolveProfileId(tx, projectId, scopeModel.getDefaultProfileId());

				if(scopeModel.getDefaultProfileId() != null && defaultProfileId == null) {
					LOGGER.warn("Default profile '{}' not found for scope '{}'", scopeModel.getDefaultProfileId(), scopeModelCode);
				}

				tx.insertInto(SCOPE_MODEL)
					.set(SCOPE_MODEL.PROJECT_ID, projectId)
					.set(SCOPE_MODEL.SCOPE_MODEL_ID, scopeModelId)
					.set(SCOPE_MODEL.CODE, scopeModelCode)
					.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
					.set(SCOPE_MODEL.EXPECTED_NUMBER, scopeModel.getExpectedNumber())
					.set(SCOPE_MODEL.MAX_NUMBER, scopeModel.getMaxNumber())
					.set(SCOPE_MODEL.SCOPE_FORMAT, scopeModel.getScopeFormat())
					.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, defaultProfileId)
					.set(SCOPE_MODEL.SHORTNAME, toJson(scopeModel.getShortname()))
					.set(SCOPE_MODEL.LONGNAME, toJson(scopeModel.getLongname()))
					.set(SCOPE_MODEL.DESCRIPTION, toJson(scopeModel.getDescription()))
					.set(SCOPE_MODEL.PLURAL_SHORTNAME, toJson(scopeModel.getPluralShortname()))
					.onDuplicateKeyUpdate()
					.set(SCOPE_MODEL.VIRTUAL, scopeModel.isVirtual())
					.set(SCOPE_MODEL.EXPECTED_NUMBER, scopeModel.getExpectedNumber())
					.set(SCOPE_MODEL.MAX_NUMBER, scopeModel.getMaxNumber())
					.set(SCOPE_MODEL.SCOPE_FORMAT, scopeModel.getScopeFormat())
					.set(SCOPE_MODEL.DEFAULT_PROFILE_ID, defaultProfileId)
					.set(SCOPE_MODEL.SHORTNAME, toJson(scopeModel.getShortname()))
					.set(SCOPE_MODEL.LONGNAME, toJson(scopeModel.getLongname()))
					.set(SCOPE_MODEL.DESCRIPTION, toJson(scopeModel.getDescription()))
					.set(SCOPE_MODEL.PLURAL_SHORTNAME, toJson(scopeModel.getPluralShortname()))
					.execute();

				putScopeRules(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getCreateRules(), "CREATE", "SCOPE_CREATE");
				putScopeRules(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getRemoveRules(), "REMOVE", "SCOPE_REMOVE");
				putScopeRules(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getRestoreRules(), "RESTORE", "SCOPE_RESTORE");

				linkMany(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getDatasetModelIds(),
					DATASET_MODEL, DATASET_MODEL.DATASET_MODEL_ID, DATASET_MODEL.CODE,
					SCOPE_MODEL_DATASET_MODEL, SCOPE_MODEL_DATASET_MODEL.DATASET_MODEL_ID, "dataset");

				linkMany(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getFormModelIds(),
					FORM_MODEL, FORM_MODEL.FORM_MODEL_ID, FORM_MODEL.CODE,
					SCOPE_MODEL_FORM_MODEL, SCOPE_MODEL_FORM_MODEL.FORM_MODEL_ID, "form");

				linkMany(tx, projectId, scopeModelId, scopeModelCode, scopeModel.getWorkflowIds(),
					WORKFLOW, WORKFLOW.WORKFLOW_ID, WORKFLOW.CODE,
					SCOPE_MODEL_WORKFLOW, SCOPE_MODEL_WORKFLOW.WORKFLOW_ID, "workflow");

				if(scopeModel.getWorkflowStatesSelectors() != null && !scopeModel.getWorkflowStatesSelectors().isEmpty()) {
					for(WorkflowStatesSelector statesSelector : scopeModel.getWorkflowStatesSelectors()) {
						final String wfCode = statesSelector.getWorkflowId();
						final UUID wfId = resolveWorkflowId(tx, projectId, wfCode);
						if(wfId == null) {
							LOGGER.warn("Scope '{}': workflow '{}' not found for selectors — skipping.", scopeModelCode, wfCode);
							continue;
						}

						for(String stateCode : statesSelector.getStateIds()) {
							final UUID stateId = resolveWorkflowStateId(tx, projectId, wfId, stateCode);
							tx.insertInto(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR)
								.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.PROJECT_ID, projectId)
								.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.SCOPE_MODEL_ID, scopeModelId)
								.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_ID, wfId)
								.set(SCOPE_MODEL_WORKFLOW_STATE_SELECTOR.WORKFLOW_STATE_ID, stateId)
								.onDuplicateKeyIgnore()
								.execute();
						}
					}
				}

				if(scopeModel.getEventGroups() != null) {
					for(EventGroup eventGroup : scopeModel.getEventGroups()) {
						final String eventGroupCode = eventGroup.getId();
						final UUID eventGroupId = deterministic(projectId, "EVENT_GROUP", scopeModelCode + "|" + eventGroupCode);

						tx.insertInto(EVENT_GROUP)
							.set(EVENT_GROUP.PROJECT_ID, projectId)
							.set(EVENT_GROUP.SCOPE_MODEL_ID, scopeModelId)
							.set(EVENT_GROUP.EVENT_GROUP_ID, eventGroupId)
							.set(EVENT_GROUP.CODE, eventGroupCode)
							.set(EVENT_GROUP.SHORTNAME, toJson(eventGroup.getShortname()))
							.set(EVENT_GROUP.LONGNAME, toJson(eventGroup.getLongname()))
							.set(EVENT_GROUP.DESCRIPTION, toJson(eventGroup.getDescription()))
							.set(EVENT_GROUP.ICON, eventGroup.getIcon())
							.onDuplicateKeyUpdate()
							.set(EVENT_GROUP.SHORTNAME, toJson(eventGroup.getShortname()))
							.set(EVENT_GROUP.LONGNAME, toJson(eventGroup.getLongname()))
							.set(EVENT_GROUP.DESCRIPTION, toJson(eventGroup.getDescription()))
							.set(EVENT_GROUP.ICON, eventGroup.getIcon())
							.execute();
					}
				}

				if(scopeModel.getEventModels() != null) {
					for(EventModel eventModel : scopeModel.getEventModels()) {
						final String eventModelCode = eventModel.getId();

						final var existingEventModelId = resolveEventModelId(tx, projectId, eventModelCode);
						final UUID eventModelId = existingEventModelId != null
							? existingEventModelId
							: deterministic(projectId, "EVENT_MODEL", scopeModelCode + "|" + eventModelCode);

						final String groupCode = eventModel.getEventGroupId();
						UUID eventGroupId = null;
						if(groupCode != null && !groupCode.isBlank()) {
							eventGroupId = resolveEventGroupIdByScope(tx, projectId, scopeModelId, groupCode);

							if(eventGroupId == null) {
								final UUID existingEventGroupId = resolveEventGroupId(tx, projectId, eventModel.getEventGroupId());

								final UUID expectedId = existingEventGroupId != null
									? existingEventGroupId
									: deterministic(projectId, "EVENT_GROUP", scopeModelCode + "|" + groupCode);

								tx.insertInto(EVENT_GROUP)
									.set(EVENT_GROUP.PROJECT_ID, projectId)
									.set(EVENT_GROUP.SCOPE_MODEL_ID, scopeModelId)
									.set(EVENT_GROUP.EVENT_GROUP_ID, eventGroupId)
									.set(EVENT_GROUP.CODE, groupCode)
									.onDuplicateKeyIgnore()
									.execute();

								eventGroupId = expectedId;
							}
						}

						tx.insertInto(EVENT_MODEL)
							.set(EVENT_MODEL.PROJECT_ID, projectId)
							.set(EVENT_MODEL.EVENT_MODEL_ID, eventModelId)
							.set(EVENT_MODEL.CODE, eventModelCode)
							.set(EVENT_MODEL.SCOPE_MODEL_ID, scopeModelId)
							.set(EVENT_MODEL.EVENT_GROUP_ID, eventGroupId)
							.set(EVENT_MODEL.INCEPTIVE, eventModel.isInceptive())
							.set(EVENT_MODEL.NUMBER, eventModel.getNumber())
							.set(EVENT_MODEL.MANDATORY, eventModel.isMandatory())
							.set(EVENT_MODEL.MAX_OCCURRENCE, eventModel.getMaxOccurrence())
							.set(EVENT_MODEL.PREVENT_ADD, eventModel.getPreventAdd())
							.set(EVENT_MODEL.DEADLINE_VALUE, eventModel.getDeadline())
							.set(EVENT_MODEL.DEADLINE_UNIT, eventModel.getDeadlineUnit())
							.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, eventModel.getDeadlineAggregationFunction())
							.set(EVENT_MODEL.INTERVAL_VALUE, eventModel.getInterval())
							.set(EVENT_MODEL.INTERVAL_UNIT, eventModel.getIntervalUnit())
							.set(EVENT_MODEL.LABEL_PATTERN, eventModel.getLabelPattern())
							.set(EVENT_MODEL.ICON, eventModel.getIcon())
							.set(EVENT_MODEL.SHORTNAME, toJson(eventModel.getShortname()))
							.set(EVENT_MODEL.LONGNAME, toJson(eventModel.getLongname()))
							.set(EVENT_MODEL.DESCRIPTION, toJson(eventModel.getDescription()))
							.onDuplicateKeyUpdate()
							.set(EVENT_MODEL.SCOPE_MODEL_ID, scopeModelId)
							.set(EVENT_MODEL.EVENT_GROUP_ID, eventGroupId)
							.set(EVENT_MODEL.INCEPTIVE, eventModel.isInceptive())
							.set(EVENT_MODEL.NUMBER, eventModel.getNumber())
							.set(EVENT_MODEL.MANDATORY, eventModel.isMandatory())
							.set(EVENT_MODEL.MAX_OCCURRENCE, eventModel.getMaxOccurrence())
							.set(EVENT_MODEL.PREVENT_ADD, eventModel.getPreventAdd())
							.set(EVENT_MODEL.DEADLINE_VALUE, eventModel.getDeadline())
							.set(EVENT_MODEL.DEADLINE_UNIT, eventModel.getDeadlineUnit())
							.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, eventModel.getDeadlineAggregationFunction())
							.set(EVENT_MODEL.INTERVAL_VALUE, eventModel.getInterval())
							.set(EVENT_MODEL.INTERVAL_UNIT, eventModel.getIntervalUnit())
							.set(EVENT_MODEL.LABEL_PATTERN, eventModel.getLabelPattern())
							.set(EVENT_MODEL.ICON, eventModel.getIcon())
							.set(EVENT_MODEL.SHORTNAME, toJson(eventModel.getShortname()))
							.set(EVENT_MODEL.LONGNAME, toJson(eventModel.getLongname()))
							.set(EVENT_MODEL.DESCRIPTION, toJson(eventModel.getDescription()))
							.execute();

						linkEventModel(tx, projectId, eventModelId, eventModel.getDatasetModelIds(),
							DATASET_MODEL, DATASET_MODEL.DATASET_MODEL_ID, DATASET_MODEL.CODE,
							EVENT_MODEL_DATASET_MODEL, EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID, "dataset");

						linkEventModel(tx, projectId, eventModelId, eventModel.getFormModelIds(),
							FORM_MODEL, FORM_MODEL.FORM_MODEL_ID, FORM_MODEL.CODE,
							EVENT_MODEL_FORM_MODEL, EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID, "form");

						linkEventModel(tx, projectId, eventModelId, eventModel.getWorkflowIds(),
							WORKFLOW, WORKFLOW.WORKFLOW_ID, WORKFLOW.CODE,
							EVENT_MODEL_WORKFLOW, EVENT_MODEL_WORKFLOW.WORKFLOW_ID, "workflow");

						relateEventsFlexible(tx, projectId, scopeModelCode, eventModelId,
							eventModel.getImpliedEventModelIds(),
							EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID,
							EVENT_MODEL_IMPLIED_EVENT,
							"implied");

						relateEventsFlexible(tx, projectId, scopeModelCode, eventModelId,
							eventModel.getBlockedEventModelIds(),
							EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID,
							EVENT_MODEL_BLOCKED_EVENT,
							"blocked");

						relateEventsFlexible(tx, projectId, scopeModelCode, eventModelId,
							eventModel.getDeadlineReferenceEventModelIds(),
							EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID,
							EVENT_MODEL_DEADLINE_REFERENCE,
							"deadline-ref");

						putEventModelRules(tx, projectId, scopeModelCode, eventModelCode, eventModelId, eventModel.getCreateRules(), "CREATE", "EVENT_CREATE");
						putEventModelRules(tx, projectId, scopeModelCode, eventModelCode, eventModelId, eventModel.getRemoveRules(), "REMOVE", "EVENT_REMOVE");
						putEventModelRules(tx, projectId, scopeModelCode, eventModelCode, eventModelId, eventModel.getRestoreRules(), "RESTORE", "EVENT_RESTORE");
					}
				}
			}
		});
	}

	private void putScopeRules(final DSLContext tx,
							   final UUID projectId,
							   final UUID scopeModelId,
							   final String scopeModelCode,
							   final List<Rule> rules,
							   final String ruleType,
							   final String saltPrefix) {

		if(rules == null || rules.isEmpty()) {
			return;
		}

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, saltPrefix, scopeModelCode + "|" + idx);

			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, RuleEntityType.SCOPE_MODEL)
				.set(RULE.ENTITY_ID, scopeModelId)
				.set(RULE.RULE_TYPE, ruleType)
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, toJson(rule.getMessage()))
				.set(RULE.TAG, JsonWriter.toJson(rule.getTags()))
				.onDuplicateKeyUpdate()
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, JsonWriter.toJson(rule.getMessage()))
				.set(RULE.TAG, JsonWriter.toJson(rule.getTags()))
				.execute();

			if(rule.getConstraint() != null) {
				insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint(), RuleConstraintConstraintType.RULE);
			}
			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}

	private void putEventModelRules(final DSLContext tx,
									final UUID projectId,
									final String scopeModelCode,
									final String eventModelCode,
									final UUID eventModelId,
									final List<Rule> rules,
									final String actionType,
									final String saltPrefix) {

		if(rules == null || rules.isEmpty()) {
			return;
		}

		for(int idx = 0; idx < rules.size(); idx++) {
			final Rule rule = rules.get(idx);
			final UUID ruleId = deterministic(projectId, saltPrefix, scopeModelCode + "|" + eventModelCode + "|" + idx);

			tx.insertInto(RULE)
				.set(RULE.PROJECT_ID, projectId)
				.set(RULE.RULE_ID, ruleId)
				.set(RULE.ENTITY_TYPE, RuleEntityType.EVENT_MODEL)
				.set(RULE.ENTITY_ID, eventModelId)
				.set(RULE.RULE_TYPE, actionType)
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, JsonWriter.toJson(rule.getMessage()))
				.set(RULE.TAG, JsonWriter.toJson(rule.getTags()))
				.onDuplicateKeyUpdate()
				.set(RULE.DESCRIPTION, rule.getDescription())
				.set(RULE.MESSAGE, JsonWriter.toJson(rule.getMessage()))
				.set(RULE.TAG, JsonWriter.toJson(rule.getTags()))
				.execute();

			if(rule.getConstraint() != null) {
				insertConstraintForOwner(tx, projectId, "RULE", ruleId, rule.getConstraint(), RuleConstraintConstraintType.RULE);
			}
			if(rule.getActions() != null && !rule.getActions().isEmpty()) {
				insertRuleActions(tx, projectId, ruleId, rule.getActions());
			}
		}
	}

	private static UUID resolveScopeId(final DSLContext tx, final UUID projectId, final String scopeCode) {
		if(scopeCode == null || scopeCode.isBlank()) {
			return null;
		}
		final Record1<UUID> r = tx.select(SCOPE_MODEL.SCOPE_MODEL_ID)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId).and(SCOPE_MODEL.CODE.eq(scopeCode)))
			.fetchOne();
		return r == null ? null : r.value1();
	}

	private static UUID resolveEventModelIdFlexible(final DSLContext tx,
													final UUID projectId,
													final String currentScopeCode,
													final String refEventCode) {

		if(refEventCode == null || refEventCode.isBlank()) {
			return null;
		}

		final UUID sameScopeId = deterministic(projectId, "EVENT_MODEL", currentScopeCode + "|" + refEventCode);
		final UUID existsSameScope =
			tx.select(EVENT_MODEL.EVENT_MODEL_ID)
				.from(EVENT_MODEL)
				.where(EVENT_MODEL.PROJECT_ID.eq(projectId)
					.and(EVENT_MODEL.EVENT_MODEL_ID.eq(sameScopeId)))
				.fetchOne(EVENT_MODEL.EVENT_MODEL_ID);
		if(existsSameScope != null) {
			return existsSameScope;
		}

		final List<UUID> matches = tx.select(EVENT_MODEL.EVENT_MODEL_ID)
			.from(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId).and(EVENT_MODEL.CODE.eq(refEventCode)))
			.fetch(EVENT_MODEL.EVENT_MODEL_ID);

		if(matches.isEmpty()) {
			return null;
		}
		if(matches.size() > 1) {
			return null;
		}
		return matches.getFirst();
	}

	private static void relateEventsFlexible(final DSLContext tx,
											 final UUID projectId,
											 final String scopeCode,
											 final UUID srcEventModelId,
											 final List<String> refCodes,
											 final org.jooq.TableField<?, UUID> refEventField,
											 final org.jooq.Table<?> relTable,
											 final String relNameForLogs) {

		if(refCodes == null || refCodes.isEmpty()) {
			return;
		}

		for(String refCode : refCodes) {
			if(refCode == null || refCode.isBlank()) {
				continue;
			}

			final UUID refEventModelId = resolveEventModelIdFlexible(tx, projectId, scopeCode, refCode);
			if(refEventModelId == null) {
				LOGGER.warn("Scope '{}': {} event '{}' not found or ambiguous — skipping relation.", scopeCode, relNameForLogs, refCode);
				continue;
			}

			tx.insertInto(relTable)
				.set(DSL.field("project_id", UUID.class), projectId)
				.set(DSL.field("event_model_id", UUID.class), srcEventModelId)
				.set(refEventField, refEventModelId)
				.onDuplicateKeyIgnore()
				.execute();
		}
	}

	private static <T extends Table<Record1<UUID>>> void linkMany(final DSLContext tx,
																  final UUID projectId,
																  final UUID scopeModelId,
																  final String scopeCode,
																  final List<String> codes,
																  final org.jooq.Table<?> targetTable,
																  final org.jooq.TableField<?, UUID> targetIdField,
																  final org.jooq.TableField<?, String> targetCodeField,
																  final org.jooq.Table<?> linkTable,
																  final org.jooq.TableField<?, UUID> linkTargetIdField,
																  final String what) {

		if(codes == null || codes.isEmpty()) {
			return;
		}

		for(String code : codes) {
			if(code == null || code.isBlank()) {
				continue;
			}

			final UUID targetId = tx.select(targetIdField)
				.from(targetTable)
				.where(DSL.field("project_id", UUID.class).eq(projectId)
					.and(targetCodeField.eq(code)))
				.fetchOne(targetIdField);

			if(targetId == null) {
				LOGGER.warn("Scope '{}': {} '{}' not found — skipping link.", scopeCode, what, code);
				continue;
			}

			tx.insertInto(linkTable)
				.set(DSL.field("project_id", UUID.class), projectId)
				.set(DSL.field("scope_model_id", UUID.class), scopeModelId)
				.set(linkTargetIdField, targetId)
				.onDuplicateKeyIgnore()
				.execute();
		}
	}

	private static void linkEventModel(final DSLContext tx,
									   final UUID projectId,
									   final UUID eventModelId,
									   final List<String> codes,
									   final org.jooq.Table<?> targetTable,
									   final org.jooq.TableField<?, UUID> targetIdField,
									   final org.jooq.TableField<?, String> targetCodeField,
									   final org.jooq.Table<?> linkTable,
									   final org.jooq.TableField<?, UUID> linkTargetIdField,
									   final String what) {

		if(codes == null || codes.isEmpty()) {
			return;
		}

		for(String code : codes) {
			if(code == null || code.isBlank()) {
				continue;
			}

			final UUID targetId = tx.select(targetIdField)
				.from(targetTable)
				.where(DSL.field("project_id", UUID.class).eq(projectId)
					.and(targetCodeField.eq(code)))
				.fetchOne(targetIdField);

			if(targetId == null) {
				LOGGER.warn("Event ctx {} '{}' not found — skipping link.", what, code);
				continue;
			}

			tx.insertInto(linkTable)
				.set(DSL.field("project_id", UUID.class), projectId)
				.set(DSL.field("event_model_id", UUID.class), eventModelId)
				.set(linkTargetIdField, targetId)
				.onDuplicateKeyIgnore()
				.execute();
		}
	}
}
