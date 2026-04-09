package ch.rodano.batch.helper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import ch.rodano.batch.pojo.RuleAction;
import ch.rodano.batch.pojo.RuleActionParameter;
import ch.rodano.batch.pojo.RuleCondition;
import ch.rodano.batch.pojo.RuleConditionList;
import ch.rodano.batch.pojo.RuleConstraint;
import ch.rodano.core.model.jooq.enums.RuleConditionListDomain;
import ch.rodano.core.model.jooq.enums.RuleConditionListMode;
import ch.rodano.core.model.jooq.enums.RuleConditionMode;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveRuleConditionId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.Feature.FEATURE;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.Validator.VALIDATOR;
import static ch.rodano.core.model.jooq.tables.Workflow.WORKFLOW;
import static ch.rodano.core.model.jooq.tables.WorkflowAction.WORKFLOW_ACTION;
import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;

public final class RuleHelper {

	private static final String UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

	private RuleHelper() {
	}

	public static void insertConstraintForOwner(
		final DSLContext tx,
		final UUID projectId,
		final String ownerTypeLiteral,
		final UUID ownerId,
		final RuleConstraint constraint,
		final RuleConstraintConstraintType constraintType
	) {
		insertConstraintForOwner(tx, projectId, ownerTypeLiteral, ownerId, constraint, constraintType, null);
	}

	public static void insertConstraintForOwner(
		final DSLContext tx,
		final UUID projectId,
		final String ownerTypeLiteral,
		final UUID ownerId,
		final RuleConstraint constraint,
		final RuleConstraintConstraintType constraintType,
		final UUID contextWorkflowId
	) {
		if(constraint == null) {
			return;
		}

		final RuleConstraintOwnerType ownerType = toOwnerType(ownerTypeLiteral);
		UUID resolvedConstraintId = tx
			.select(RULE_CONSTRAINT.CONSTRAINT_ID)
			.from(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.PROJECT_ID.eq(projectId))
			.and(RULE_CONSTRAINT.OWNER_TYPE.eq(ownerType))
			.and(RULE_CONSTRAINT.OWNER_ID.eq(ownerId))
			.fetchOne(RULE_CONSTRAINT.CONSTRAINT_ID);

		if(resolvedConstraintId == null) {
			final UUID candidateId = deterministic(projectId, "RULE_CONSTRAINT", ownerId.toString() + "|" + constraintType.name());

			tx.insertInto(RULE_CONSTRAINT)
				.set(RULE_CONSTRAINT.PROJECT_ID, projectId)
				.set(RULE_CONSTRAINT.CONSTRAINT_ID, candidateId)
				.set(RULE_CONSTRAINT.OWNER_TYPE, ownerType)
				.set(RULE_CONSTRAINT.OWNER_ID, ownerId)
				.set(RULE_CONSTRAINT.CONSTRAINT_TYPE, constraintType)
				.onDuplicateKeyIgnore()
				.execute();

			resolvedConstraintId = tx
				.select(RULE_CONSTRAINT.CONSTRAINT_ID)
				.from(RULE_CONSTRAINT)
				.where(RULE_CONSTRAINT.PROJECT_ID.eq(projectId))
				.and(RULE_CONSTRAINT.OWNER_TYPE.eq(ownerType))
				.and(RULE_CONSTRAINT.OWNER_ID.eq(ownerId))
				.and(RULE_CONSTRAINT.CONSTRAINT_TYPE.eq(constraintType))
				.fetchOne(RULE_CONSTRAINT.CONSTRAINT_ID);

			if(resolvedConstraintId == null) {
				throw new IllegalStateException("rule_constraint not found after insert: project="
					+ projectId + ", ownerType=" + ownerType + ", ownerId=" + ownerId + ", constraintType=" + constraintType);
			}
		}

		final Map<String, RuleConditionList> domains = constraint.getConditions();
		if(domains != null && !domains.isEmpty()) {
			for(Map.Entry<String, RuleConditionList> e : domains.entrySet()) {
				final String dom = normDomain(e.getKey());
				final RuleConditionList list = e.getValue();
				if(list == null) {
					continue;
				}

				final UUID listId = deterministic(projectId, "RULE_CONDITION_LIST", resolvedConstraintId + "|" + dom);

				tx.insertInto(RULE_CONDITION_LIST)
					.set(RULE_CONDITION_LIST.PROJECT_ID, projectId)
					.set(RULE_CONDITION_LIST.CONDITION_LIST_ID, listId)
					.set(RULE_CONDITION_LIST.CONSTRAINT_ID, resolvedConstraintId)
					.set(RULE_CONDITION_LIST.DOMAIN, asDomainTableDomain(dom))
					.set(RULE_CONDITION_LIST.MODE, toDomainMode(nz(list.getMode(), "OR")))
					.onDuplicateKeyUpdate()
					.set(RULE_CONDITION_LIST.MODE, toDomainMode(nz(list.getMode(), "OR")))
					.execute();

				final UUID rootConditionId = deterministic(projectId, "RULE_CONDITION",
					listId + "|root|" + dom);

				tx.insertInto(RULE_CONDITION)
					.set(RULE_CONDITION.PROJECT_ID, projectId)
					.set(RULE_CONDITION.CONDITION_ID, rootConditionId)
					.set(RULE_CONDITION.CONDITION_LIST_ID, listId)
					.set(RULE_CONDITION.PARENT_CONDITION_ID, (UUID) null)
					.set(RULE_CONDITION.CODE, dom)
					.set(RULE_CONDITION.MODE, RuleConditionMode.OR)
					.set(RULE_CONDITION.INVERSE, false)
					.set(RULE_CONDITION.DEPENDENCY, false)
					.set(RULE_CONDITION.BREAK_TYPE, "NONE")
					.set(RULE_CONDITION.CONDITION_ORDER, 0)
					.onDuplicateKeyIgnore()
					.execute();

				final List<RuleCondition> roots = list.getConditions() == null ? List.of() : list.getConditions();
				int sort = 0;
				for(RuleCondition node : roots) {
					final UUID nodeId = upsertNode(tx, projectId, listId, rootConditionId, node, sort++);
					upsertCriterionAndValues(tx, projectId, nodeId, node, dom, contextWorkflowId);
					final String nextEntity = getNextEntityType(
						node.getCriterion() != null ? node.getCriterion().getProperty() : null, dom);
					putChildren(tx, projectId, listId, nodeId, node.getConditions(), nextEntity, contextWorkflowId);
				}
			}
		}
	}

	public static void insertRuleActions(
		final DSLContext tx,
		final UUID projectId,
		final UUID ruleId,
		final List<RuleAction> actions
	) {
		insertRuleActions(tx, projectId, ruleId, actions, null);
	}

	public static void insertRuleActions(
		final DSLContext tx,
		final UUID projectId,
		final UUID ruleId,
		final List<RuleAction> actions,
		final UUID contextWorkflowId
	) {
		if(actions == null || actions.isEmpty()) {
			return;
		}

		int order = 0;
		for(RuleAction a : actions) {
			final String actionCode = (a.getId() == null || a.getId().trim().isEmpty()) ? a.getActionId() : a.getId();
			final String actionKey = (a.getId() == null || a.getId().trim().isEmpty()) ? "#" + order : actionCode;

			final UUID actionId = deterministic(projectId, "RULE_ACTION", ruleId + "|" + actionKey);

			final String staticActionId = (a.getStaticActionId() == null || a.getStaticActionId().trim().isEmpty())
				? null
				: a.getStaticActionId();

			final UUID conditionId = resolveRuleConditionId(tx, projectId, ruleId, a.getConditionId());

			tx.insertInto(RULE_ACTION)
				.set(RULE_ACTION.PROJECT_ID, projectId)
				.set(RULE_ACTION.RULE_ACTION_ID, actionId)
				.set(RULE_ACTION.RULE_ID, ruleId)
				.set(RULE_ACTION.CODE, actionCode)
				.set(RULE_ACTION.ACTION_ID_CODE, nz(a.getActionId(), a.getStaticActionId()))
				.set(RULE_ACTION.CONDITION_ID, conditionId)
				.set(RULE_ACTION.RULABLE_ENTITY, n2(a.getRulableEntity()))
				.set(RULE_ACTION.ACTION_ORDER, order)
				.set(RULE_ACTION.STATIC_ACTION_ID, staticActionId)
				.set(RULE_ACTION.OPTIONAL, a.isOptional())
				.set(RULE_ACTION.LABEL, toJson(a.getLabel()))
				.onDuplicateKeyIgnore()
				.execute();

			final boolean exists = tx.fetchExists(
				tx.selectOne()
					.from(RULE_ACTION)
					.where(RULE_ACTION.PROJECT_ID.eq(projectId))
					.and(RULE_ACTION.RULE_ACTION_ID.eq(actionId))
			);

			if(!exists) {
				System.out.println("Skipping parameters for duplicate/failed action:: " + actionId);
				order++;
				continue;
			}

			if(a.getParameters() != null && !a.getParameters().isEmpty()) {
				int pi = 0;
				for(RuleActionParameter p : a.getParameters()) {
					final String pCode = (p.getId() == null || p.getId().isBlank()) ? "P" + pi : p.getId();
					final UUID paramId = deterministic(projectId, "RULE_ACTION_PARAMETER", actionId + "|" + pCode);

					String paramValue = n2(p.getValue());
					if("STATUS".equals(p.getId()) && paramValue != null
						&& !paramValue.matches(UUID_PATTERN)) {
						final UUID resolved = resolveStatusParam(tx, projectId, conditionId, paramValue, contextWorkflowId);
						if(resolved != null) {
							paramValue = resolved.toString();
						}
					}
					if("FEATURE_ID".equals(p.getId()) && paramValue != null
						&& !paramValue.matches(UUID_PATTERN)) {
						final UUID resolved = tx.select(FEATURE.FEATURE_ID).from(FEATURE)
							.where(FEATURE.PROJECT_ID.eq(projectId).and(FEATURE.CODE.eq(paramValue)))
							.fetchOne(FEATURE.FEATURE_ID);
						if(resolved != null) {
							paramValue = resolved.toString();
						}
					}

					tx.insertInto(RULE_ACTION_PARAMETER)
						.set(RULE_ACTION_PARAMETER.PROJECT_ID, projectId)
						.set(RULE_ACTION_PARAMETER.RULE_ACTION_ID, actionId)
						.set(RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID, paramId)
						.set(RULE_ACTION_PARAMETER.CODE, p.getId())
						.set(RULE_ACTION_PARAMETER.VALUE, paramValue)
						.set(RULE_ACTION_PARAMETER.RULING_ENTITY, n2(p.getRulableEntity()))
						.set(RULE_ACTION_PARAMETER.CONDITION_ID, n2(p.getConditionId()))
						.onDuplicateKeyUpdate()
						.set(RULE_ACTION_PARAMETER.CODE, p.getId())
						.set(RULE_ACTION_PARAMETER.VALUE, paramValue)
						.set(RULE_ACTION_PARAMETER.RULING_ENTITY, n2(p.getRulableEntity()))
						.set(RULE_ACTION_PARAMETER.CONDITION_ID, n2(p.getConditionId()))
						.execute();
					pi++;
				}
			}

			order++;
		}
	}

	private static UUID resolveStatusParam(final DSLContext tx, final UUID projectId,
	                                       final UUID conditionId, final String stateCode,
	                                       final UUID contextWorkflowId) {
		if(conditionId != null) {
			final UUID parentId = tx.select(RULE_CONDITION.PARENT_CONDITION_ID)
				.from(RULE_CONDITION)
				.where(RULE_CONDITION.CONDITION_ID.eq(conditionId))
				.fetchOne(RULE_CONDITION.PARENT_CONDITION_ID);

			if(parentId != null) {
				final String workflowCode = tx
					.select(RULE_CRITERION_VALUE.VALUE_TEXT)
					.from(RULE_CONDITION)
					.join(RULE_CRITERION).on(RULE_CRITERION.CONDITION_ID.eq(RULE_CONDITION.CONDITION_ID))
					.join(RULE_CRITERION_VALUE).on(RULE_CRITERION_VALUE.CRITERION_ID.eq(RULE_CRITERION.CRITERION_ID))
					.where(RULE_CONDITION.PARENT_CONDITION_ID.eq(parentId))
					.and(RULE_CRITERION.PROPERTY.eq("ID"))
					.and(RULE_CONDITION.CONDITION_ID.ne(conditionId))
					.fetchOne(RULE_CRITERION_VALUE.VALUE_TEXT);

				if(workflowCode != null) {
					final UUID stateUuid = tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
						.from(WORKFLOW_STATE)
						.join(WORKFLOW).on(WORKFLOW.WORKFLOW_ID.eq(WORKFLOW_STATE.WORKFLOW_ID))
						.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
						.and(WORKFLOW.CODE.eq(workflowCode))
						.and(WORKFLOW_STATE.CODE.eq(stateCode))
						.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);
					if(stateUuid != null) {
						return stateUuid;
					}
				}
			}
		}

		if(contextWorkflowId != null) {
			final UUID stateUuid = tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
				.from(WORKFLOW_STATE)
				.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
				.and(WORKFLOW_STATE.WORKFLOW_ID.eq(contextWorkflowId))
				.and(WORKFLOW_STATE.CODE.eq(stateCode))
				.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);
			if(stateUuid != null) {
				return stateUuid;
			}
		}

		return resolveWorkflowStateUnique(tx, projectId, stateCode);
	}

	private static UUID upsertNode(
		final DSLContext tx,
		final UUID projectId,
		final UUID conditionListId,
		final UUID parentNodeId,
		final RuleCondition node,
		final int sort
	) {
		final String nodeCode = nz(node.getId(), "IDX_" + sort);
		final UUID nodeId = deterministic(projectId, "RULE_CONDITION",
			conditionListId + "|" + (parentNodeId == null ? "root" : parentNodeId) + "|" + nodeCode);

		tx.insertInto(RULE_CONDITION)
			.set(RULE_CONDITION.PROJECT_ID, projectId)
			.set(RULE_CONDITION.CONDITION_ID, nodeId)
			.set(RULE_CONDITION.CONDITION_LIST_ID, conditionListId)
			.set(RULE_CONDITION.PARENT_CONDITION_ID, parentNodeId)
			.set(RULE_CONDITION.CODE, nodeCode)
			.set(RULE_CONDITION.MODE, toNodeMode(nz(node.getMode(), "OR")))
			.set(RULE_CONDITION.INVERSE, node.isInverse())
			.set(RULE_CONDITION.DEPENDENCY, node.isDependency())
			.set(RULE_CONDITION.BREAK_TYPE, n2(node.getBreakType()))
			.set(RULE_CONDITION.CONDITION_ORDER, sort)
			.onDuplicateKeyUpdate()
			.set(RULE_CONDITION.MODE, toNodeMode(nz(node.getMode(), "OR")))
			.set(RULE_CONDITION.INVERSE, node.isInverse())
			.set(RULE_CONDITION.DEPENDENCY, node.isDependency())
			.set(RULE_CONDITION.BREAK_TYPE, n2(node.getBreakType()))
			.set(RULE_CONDITION.CONDITION_ORDER, sort)
			.execute();

		final boolean nodeExists = tx.fetchExists(
			DSL.selectOne().from(RULE_CONDITION)
				.where(RULE_CONDITION.PROJECT_ID.eq(projectId))
				.and(RULE_CONDITION.CONDITION_ID.eq(nodeId))
		);
		if(!nodeExists) {
			throw new IllegalStateException("Inserted node not found: proj=" + projectId
				+ " list=" + conditionListId + " nodeId=" + nodeId);
		}
		return nodeId;
	}

	private static void putChildren(
		final DSLContext tx,
		final UUID projectId,
		final UUID conditionListId,
		final UUID parentNodeId,
		final List<RuleCondition> children,
		final String entityType,
		final UUID contextWorkflowId
	) {
		if(children == null || children.isEmpty()) {
			return;
		}
		int sort = 0;
		for(RuleCondition child : children) {
			final UUID nodeId = upsertNode(tx, projectId, conditionListId, parentNodeId, child, sort++);
			upsertCriterionAndValues(tx, projectId, nodeId, child, entityType, contextWorkflowId);
			final String nextEntity = getNextEntityType(
				child.getCriterion() != null ? child.getCriterion().getProperty() : null, entityType);
			putChildren(tx, projectId, conditionListId, nodeId, child.getConditions(), nextEntity, contextWorkflowId);
		}
	}

	private static void upsertCriterionAndValues(
		final DSLContext tx,
		final UUID projectId,
		final UUID nodeId,
		final RuleCondition node,
		final String entityType,
		final UUID contextWorkflowId
	) {
		if(node.getCriterion() == null || node.getCriterion().getProperty() == null) {
			return;
		}

		final String property = node.getCriterion().getProperty();

		final UUID criterionId = deterministic(projectId, "RULE_CRITERION", nodeId.toString());

		tx.insertInto(RULE_CRITERION)
			.set(RULE_CRITERION.PROJECT_ID, projectId)
			.set(RULE_CRITERION.CRITERION_ID, criterionId)
			.set(RULE_CRITERION.CONDITION_ID, nodeId)
			.set(RULE_CRITERION.PROPERTY, property)
			.set(RULE_CRITERION.OPERATOR, n2(node.getCriterion().getOperator()))
			.onDuplicateKeyUpdate()
			.set(RULE_CRITERION.OPERATOR, n2(node.getCriterion().getOperator()))
			.execute();

		final List<String> values = node.getCriterion().getValues() == null ? List.of() : node.getCriterion().getValues();
		for(int i = 0; i < values.size(); i++) {
			String value = values.get(i);

			if(value != null && !value.isBlank()
				&& !value.matches(UUID_PATTERN)) {
				final UUID resolved = resolveEntityValue(tx, projectId, entityType, property, value, contextWorkflowId);
				if(resolved != null) {
					value = resolved.toString();
				}
			}

			tx.insertInto(RULE_CRITERION_VALUE)
				.set(RULE_CRITERION_VALUE.PROJECT_ID, projectId)
				.set(RULE_CRITERION_VALUE.CRITERION_ID, criterionId)
				.set(RULE_CRITERION_VALUE.VALUE_ORDER, i)
				.set(RULE_CRITERION_VALUE.VALUE_TEXT, value)
				.onDuplicateKeyUpdate()
				.set(RULE_CRITERION_VALUE.VALUE_TEXT, value)
				.execute();
		}
	}

	private static UUID resolveEntityValue(final DSLContext tx, final UUID projectId,
	                                       final String entityType, final String property,
	                                       final String code, final UUID contextWorkflowId) {
		return switch(entityType + "." + property) {
			case "SCOPE.MODEL", "SCOPE.ID" -> tx.select(SCOPE_MODEL.SCOPE_MODEL_ID).from(SCOPE_MODEL)
				.where(SCOPE_MODEL.PROJECT_ID.eq(projectId).and(SCOPE_MODEL.CODE.eq(code)))
				.fetchOne(SCOPE_MODEL.SCOPE_MODEL_ID);
			case "EVENT.ID" -> tx.select(EVENT_MODEL.EVENT_MODEL_ID).from(EVENT_MODEL)
				.where(EVENT_MODEL.PROJECT_ID.eq(projectId).and(EVENT_MODEL.CODE.eq(code)))
				.fetchOne(EVENT_MODEL.EVENT_MODEL_ID);
			case "EVENT.EVENT_GROUP_ID" -> tx.select(EVENT_GROUP.EVENT_GROUP_ID).from(EVENT_GROUP)
				.where(EVENT_GROUP.PROJECT_ID.eq(projectId).and(EVENT_GROUP.CODE.eq(code)))
				.fetchOne(EVENT_GROUP.EVENT_GROUP_ID);
			case "DATASET.ID" -> tx.select(DATASET_MODEL.DATASET_MODEL_ID).from(DATASET_MODEL)
				.where(DATASET_MODEL.PROJECT_ID.eq(projectId).and(DATASET_MODEL.CODE.eq(code)))
				.fetchOne(DATASET_MODEL.DATASET_MODEL_ID);
			case "FIELD.ID" -> tx.select(FIELD_MODEL.FIELD_MODEL_ID).from(FIELD_MODEL)
				.where(FIELD_MODEL.PROJECT_ID.eq(projectId).and(FIELD_MODEL.CODE.eq(code)))
				.fetchOne(FIELD_MODEL.FIELD_MODEL_ID);
			case "FORM.ID" -> tx.select(FORM_MODEL.FORM_MODEL_ID).from(FORM_MODEL)
				.where(FORM_MODEL.PROJECT_ID.eq(projectId).and(FORM_MODEL.CODE.eq(code)))
				.fetchOne(FORM_MODEL.FORM_MODEL_ID);
			case "WORKFLOW.ID" -> tx.select(WORKFLOW.WORKFLOW_ID).from(WORKFLOW)
				.where(WORKFLOW.PROJECT_ID.eq(projectId).and(WORKFLOW.CODE.eq(code)))
				.fetchOne(WORKFLOW.WORKFLOW_ID);
			case "WORKFLOW.STATUS" -> {
				if(contextWorkflowId != null) {
					final UUID stateUuid = tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID)
						.from(WORKFLOW_STATE)
						.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
						.and(WORKFLOW_STATE.WORKFLOW_ID.eq(contextWorkflowId))
						.and(WORKFLOW_STATE.CODE.eq(code))
						.fetchOne(WORKFLOW_STATE.WORKFLOW_STATE_ID);
					if(stateUuid != null) {
						yield stateUuid;
					}
				}
				yield resolveWorkflowStateUnique(tx, projectId, code);
			}
			case "WORKFLOW.CREATION_ACTION" -> tx.select(WORKFLOW_ACTION.WORKFLOW_ACTION_ID).from(WORKFLOW_ACTION)
				.where(WORKFLOW_ACTION.PROJECT_ID.eq(projectId).and(WORKFLOW_ACTION.CODE.eq(code)))
				.fetchOne(WORKFLOW_ACTION.WORKFLOW_ACTION_ID);
			case "WORKFLOW.VALIDATOR_ID" -> tx.select(VALIDATOR.VALIDATOR_ID).from(VALIDATOR)
				.where(VALIDATOR.PROJECT_ID.eq(projectId).and(VALIDATOR.CODE.eq(code)))
				.fetchOne(VALIDATOR.VALIDATOR_ID);
			default -> null;
		};
	}

	private static UUID resolveWorkflowStateUnique(final DSLContext tx, final UUID projectId, final String code) {
		final var candidates = tx.select(WORKFLOW_STATE.WORKFLOW_STATE_ID).from(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId).and(WORKFLOW_STATE.CODE.eq(code)))
			.fetch(WORKFLOW_STATE.WORKFLOW_STATE_ID);
		return candidates.size() == 1 ? candidates.getFirst() : null;
	}

	private static String getNextEntityType(final String property, final String currentEntity) {
		if(property == null) {
			return currentEntity;
		}
		return switch(property) {
			case "WORKFLOW" -> "WORKFLOW";
			case "DATASET" -> "DATASET";
			case "FIELD", "FIELD_HAVING_VALUE", "LAST_NON_EMPTY_VALUE" -> "FIELD";
			case "FORM" -> "FORM";
			case "EVENT", "INCEPTIVE_EVENT", "PREVIOUS", "ALL_PREVIOUS",
			     "NEXT", "ALL_NEXT", "ALL_NEXT_INCLUDING_REMOVED" -> "EVENT";
			case "SCOPE", "ANCESTOR", "PARENT", "DEFAULT_PARENT",
			     "DESCENDANT", "LEAF" -> "SCOPE";
			default -> currentEntity;
		};
	}

	private static RuleConstraintOwnerType toOwnerType(final String s) {
		if(s == null) {
			return RuleConstraintOwnerType.RULE;
		}
		return switch(s.trim().toUpperCase()) {
			case "RULE" -> RuleConstraintOwnerType.RULE;
			case "FIELD_MODEL" -> RuleConstraintOwnerType.FIELD_MODEL;
			case "VALIDATOR" -> RuleConstraintOwnerType.VALIDATOR;
			case "FORM_LAYOUT" -> RuleConstraintOwnerType.FORM_LAYOUT;
			case "FORM_LAYOUT_CELL" -> RuleConstraintOwnerType.FORM_LAYOUT_CELL;
			default -> RuleConstraintOwnerType.RULE;
		};
	}

	private static String normDomain(final String d) {
		final String k = nzu(d);
		return switch(k) {
			case "SCOPE" -> "SCOPE";
			case "DATASET" -> "DATASET";
			case "WORKFLOW" -> "WORKFLOW";
			case "FIELD" -> "FIELD";
			case "FORM" -> "FORM";
			case "EVENT" -> "EVENT";
			default -> "SCOPE";
		};
	}

	private static RuleConditionListDomain asDomainTableDomain(final String token) {
		return RuleConditionListDomain.valueOf(token);
	}

	private static RuleConditionListMode toDomainMode(final String m) {
		return "AND".equalsIgnoreCase(m) ? RuleConditionListMode.AND : RuleConditionListMode.OR;
	}

	private static RuleConditionMode toNodeMode(final String m) {
		return "AND".equalsIgnoreCase(m) ? RuleConditionMode.AND : RuleConditionMode.OR;
	}

	private static String n2(final String s) {
		return (s == null || s.isBlank()) ? null : s;
	}

	private static String nzu(final String s) {
		return s == null ? "" : s.trim().toUpperCase();
	}

	private static String nz(final String s, final String def) {
		return (s == null || s.isBlank()) ? def : s;
	}
}
