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
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;
import static org.jooq.impl.DSL.val;

public final class RuleHelper {

	private RuleHelper() {
	}

	public static void insertConstraintForOwner(
		final DSLContext tx,
		final UUID projectId,
		final String ownerTypeLiteral,  // "RULE","FIELD_MODEL","VALIDATOR","FORM_LAYOUT","FORM_LAYOUT_CELL"
		final UUID ownerId,
		final RuleConstraint constraint,
		final RuleConstraintConstraintType constraintType
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
					upsertCriterionAndValues(tx, projectId, nodeId, node);
					putChildren(tx, projectId, listId, nodeId, node.getConditions());
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

					tx.insertInto(RULE_ACTION_PARAMETER)
						.set(RULE_ACTION_PARAMETER.PROJECT_ID, projectId)
						.set(RULE_ACTION_PARAMETER.RULE_ACTION_ID, actionId)
						.set(RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID, paramId)
						.set(RULE_ACTION_PARAMETER.CODE, p.getId())
						.set(RULE_ACTION_PARAMETER.VALUE, n2(p.getValue()))
						.set(RULE_ACTION_PARAMETER.RULING_ENTITY, val((String) null))
						.set(RULE_ACTION_PARAMETER.CONDITION_ID, n2(p.getConditionId()))
						.onDuplicateKeyUpdate()
						.set(RULE_ACTION_PARAMETER.CODE, p.getId())
						.set(RULE_ACTION_PARAMETER.VALUE, n2(p.getValue()))
						.set(RULE_ACTION_PARAMETER.RULING_ENTITY, val((String) null))
						.set(RULE_ACTION_PARAMETER.CONDITION_ID, n2(p.getConditionId()))
						.execute();
					pi++;
				}
			}

			order++;
		}
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
		final List<RuleCondition> children
	) {

		if(children == null || children.isEmpty()) {
			return;
		}
		int sort = 0;
		for(RuleCondition child : children) {
			final UUID nodeId = upsertNode(tx, projectId, conditionListId, parentNodeId, child, sort++);
			upsertCriterionAndValues(tx, projectId, nodeId, child);
			putChildren(tx, projectId, conditionListId, nodeId, child.getConditions());
		}
	}

	private static void upsertCriterionAndValues(
		final DSLContext tx,
		final UUID projectId,
		final UUID nodeId,
		final RuleCondition node
	) {
		if(node.getCriterion() == null || node.getCriterion().getProperty() == null) {
			return;
		}

		final UUID criterionId = deterministic(projectId, "RULE_CRITERION", nodeId.toString());

		tx.insertInto(RULE_CRITERION)
			.set(RULE_CRITERION.PROJECT_ID, projectId)
			.set(RULE_CRITERION.CRITERION_ID, criterionId)
			.set(RULE_CRITERION.CONDITION_ID, nodeId)
			.set(RULE_CRITERION.PROPERTY, node.getCriterion().getProperty())
			.set(RULE_CRITERION.OPERATOR, n2(node.getCriterion().getOperator()))
			.onDuplicateKeyUpdate()
			.set(RULE_CRITERION.OPERATOR, n2(node.getCriterion().getOperator()))
			.execute();

		final List<String> values = node.getCriterion().getValues() == null ? List.of() : node.getCriterion().getValues();
		for(int i = 0; i < values.size(); i++) {
			tx.insertInto(RULE_CRITERION_VALUE)
				.set(RULE_CRITERION_VALUE.PROJECT_ID, projectId)
				.set(RULE_CRITERION_VALUE.CRITERION_ID, criterionId)
				.set(RULE_CRITERION_VALUE.VALUE_ORDER, i)
				.set(RULE_CRITERION_VALUE.VALUE_TEXT, values.get(i))
				.onDuplicateKeyIgnore()
				.execute();
		}
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
