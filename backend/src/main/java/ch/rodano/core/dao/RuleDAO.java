package ch.rodano.core.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleAction;
import ch.rodano.configuration.model.rules.RuleActionParameter;
import ch.rodano.configuration.model.rules.RuleBreakType;
import ch.rodano.configuration.model.rules.RuleCondition;
import ch.rodano.configuration.model.rules.RuleConditionCriterion;
import ch.rodano.configuration.model.rules.RuleConditionList;
import ch.rodano.configuration.model.rules.RuleConditionListEvaluationMode;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.rules.RuleEvaluation;
import ch.rodano.core.model.jooq.enums.RuleConditionListDomain;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.RuleActionParameterRecord;
import ch.rodano.core.model.jooq.tables.records.RuleActionRecord;
import ch.rodano.core.model.jooq.tables.records.RuleConditionListRecord;
import ch.rodano.core.model.jooq.tables.records.RuleConditionRecord;
import ch.rodano.core.model.jooq.tables.records.RuleConstraintRecord;
import ch.rodano.core.model.jooq.tables.records.RuleCriterionRecord;
import ch.rodano.core.model.jooq.tables.records.RuleRecord;

import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;

@Repository
public class RuleDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public RuleDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<Rule> findByEntity(final RuleEntityType entityType, final UUID entityId) {
		return dslContext.selectFrom(RULE)
			.where(RULE.ENTITY_TYPE.eq(entityType))
			.and(RULE.ENTITY_ID.eq(entityId))
			.fetch(this::mapToModel);
	}

	public List<Rule> findByEntityAndType(final RuleEntityType entityType, final UUID entityId, final String ruleType) {
		return dslContext.selectFrom(RULE)
			.where(RULE.ENTITY_TYPE.eq(entityType))
			.and(RULE.ENTITY_ID.eq(entityId))
			.and(RULE.RULE_TYPE.eq(ruleType))
			.fetch(this::mapToModel);
	}

	private Rule mapToModel(final RuleRecord record) {
		if(record == null) {
			return null;
		}

		final Rule model = new Rule();

		model.setRuleId(record.getRuleId());

		model.setDescription(record.getDescription());

		model.setMessage(mappingHelper.parseJsonToMap(record.getMessage()));

		if(record.getTag() != null && !record.getTag().isBlank()) {
			try {
				final List<String> tagList = mappingHelper.parseJson(record.getTag(), new TypeReference<>() {
				});
				model.setTags(new TreeSet<>(tagList));
			}
			catch(Exception e) {
				model.setTags(new TreeSet<>());
			}
		}

		model.setConstraint(loadConstraintForRule(record.getRuleId()));
		model.setActions(loadActionsForRule(record.getRuleId()));

		return model;
	}

	private RuleConstraint loadConstraintForRule(final UUID ruleId) {
		return dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(ruleId))
			.fetchOne(this::mapConstraintToModel);
	}

	private RuleConstraint mapConstraintToModel(final RuleConstraintRecord record) {
		final RuleConstraint model = new RuleConstraint();

		model.setRuleConstraintId(record.getConstraintId());

		final Map<RulableEntity, RuleConditionList> conditions = loadConditionListsForConstraint(record.getConstraintId());
		model.setConditions(conditions);

		if(record.getEvaluations() != null && !record.getEvaluations().isBlank()) {
			try {
				final List<RuleEvaluation> evaluations = mappingHelper.parseJson(record.getEvaluations(),
					new TypeReference<>() {
					});
				model.setEvaluations(evaluations);
			}
			catch(Exception e) {
				model.setEvaluations(new ArrayList<>());
			}
		}

		return model;
	}

	public Map<RuleConstraintOwnerType, Map<UUID, RuleConstraint>> loadAllConstraintsForProject(final UUID projectId) {
		final Map<RuleConstraintOwnerType, Map<UUID, RuleConstraint>> result = new TreeMap<>();

		final var constraintRecords = dslContext
			.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.PROJECT_ID.eq(projectId))
			.fetch();

		for(final var record : constraintRecords) {
			final var constraint = mapConstraintToModel(record);

			result.computeIfAbsent(record.getOwnerType(), k -> new TreeMap<>())
				.put(record.getOwnerId(), constraint);
		}

		return result;
	}

	private Map<RulableEntity, RuleConditionList> loadConditionListsForConstraint(final UUID constraintId) {
		final Map<RulableEntity, RuleConditionList> result = new TreeMap<>();

		dslContext.selectFrom(RULE_CONDITION_LIST)
			.where(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(constraintId))
			.fetch()
			.forEach(record -> {
				final RulableEntity domain = convertToRulableEntity(record.getDomain());
				if(domain != null) {
					result.put(domain, mapConditionListToModel(record));
				}
			});

		return result;
	}

	private RuleConditionList mapConditionListToModel(final RuleConditionListRecord record) {
		if(record == null) {
			return null;
		}

		final RuleConditionList model = new RuleConditionList();

		model.setRuleConditionListId(record.getConditionListId());

		if(record.getMode() != null) {
			model.setMode(mappingHelper.parseEnum(RuleConditionListEvaluationMode.class, record.getMode().name(), "mode"));
		}

		if(model.getMode() == null) {
			model.setMode(RuleConditionListEvaluationMode.OR);
		}

		model.setConditions(loadConditionsForList(record.getConditionListId(), null));

		return model;
	}

	private List<RuleCondition> loadConditionsForList(final UUID conditionListId, final UUID parentConditionId) {
		return dslContext.selectFrom(RULE_CONDITION)
			.where(RULE_CONDITION.CONDITION_LIST_ID.eq(conditionListId))
			.and(parentConditionId == null
				? RULE_CONDITION.PARENT_CONDITION_ID.isNull()
				: RULE_CONDITION.PARENT_CONDITION_ID.eq(parentConditionId))
			.fetch(this::mapConditionToModel);
	}

	private RuleCondition mapConditionToModel(final RuleConditionRecord record) {
		if(record == null) {
			return null;
		}

		final RuleCondition model = new RuleCondition();

		model.setRuleConditionId(record.getConditionId());
		model.setId(record.getCode());

		model.setInverse(record.getInverse() != null ? record.getInverse() : false);
		model.setDependency(record.getDependency() != null ? record.getDependency() : false);

		if(record.getMode() != null) {
			model.setMode(mappingHelper.parseEnum(RuleConditionListEvaluationMode.class, record.getMode().name(), "mode"));
		}

		if(model.getMode() == null) {
			model.setMode(RuleConditionListEvaluationMode.OR);
		}

		model.setBreakType(mappingHelper.parseEnum(RuleBreakType.class, record.getBreakType(), "breakType"));
		if(model.getBreakType() == null) {
			model.setBreakType(RuleBreakType.NONE);
		}

		model.setCriterion(loadCriterionForCondition(record.getConditionId()));

		model.setConditions(loadConditionsForList(record.getConditionListId(), record.getConditionId()));

		return model;
	}

	private RuleConditionCriterion loadCriterionForCondition(final UUID conditionId) {
		final var record = dslContext.selectFrom(RULE_CRITERION)
			.where(RULE_CRITERION.CONDITION_ID.eq(conditionId))
			.fetchOne();

		if(record == null) {
			return new RuleConditionCriterion();
		}

		return mapCriterionToModel(record);
	}

	private RuleConditionCriterion mapCriterionToModel(final RuleCriterionRecord record) {
		if(record == null) {
			return null;
		}

		final RuleConditionCriterion model = new RuleConditionCriterion();

		model.setRuleCriterionId(record.getCriterionId());

		model.setProperty(record.getProperty());

		model.setOperator(mappingHelper.parseEnum(Operator.class, record.getOperator(), "operator"));

		model.setValues(loadCriterionValues(record.getCriterionId()));

		return model;
	}

	private Set<String> loadCriterionValues(final UUID criterionId) {
		final var values = dslContext.select(RULE_CRITERION_VALUE.VALUE_TEXT)
			.from(RULE_CRITERION_VALUE)
			.where(RULE_CRITERION_VALUE.CRITERION_ID.eq(criterionId))
			.orderBy(RULE_CRITERION_VALUE.VALUE_ORDER)
			.fetch(RULE_CRITERION_VALUE.VALUE_TEXT);

		return new HashSet<>(values);
	}

	private List<RuleAction> loadActionsForRule(final UUID ruleId) {
		final var records = dslContext.selectFrom(RULE_ACTION)
			.where(RULE_ACTION.RULE_ID.eq(ruleId))
			.orderBy(RULE_ACTION.ACTION_ORDER)
			.fetch();

		return records.map(this::mapActionToModel);
	}

	private RuleAction mapActionToModel(final RuleActionRecord record) {
		if(record == null) {
			return null;
		}

		final RuleAction model = new RuleAction();

		model.setRuleActionId(record.getRuleActionId());
		model.setId(record.getCode());

		model.setOptional(record.getOptional() != null ? record.getOptional() : false);

		model.setLabel(mappingHelper.parseJsonToMap(record.getLabel()));

		model.setStaticActionId(record.getStaticActionId());
		model.setActionId(record.getActionIdCode());

		model.setRulableEntity(mappingHelper.parseEnum(RulableEntity.class, record.getRulableEntity(), "rulableEntity"));

		if(record.getConditionId() != null) {
			model.setConditionId(record.getConditionId().toString());
		}

		model.setParameters(loadParametersForAction(record.getRuleActionId()));

		return model;
	}

	private List<RuleActionParameter> loadParametersForAction(final UUID actionId) {
		return dslContext.selectFrom(RULE_ACTION_PARAMETER)
			.where(RULE_ACTION_PARAMETER.RULE_ACTION_ID.eq(actionId))
			.fetch(this::mapParameterToModel);
	}

	private RuleActionParameter mapParameterToModel(final RuleActionParameterRecord record) {
		if(record == null) {
			return null;
		}

		final RuleActionParameter model = new RuleActionParameter();

		model.setRuleActionParameterId(record.getRuleActionParameterId());
		model.setId(record.getCode());

		model.setValue(record.getValue());
		model.setConditionId(record.getConditionId());

		model.setRulableEntity(mappingHelper.parseEnum(RulableEntity.class, record.getRulingEntity(), "rulingEntity"));

		return model;
	}

	private RulableEntity convertToRulableEntity(final RuleConditionListDomain domain) {
		if(domain == null) {
			return null;
		}
		try {
			return RulableEntity.valueOf(domain.name());
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}

	public RuleConstraint loadConstraintForOwner(final RuleConstraintOwnerType ownerType, final UUID ownerId) {
		if(ownerId == null) {
			return null;
		}
		return dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_TYPE.eq(ownerType))
			.and(RULE_CONSTRAINT.OWNER_ID.eq(ownerId))
			.fetchOne(this::mapConstraintToModel);
	}
}
