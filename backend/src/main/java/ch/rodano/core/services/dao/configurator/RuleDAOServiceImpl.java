package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.RuleActionDTO;
import ch.rodano.api.config.RuleActionParameterDTO;
import ch.rodano.api.config.RuleConditionDTO;
import ch.rodano.api.config.RuleConditionListDTO;
import ch.rodano.api.config.RuleConstraintDTO;
import ch.rodano.api.config.RuleCriterionDTO;
import ch.rodano.api.config.RuleDTO;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleAction;
import ch.rodano.configuration.model.rules.RuleActionParameter;
import ch.rodano.configuration.model.rules.RuleCondition;
import ch.rodano.configuration.model.rules.RuleConditionCriterion;
import ch.rodano.configuration.model.rules.RuleConditionList;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.core.dao.RuleDAO;
import ch.rodano.core.model.jooq.enums.RuleConditionListDomain;
import ch.rodano.core.model.jooq.enums.RuleConditionListMode;
import ch.rodano.core.model.jooq.enums.RuleConditionMode;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;

import static ch.rodano.core.model.jooq.tables.Rule.RULE;
import static ch.rodano.core.model.jooq.tables.RuleAction.RULE_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleActionParameter.RULE_ACTION_PARAMETER;
import static ch.rodano.core.model.jooq.tables.RuleCondition.RULE_CONDITION;
import static ch.rodano.core.model.jooq.tables.RuleConditionList.RULE_CONDITION_LIST;
import static ch.rodano.core.model.jooq.tables.RuleConstraint.RULE_CONSTRAINT;
import static ch.rodano.core.model.jooq.tables.RuleCriterion.RULE_CRITERION;
import static ch.rodano.core.model.jooq.tables.RuleCriterionValue.RULE_CRITERION_VALUE;

@Repository
public class RuleDAOServiceImpl implements RuleDAOService {

	private final DSLContext dslContext;
	private final RuleDAO ruleDAO;
	private final JsonMapperService jsonMapperService;

	public RuleDAOServiceImpl(final DSLContext dslContext, final RuleDAO ruleDAO, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.ruleDAO = ruleDAO;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "rules", key = "#projectId.toString() + ':' + #entityType.name() + ':' + #entityId.toString()")
	public List<RuleDTO> getRules(final UUID projectId, final RuleEntityType entityType, final UUID entityId) {
		return ruleDAO.findByEntity(entityType, entityId).stream()
			.map(this::toDTO)
			.toList();
	}

	@Override
	@Transactional
	@CacheEvict(value = "rules", key = "#projectId.toString() + ':' + #entityType.name() + ':' + #entityId.toString()")
	public RuleDTO createRule(final UUID projectId, final RuleEntityType entityType, final UUID entityId, final String ruleType, final RuleDTO dto) {
		final UUID ruleId = UUID.randomUUID();

		dslContext.insertInto(RULE)
			.set(RULE.RULE_ID, ruleId)
			.set(RULE.PROJECT_ID, projectId)
			.set(RULE.ENTITY_TYPE, entityType)
			.set(RULE.ENTITY_ID, entityId)
			.set(RULE.RULE_TYPE, ruleType)
			.set(RULE.DESCRIPTION, dto.description())
			.set(RULE.MESSAGE, jsonMapperService.toJson(dto.message()))
			.set(RULE.TAG, jsonMapperService.toJson(dto.tags()))
			.execute();

		final UUID constraintId = UUID.randomUUID();
		dslContext.insertInto(RULE_CONSTRAINT)
			.set(RULE_CONSTRAINT.CONSTRAINT_ID, constraintId)
			.set(RULE_CONSTRAINT.PROJECT_ID, projectId)
			.set(RULE_CONSTRAINT.OWNER_TYPE, RuleConstraintOwnerType.RULE)
			.set(RULE_CONSTRAINT.OWNER_ID, ruleId)
			.set(RULE_CONSTRAINT.CONSTRAINT_TYPE, ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType.RULE)
			.execute();

		if(dto.constraint() != null) {
			saveConditionLists(projectId, constraintId, dto.constraint().conditions());
		}

		if(dto.actions() != null) {
			saveActions(projectId, ruleId, dto.actions());
		}

		return getRuleById(ruleId);
	}

	@Override
	@Transactional
	@CacheEvict(value = "rules", key = "#projectId.toString() + ':' + #entityType.name() + ':' + #entityId.toString()")
	public RuleDTO updateRule(final UUID projectId, final RuleEntityType entityType, final UUID entityId, final UUID ruleId, final RuleDTO dto) {
		dslContext.update(RULE)
			.set(RULE.DESCRIPTION, dto.description())
			.set(RULE.MESSAGE, jsonMapperService.toJson(dto.message()))
			.set(RULE.TAG, jsonMapperService.toJson(dto.tags()))
			.where(RULE.RULE_ID.eq(ruleId))
			.execute();

		final var constraintRecord = dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(ruleId))
			.fetchOne();

		if(constraintRecord != null && dto.constraint() != null) {
			final UUID constraintId = constraintRecord.getConstraintId();
			deleteConditionListsForConstraint(constraintId);
			saveConditionLists(projectId, constraintId, dto.constraint().conditions());
		}

		deleteActionsForRule(ruleId);
		if(dto.actions() != null) {
			saveActions(projectId, ruleId, dto.actions());
		}

		return getRuleById(ruleId);
	}

	@Override
	@Transactional
	@CacheEvict(value = "rules", key = "#projectId.toString() + ':' + #entityType.name() + ':' + #entityId.toString()")
	public void deleteRule(final UUID projectId, final RuleEntityType entityType, final UUID entityId, final UUID ruleId) {
		final var constraintRecord = dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(ruleId))
			.fetchOne();

		if(constraintRecord != null) {
			deleteConditionListsForConstraint(constraintRecord.getConstraintId());
			dslContext.deleteFrom(RULE_CONSTRAINT)
				.where(RULE_CONSTRAINT.CONSTRAINT_ID.eq(constraintRecord.getConstraintId()))
				.execute();
		}

		deleteActionsForRule(ruleId);

		dslContext.deleteFrom(RULE)
			.where(RULE.RULE_ID.eq(ruleId))
			.execute();
	}

	private RuleDTO getRuleById(final UUID ruleId) {
		final var record = dslContext.selectFrom(RULE)
			.where(RULE.RULE_ID.eq(ruleId))
			.fetchOne();
		if(record == null) {
			return null;
		}
		return toDTO(ruleDAO.findByEntity(
			record.getEntityType(),
			record.getEntityId()
		).stream().filter(r -> r.getRuleId().equals(ruleId)).findFirst().orElseThrow());
	}

	private RuleDTO toDTO(final Rule rule) {
		return new RuleDTO(
			rule.getRuleId(),
			rule.getDescription(),
			rule.getMessage(),
			new ArrayList<>(rule.getTags()),
			toConstraintDTO(rule.getConstraint()),
			rule.getActions().stream().map(this::toActionDTO).toList()
		);
	}

	private RuleConstraintDTO toConstraintDTO(final RuleConstraint constraint) {
		if(constraint == null) {
			return null;
		}
		final Map<String, RuleConditionListDTO> conditions = new TreeMap<>();
		constraint.getConditions().forEach((entity, list) ->
			conditions.put(entity.name(), toConditionListDTO(list))
		);
		return new RuleConstraintDTO(constraint.getRuleConstraintId(), conditions);
	}

	private RuleConditionListDTO toConditionListDTO(final RuleConditionList list) {
		if(list == null) {
			return null;
		}
		return new RuleConditionListDTO(
			list.getRuleConditionListId(),
			list.getMode().name(),
			list.getConditions().stream().map(this::toConditionDTO).toList()
		);
	}

	private RuleConditionDTO toConditionDTO(final RuleCondition condition) {
		return new RuleConditionDTO(
			condition.getRuleConditionId(),
			condition.getId(),
			toCriterionDTO(condition.getCriterion()),
			condition.isInverse(),
			condition.isDependency(),
			condition.getBreakType() != null ? condition.getBreakType().name() : "NONE",
			condition.getMode() != null ? condition.getMode().name() : "OR",
			condition.getConditions().stream().map(this::toConditionDTO).toList()
		);
	}

	private RuleCriterionDTO toCriterionDTO(final RuleConditionCriterion criterion) {
		if(criterion == null) {
			return null;
		}
		return new RuleCriterionDTO(
			criterion.getRuleCriterionId(),
			criterion.getProperty(),
			criterion.getOperator() != null ? criterion.getOperator().name() : null,
			new ArrayList<>(criterion.getValues())
		);
	}

	private RuleActionDTO toActionDTO(final RuleAction action) {
		return new RuleActionDTO(
			action.getRuleActionId(),
			action.getId(),
			action.getLabel(),
			action.isOptional(),
			action.getStaticActionId(),
			action.getConfigurationWorkflowId(),
			action.getConfigurationActionId(),
			action.getRulableEntity() != null ? action.getRulableEntity().name() : null,
			action.getConditionId(),
			action.getActionId(),
			action.getParameters().stream().map(this::toParameterDTO).toList()
		);
	}

	private RuleActionParameterDTO toParameterDTO(final RuleActionParameter param) {
		return new RuleActionParameterDTO(
			param.getRuleActionParameterId(),
			param.getId(),
			param.getValue(),
			param.getRulableEntity() != null ? param.getRulableEntity().name() : null,
			param.getConditionId()
		);
	}

	private void saveConditionLists(final UUID projectId, final UUID constraintId, final Map<String, RuleConditionListDTO> conditions) {
		if(conditions == null) {
			return;
		}
		conditions.forEach((domainName, listDTO) -> {
			final UUID listId = listDTO.ruleConditionListId() != null ? listDTO.ruleConditionListId() : UUID.randomUUID();
			dslContext.insertInto(RULE_CONDITION_LIST)
				.set(RULE_CONDITION_LIST.CONDITION_LIST_ID, listId)
				.set(RULE_CONDITION_LIST.PROJECT_ID, projectId)
				.set(RULE_CONDITION_LIST.CONSTRAINT_ID, constraintId)
				.set(RULE_CONDITION_LIST.DOMAIN, RuleConditionListDomain.valueOf(domainName))
				.set(RULE_CONDITION_LIST.MODE, RuleConditionListMode.valueOf(listDTO.mode() != null ? listDTO.mode() : "OR"))
				.execute();

			if(listDTO.conditions() != null) {
				listDTO.conditions().forEach(c -> saveCondition(projectId, listId, null, c));
			}
		});
	}

	private void saveCondition(final UUID projectId, final UUID listId, final UUID parentConditionId, final RuleConditionDTO dto) {
		final UUID conditionId = dto.ruleConditionId() != null ? dto.ruleConditionId() : UUID.randomUUID();

		dslContext.insertInto(RULE_CONDITION)
			.set(RULE_CONDITION.CONDITION_ID, conditionId)
			.set(RULE_CONDITION.PROJECT_ID, projectId)
			.set(RULE_CONDITION.CONDITION_LIST_ID, listId)
			.set(RULE_CONDITION.PARENT_CONDITION_ID, parentConditionId)
			.set(RULE_CONDITION.CODE, dto.id())
			.set(RULE_CONDITION.MODE, RuleConditionMode.valueOf(dto.mode() != null ? dto.mode() : "OR"))
			.set(RULE_CONDITION.INVERSE, dto.inverse())
			.set(RULE_CONDITION.DEPENDENCY, dto.dependency())
			.set(RULE_CONDITION.BREAK_TYPE, dto.breakType())
			.execute();

		if(dto.criterion() != null) {
			saveCriterion(projectId, conditionId, dto.criterion());
		}

		if(dto.conditions() != null) {
			dto.conditions().forEach(child -> saveCondition(projectId, listId, conditionId, child));
		}
	}

	private void saveCriterion(final UUID projectId, final UUID conditionId, final RuleCriterionDTO dto) {
		final UUID criterionId = dto.ruleCriterionId() != null ? dto.ruleCriterionId() : UUID.randomUUID();

		dslContext.insertInto(RULE_CRITERION)
			.set(RULE_CRITERION.CRITERION_ID, criterionId)
			.set(RULE_CRITERION.PROJECT_ID, projectId)
			.set(RULE_CRITERION.CONDITION_ID, conditionId)
			.set(RULE_CRITERION.PROPERTY, dto.property())
			.set(RULE_CRITERION.OPERATOR, dto.operator())
			.execute();

		if(dto.values() != null) {
			for(int i = 0; i < dto.values().size(); i++) {
				dslContext.insertInto(RULE_CRITERION_VALUE)
					.set(RULE_CRITERION_VALUE.PROJECT_ID, projectId)
					.set(RULE_CRITERION_VALUE.CRITERION_ID, criterionId)
					.set(RULE_CRITERION_VALUE.VALUE_ORDER, i)
					.set(RULE_CRITERION_VALUE.VALUE_TEXT, dto.values().get(i))
					.execute();
			}
		}
	}

	private void saveActions(final UUID projectId, final UUID ruleId, final List<RuleActionDTO> actions) {
		for(int i = 0; i < actions.size(); i++) {
			final RuleActionDTO dto = actions.get(i);
			final UUID actionId = dto.ruleActionId() != null ? dto.ruleActionId() : UUID.randomUUID();

			dslContext.insertInto(RULE_ACTION)
				.set(RULE_ACTION.RULE_ACTION_ID, actionId)
				.set(RULE_ACTION.PROJECT_ID, projectId)
				.set(RULE_ACTION.RULE_ID, ruleId)
				.set(RULE_ACTION.CODE, dto.id())
				.set(RULE_ACTION.STATIC_ACTION_ID, dto.staticActionId())
				.set(RULE_ACTION.ACTION_ID_CODE, dto.actionId())
				.set(RULE_ACTION.OPTIONAL, dto.optional())
				.set(RULE_ACTION.LABEL, jsonMapperService.toJson(dto.label()))
				.set(RULE_ACTION.CONDITION_ID, dto.conditionId() != null ? UUID.fromString(dto.conditionId()) : null)
				.set(RULE_ACTION.RULABLE_ENTITY, dto.rulableEntity())
				.set(RULE_ACTION.ACTION_ORDER, i)
				.execute();

			if(dto.parameters() != null) {
				dto.parameters().forEach(p -> saveParameter(projectId, actionId, p));
			}
		}
	}

	private void saveParameter(final UUID projectId, final UUID actionId, final RuleActionParameterDTO dto) {
		dslContext.insertInto(RULE_ACTION_PARAMETER)
			.set(RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID, UUID.randomUUID())
			.set(RULE_ACTION_PARAMETER.PROJECT_ID, projectId)
			.set(RULE_ACTION_PARAMETER.RULE_ACTION_ID, actionId)
			.set(RULE_ACTION_PARAMETER.CODE, dto.id())
			.set(RULE_ACTION_PARAMETER.VALUE, dto.value())
			.set(RULE_ACTION_PARAMETER.RULING_ENTITY, dto.rulableEntity())
			.set(RULE_ACTION_PARAMETER.CONDITION_ID, dto.conditionId())
			.execute();
	}

	private void deleteConditionListsForConstraint(final UUID constraintId) {
		final var listIds = dslContext.select(RULE_CONDITION_LIST.CONDITION_LIST_ID)
			.from(RULE_CONDITION_LIST)
			.where(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(constraintId))
			.fetch(RULE_CONDITION_LIST.CONDITION_LIST_ID);

		for(final UUID listId : listIds) {
			final var conditionIds = dslContext.select(RULE_CONDITION.CONDITION_ID)
				.from(RULE_CONDITION)
				.where(RULE_CONDITION.CONDITION_LIST_ID.eq(listId))
				.fetch(RULE_CONDITION.CONDITION_ID);

			for(final UUID conditionId : conditionIds) {
				final var criterionIds = dslContext.select(RULE_CRITERION.CRITERION_ID)
					.from(RULE_CRITERION)
					.where(RULE_CRITERION.CONDITION_ID.eq(conditionId))
					.fetch(RULE_CRITERION.CRITERION_ID);

				criterionIds.forEach(cid ->
					dslContext.deleteFrom(RULE_CRITERION_VALUE)
						.where(RULE_CRITERION_VALUE.CRITERION_ID.eq(cid))
						.execute()
				);

				dslContext.deleteFrom(RULE_CRITERION)
					.where(RULE_CRITERION.CONDITION_ID.eq(conditionId))
					.execute();
			}

			dslContext.deleteFrom(RULE_CONDITION)
				.where(RULE_CONDITION.CONDITION_LIST_ID.eq(listId))
				.execute();
		}

		dslContext.deleteFrom(RULE_CONDITION_LIST)
			.where(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(constraintId))
			.execute();
	}

	private void deleteActionsForRule(final UUID ruleId) {
		final var actionIds = dslContext.select(RULE_ACTION.RULE_ACTION_ID)
			.from(RULE_ACTION)
			.where(RULE_ACTION.RULE_ID.eq(ruleId))
			.fetch(RULE_ACTION.RULE_ACTION_ID);

		actionIds.forEach(aid ->
			dslContext.deleteFrom(RULE_ACTION_PARAMETER)
				.where(RULE_ACTION_PARAMETER.RULE_ACTION_ID.eq(aid))
				.execute()
		);

		dslContext.deleteFrom(RULE_ACTION)
			.where(RULE_ACTION.RULE_ID.eq(ruleId))
			.execute();
	}
}
