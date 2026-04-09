package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.RuleActionDTO;
import ch.rodano.api.config.RuleActionParameterDTO;
import ch.rodano.api.config.RuleConditionDTO;
import ch.rodano.api.config.RuleConditionListDTO;
import ch.rodano.api.config.RuleConstraintDTO;
import ch.rodano.api.config.RuleCriterionDTO;
import ch.rodano.api.config.RuleDTO;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.rules.RuleCondition;
import ch.rodano.configuration.model.rules.RuleConditionCriterion;
import ch.rodano.configuration.model.rules.RuleConditionList;
import ch.rodano.core.dao.RuleDAO;
import ch.rodano.core.model.jooq.enums.RuleConditionListDomain;
import ch.rodano.core.model.jooq.enums.RuleConditionListMode;
import ch.rodano.core.model.jooq.enums.RuleConditionMode;
import ch.rodano.core.model.jooq.enums.RuleConstraintConstraintType;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.RuleActionRecord;

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
		final var ruleTypeMap = dslContext.selectFrom(RULE)
			.where(RULE.PROJECT_ID.eq(projectId))
			.and(RULE.ENTITY_TYPE.eq(entityType))
			.and(RULE.ENTITY_ID.eq(entityId))
			.fetch()
			.intoMap(RULE.RULE_ID, RULE.RULE_TYPE);

		return ruleDAO.findByEntity(entityType, entityId).stream()
			.map(rule -> toDTO(rule, ruleTypeMap.get(rule.getRuleId())))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RuleConstraintDTO getConstraint(final UUID projectId, final UUID ownerId) {
		final var constraintRecord = dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(ownerId))
			.fetchOne();
		if(constraintRecord == null) {
			return new RuleConstraintDTO(null, Map.of());
		}
		return toConstraintDTO(constraintRecord.getConstraintId());
	}

	@Override
	@Transactional
	public RuleConstraintDTO saveConstraint(final UUID projectId, final UUID ownerId, final RuleConstraintOwnerType ownerType,
	                                        final RuleConstraintConstraintType constraintType, final RuleConstraintDTO dto) {
		final var constraintRecord = dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(ownerId))
			.fetchOne();

		final UUID constraintId;
		if(constraintRecord == null) {
			constraintId = UUID.randomUUID();
			dslContext.insertInto(RULE_CONSTRAINT)
				.set(RULE_CONSTRAINT.CONSTRAINT_ID, constraintId)
				.set(RULE_CONSTRAINT.PROJECT_ID, projectId)
				.set(RULE_CONSTRAINT.OWNER_TYPE, ownerType)
				.set(RULE_CONSTRAINT.OWNER_ID, ownerId)
				.set(RULE_CONSTRAINT.CONSTRAINT_TYPE, constraintType)
				.execute();
		}
		else {
			constraintId = constraintRecord.getConstraintId();
			deleteConditionListsForConstraint(constraintId);
		}

		if(dto.conditions() != null) {
			saveConditionLists(projectId, constraintId, dto.conditions());
		}

		return toConstraintDTO(constraintId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getAllTags(final UUID projectId) {
		return dslContext.select(RULE.TAG)
			.from(RULE)
			.where(RULE.PROJECT_ID.eq(projectId))
			.and(RULE.TAG.isNotNull())
			.fetch(RULE.TAG)
			.stream()
			.flatMap(json -> {
				final List<String> tags = jsonMapperService.fromJson(json, new TypeReference<>() {
				});
				return tags != null ? tags.stream() : Stream.empty();
			})
			.distinct()
			.sorted()
			.toList();
	}

	@Override
	@Transactional
	@CacheEvict(value = "rules", key = "#projectId.toString() + ':' + #entityType.name() + ':' + #entityId.toString()")
	public RuleDTO createRule(final UUID projectId, final RuleEntityType entityType, final UUID entityId, final String ruleType, final RuleDTO dto) {
		final UUID ruleId = dto.ruleId() != null ? dto.ruleId() : UUID.randomUUID();
		final String resolvedType = ruleType != null ? ruleType : dto.ruleType();

		dslContext.insertInto(RULE)
			.set(RULE.RULE_ID, ruleId)
			.set(RULE.PROJECT_ID, projectId)
			.set(RULE.ENTITY_TYPE, entityType)
			.set(RULE.ENTITY_ID, entityId)
			.set(RULE.RULE_TYPE, resolvedType)
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

		final String ruleType = record.getRuleType();
		return ruleDAO.findByEntity(record.getEntityType(), record.getEntityId())
			.stream()
			.filter(r -> r.getRuleId().equals(ruleId))
			.findFirst()
			.map(rule -> toDTO(rule, ruleType))
			.orElseThrow();
	}

	private RuleDTO toDTO(final Rule rule, final String ruleType) {
		final List<RuleActionDTO> actions = dslContext.selectFrom(RULE_ACTION)
			.where(RULE_ACTION.RULE_ID.eq(rule.getRuleId()))
			.orderBy(RULE_ACTION.ACTION_ORDER)
			.fetch()
			.stream()
			.map(record -> {
				final List<RuleActionParameterDTO> parameters = dslContext.selectFrom(RULE_ACTION_PARAMETER)
					.where(RULE_ACTION_PARAMETER.RULE_ACTION_ID.eq(record.get(RULE_ACTION.RULE_ACTION_ID)))
					.fetch()
					.stream()
					.map(p -> new RuleActionParameterDTO(
						p.get(RULE_ACTION_PARAMETER.RULE_ACTION_PARAMETER_ID),
						p.get(RULE_ACTION_PARAMETER.CODE),
						p.get(RULE_ACTION_PARAMETER.VALUE),
						p.get(RULE_ACTION_PARAMETER.RULING_ENTITY),
						p.get(RULE_ACTION_PARAMETER.CONDITION_ID)
					))
					.toList();
				return toActionDTO(record, parameters);
			})
			.toList();

		final var constraintRecord = dslContext.selectFrom(RULE_CONSTRAINT)
			.where(RULE_CONSTRAINT.OWNER_ID.eq(rule.getRuleId()))
			.fetchOne();

		final RuleConstraintDTO constraintDTO = constraintRecord != null
			? toConstraintDTO(constraintRecord.getConstraintId())
			: null;

		return new RuleDTO(
			rule.getRuleId(),
			ruleType,
			rule.getDescription(),
			rule.getMessage(),
			new ArrayList<>(rule.getTags()),
			constraintDTO,
			actions
		);
	}

	private RuleConstraintDTO toConstraintDTO(final UUID constraintId) {
		final Map<String, RuleConditionListDTO> conditions = new TreeMap<>();

		dslContext.selectFrom(RULE_CONDITION_LIST)
			.where(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(constraintId))
			.fetch()
			.forEach(listRecord -> {
				final String domainName = listRecord.getDomain().name();
				final UUID listId = listRecord.getConditionListId();

				final var rootCondition = dslContext.selectFrom(RULE_CONDITION)
					.where(RULE_CONDITION.CONDITION_LIST_ID.eq(listId))
					.and(RULE_CONDITION.CODE.eq(domainName))
					.and(RULE_CONDITION.PARENT_CONDITION_ID.isNull())
					.fetchOne();

				final List<RuleConditionDTO> topLevel;
				if(rootCondition != null) {
					topLevel = loadChildConditions(listId, rootCondition.getConditionId());
				}
				else {
					topLevel = loadChildConditions(listId, null);
				}

				conditions.put(domainName, new RuleConditionListDTO(
					listId,
					listRecord.getMode().name(),
					topLevel
				));
			});

		return new RuleConstraintDTO(constraintId, conditions);
	}

	private RuleConditionListDTO toConditionListDTO(final RuleConditionList list, final String domainName) {
		if(list == null) {
			return null;
		}
		final var rootCondition = list.getConditions().stream()
			.filter(c -> domainName.equals(c.getId()))
			.findFirst();

		final var topLevelConditions = rootCondition
			.map(root -> root.getConditions().stream().map(this::toConditionDTO).toList())
			.orElse(list.getConditions().stream().map(this::toConditionDTO).toList());

		return new RuleConditionListDTO(
			list.getRuleConditionListId(),
			list.getMode().name(),
			topLevelConditions
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

	private RuleActionDTO toActionDTO(final RuleActionRecord record, final List<RuleActionParameterDTO> parameters) {
		final UUID conditionUUID = record.get(RULE_ACTION.CONDITION_ID);
		final String rulableEntity = record.get(RULE_ACTION.RULABLE_ENTITY);
		final String actionIdCode = record.get(RULE_ACTION.ACTION_ID_CODE);

		String conditionCode = null;
		String configurationWorkflowId = null;
		String configurationActionId = null;
		String actionId = null;

		if(conditionUUID != null) {
			conditionCode = dslContext
				.select(RULE_CONDITION.CODE)
				.from(RULE_CONDITION)
				.where(RULE_CONDITION.CONDITION_ID.eq(conditionUUID))
				.fetchOne(RULE_CONDITION.CODE);
		}

		if(rulableEntity != null) {
			actionId = actionIdCode;
		}
		else if(conditionCode != null) {
			actionId = actionIdCode;
		}
		else if(conditionUUID != null) {
			configurationWorkflowId = conditionUUID.toString();
			configurationActionId = actionIdCode;
		}
		else {
			actionId = actionIdCode;
		}

		return new RuleActionDTO(
			record.get(RULE_ACTION.RULE_ACTION_ID),
			record.get(RULE_ACTION.CODE),
			jsonMapperService.fromJson(record.get(RULE_ACTION.LABEL), new TypeReference<>() {
			}),
			record.get(RULE_ACTION.OPTIONAL),
			record.get(RULE_ACTION.STATIC_ACTION_ID),
			configurationWorkflowId,
			configurationActionId,
			rulableEntity,
			conditionCode,
			actionId,
			parameters
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

			final UUID rootConditionId = UUID.randomUUID();
			dslContext.insertInto(RULE_CONDITION)
				.set(RULE_CONDITION.CONDITION_ID, rootConditionId)
				.set(RULE_CONDITION.PROJECT_ID, projectId)
				.set(RULE_CONDITION.CONDITION_LIST_ID, listId)
				.set(RULE_CONDITION.PARENT_CONDITION_ID, (UUID) null)
				.set(RULE_CONDITION.CODE, domainName)
				.set(RULE_CONDITION.MODE, RuleConditionMode.OR)
				.set(RULE_CONDITION.INVERSE, false)
				.set(RULE_CONDITION.DEPENDENCY, false)
				.set(RULE_CONDITION.BREAK_TYPE, "NONE")
				.set(RULE_CONDITION.CONDITION_ORDER, 0)
				.execute();

			if(listDTO.conditions() != null) {
				for(int i = 0; i < listDTO.conditions().size(); i++) {
					saveCondition(projectId, listId, rootConditionId, listDTO.conditions().get(i), i);
				}
			}
		});
	}

	private void saveCondition(final UUID projectId, final UUID listId, final UUID parentConditionId, final RuleConditionDTO dto, final int order) {
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
			.set(RULE_CONDITION.CONDITION_ORDER, order)
			.execute();

		if(dto.criterion() != null) {
			saveCriterion(projectId, conditionId, dto.criterion());
		}

		if(dto.conditions() != null) {
			for(int i = 0; i < dto.conditions().size(); i++) {
				saveCondition(projectId, listId, conditionId, dto.conditions().get(i), i);
			}
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
		final Set<String> usedCodes = new HashSet<>();
		for(int i = 0; i < actions.size(); i++) {
			final RuleActionDTO dto = actions.get(i);
			final UUID actionId = dto.ruleActionId() != null ? dto.ruleActionId() : UUID.randomUUID();

			final String code = getString(dto, usedCodes);
			usedCodes.add(code);

			final UUID conditionUUID;
			if(dto.configurationWorkflowId() != null && !dto.configurationWorkflowId().isBlank()) {
				conditionUUID = UUID.fromString(dto.configurationWorkflowId());
			}
			else if(dto.conditionId() != null && !dto.conditionId().isBlank()) {
				conditionUUID = dslContext
					.select(RULE_CONDITION.CONDITION_ID)
					.from(RULE_CONDITION)
					.join(RULE_CONDITION_LIST)
					.on(RULE_CONDITION.CONDITION_LIST_ID.eq(RULE_CONDITION_LIST.CONDITION_LIST_ID))
					.join(RULE_CONSTRAINT)
					.on(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(RULE_CONSTRAINT.CONSTRAINT_ID))
					.where(RULE_CONSTRAINT.OWNER_ID.eq(ruleId))
					.and(RULE_CONDITION.CODE.eq(dto.conditionId()))
					.fetchOne(RULE_CONDITION.CONDITION_ID);
			}
			else {
				conditionUUID = null;
			}

			final String actionIdCode = dto.actionId() != null ? dto.actionId() : dto.configurationActionId();

			dslContext.insertInto(RULE_ACTION)
				.set(RULE_ACTION.RULE_ACTION_ID, actionId)
				.set(RULE_ACTION.PROJECT_ID, projectId)
				.set(RULE_ACTION.RULE_ID, ruleId)
				.set(RULE_ACTION.CODE, code)
				.set(RULE_ACTION.STATIC_ACTION_ID, dto.staticActionId())
				.set(RULE_ACTION.ACTION_ID_CODE, actionIdCode)
				.set(RULE_ACTION.OPTIONAL, dto.optional())
				.set(RULE_ACTION.LABEL, jsonMapperService.toJson(dto.label()))
				.set(RULE_ACTION.CONDITION_ID, conditionUUID)
				.set(RULE_ACTION.RULABLE_ENTITY, dto.rulableEntity())
				.set(RULE_ACTION.ACTION_ORDER, i)
				.execute();

			if(dto.parameters() != null) {
				dto.parameters().forEach(p -> saveParameter(projectId, actionId, p));
			}
		}
	}

	private static @NonNull String getString(final RuleActionDTO dto, final Set<String> usedCodes) {
		final String actionIdentifier = dto.staticActionId() != null
			? dto.staticActionId() : dto.actionId() != null
									 ? dto.actionId() : dto.configurationActionId() != null
														? dto.configurationActionId() : null;

		final String baseCode = (dto.id() != null && !dto.id().isBlank())
			? dto.id() : (actionIdentifier != null ? actionIdentifier : "ACTION");

		String code = baseCode;
		int suffix = 1;
		while(usedCodes.contains(code)) {
			code = baseCode + "_" + (++suffix);
		}
		return code;
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
			deleteConditionsForList(listId);
		}

		dslContext.deleteFrom(RULE_CONDITION_LIST)
			.where(RULE_CONDITION_LIST.CONSTRAINT_ID.eq(constraintId))
			.execute();
	}

	private void deleteConditionsForList(final UUID listId) {
		deleteConditionsRecursive(listId, null);
	}

	private void deleteConditionsRecursive(final UUID listId, final UUID parentConditionId) {
		final var conditionIds = dslContext.select(RULE_CONDITION.CONDITION_ID)
			.from(RULE_CONDITION)
			.where(RULE_CONDITION.CONDITION_LIST_ID.eq(listId))
			.and(parentConditionId == null
				? RULE_CONDITION.PARENT_CONDITION_ID.isNull()
				: RULE_CONDITION.PARENT_CONDITION_ID.eq(parentConditionId))
			.fetch(RULE_CONDITION.CONDITION_ID);

		for(final UUID conditionId : conditionIds) {
			deleteConditionsRecursive(listId, conditionId);

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

			dslContext.deleteFrom(RULE_CONDITION)
				.where(RULE_CONDITION.CONDITION_ID.eq(conditionId))
				.execute();
		}
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

	private List<RuleConditionDTO> loadChildConditions(final UUID listId, final UUID parentId) {
		return dslContext.selectFrom(RULE_CONDITION)
			.where(RULE_CONDITION.CONDITION_LIST_ID.eq(listId))
			.and(parentId == null
				? RULE_CONDITION.PARENT_CONDITION_ID.isNull()
				: RULE_CONDITION.PARENT_CONDITION_ID.eq(parentId))
			.orderBy(RULE_CONDITION.CONDITION_ORDER)
			.fetch()
			.stream()
			.map(c -> {
				final var criterion = dslContext.selectFrom(RULE_CRITERION)
					.where(RULE_CRITERION.CONDITION_ID.eq(c.getConditionId()))
					.fetchOne();
				final RuleCriterionDTO criterionDTO = criterion == null ? null : new RuleCriterionDTO(
					criterion.getCriterionId(),
					criterion.getProperty(),
					criterion.getOperator(),
					dslContext.selectFrom(RULE_CRITERION_VALUE)
					.where(RULE_CRITERION_VALUE.CRITERION_ID.eq(criterion.getCriterionId()))
					.orderBy(RULE_CRITERION_VALUE.VALUE_ORDER)
					.fetch(RULE_CRITERION_VALUE.VALUE_TEXT)
				);
				return new RuleConditionDTO(
					c.getConditionId(),
					c.getCode(),
					criterionDTO,
					c.getInverse(),
					c.getDependency(),
					c.getBreakType(),
					c.getMode().name(),
					loadChildConditions(listId, c.getConditionId())
				);
			})
			.toList();
	}
}
