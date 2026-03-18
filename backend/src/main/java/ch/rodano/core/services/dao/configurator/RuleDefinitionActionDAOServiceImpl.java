package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.RuleDefinitionActionDTO;
import ch.rodano.api.config.RuleDefinitionActionParameterDTO;
import ch.rodano.core.model.jooq.tables.records.RuleDefinitionActionRecord;

import static ch.rodano.core.model.jooq.tables.RuleDefinitionAction.RULE_DEFINITION_ACTION;
import static ch.rodano.core.model.jooq.tables.RuleDefinitionActionParameter.RULE_DEFINITION_ACTION_PARAMETER;

@Repository
public class RuleDefinitionActionDAOServiceImpl implements RuleDefinitionActionDAOService {

	private final DSLContext dslContext;

	public RuleDefinitionActionDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "ruleDefinitionActions", key = "#projectId.toString()")
	public List<RuleDefinitionActionDTO> getRuleDefinitionActions(final UUID projectId) {
		final var records = dslContext
			.selectFrom(RULE_DEFINITION_ACTION)
			.where(RULE_DEFINITION_ACTION.PROJECT_ID.eq(projectId))
			.orderBy(RULE_DEFINITION_ACTION.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var actionIds = records.map(RuleDefinitionActionRecord::getRuleDefinitionActionId);
		final var parameterMap = loadParameters(projectId, actionIds);

		return records.map(record -> mapToDTO(record, parameterMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "ruleDefinitionAction", key = "#projectId.toString() + ':' + #ruleDefinitionActionId.toString()")
	public RuleDefinitionActionDTO getRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId) {
		final var record = dslContext
			.selectFrom(RULE_DEFINITION_ACTION)
			.where(RULE_DEFINITION_ACTION.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION.RULE_DEFINITION_ACTION_ID.eq(ruleDefinitionActionId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var actionIds = List.of(ruleDefinitionActionId);
		final var parameterMap = loadParameters(projectId, actionIds);

		return mapToDTO(record, parameterMap);
	}

	@Override
	@Transactional
	@CacheEvict(value = "ruleDefinitionActions", key = "#projectId.toString()")
	public RuleDefinitionActionDTO createRuleDefinitionAction(final UUID projectId, final RuleDefinitionActionDTO dto) {
		final var ruleDefinitionActionId = dto.getRuleDefinitionActionId() != null ? dto.getRuleDefinitionActionId() : UUID.randomUUID();

		dslContext.insertInto(RULE_DEFINITION_ACTION)
			.set(RULE_DEFINITION_ACTION.RULE_DEFINITION_ACTION_ID, ruleDefinitionActionId)
			.set(RULE_DEFINITION_ACTION.PROJECT_ID, projectId)
			.set(RULE_DEFINITION_ACTION.CODE, dto.getId())
			.set(RULE_DEFINITION_ACTION.LABEL, dto.getLabel())
			.set(RULE_DEFINITION_ACTION.ENTITY_ID, dto.getEntity())
			.execute();

		replaceChildren(projectId, ruleDefinitionActionId, dto);

		return getRuleDefinitionAction(projectId, ruleDefinitionActionId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "ruleDefinitionActions", key = "#projectId.toString()"),
		@CacheEvict(value = "ruleDefinitionAction", key = "#projectId.toString() + ':' + #ruleDefinitionActionId.toString()")
	})
	public RuleDefinitionActionDTO updateRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId, final RuleDefinitionActionDTO dto) {
		dslContext.update(RULE_DEFINITION_ACTION)
			.set(RULE_DEFINITION_ACTION.CODE, dto.getId())
			.set(RULE_DEFINITION_ACTION.LABEL, dto.getLabel())
			.set(RULE_DEFINITION_ACTION.ENTITY_ID, dto.getEntity())
			.where(RULE_DEFINITION_ACTION.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION.RULE_DEFINITION_ACTION_ID.eq(ruleDefinitionActionId))
			.execute();

		replaceChildren(projectId, ruleDefinitionActionId, dto);

		return getRuleDefinitionAction(projectId, ruleDefinitionActionId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "ruleDefinitionActions", key = "#projectId.toString()"),
		@CacheEvict(value = "ruleDefinitionAction", key = "#projectId.toString() + ':' + #ruleDefinitionActionId.toString()")
	})
	public void deleteRuleDefinitionAction(final UUID projectId, final UUID ruleDefinitionActionId) {
		dslContext.deleteFrom(RULE_DEFINITION_ACTION_PARAMETER)
			.where(RULE_DEFINITION_ACTION_PARAMETER.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION_PARAMETER.RULE_DEFINITION_ACTION_ID.eq(ruleDefinitionActionId))
			.execute();

		dslContext.deleteFrom(RULE_DEFINITION_ACTION)
			.where(RULE_DEFINITION_ACTION.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION.RULE_DEFINITION_ACTION_ID.eq(ruleDefinitionActionId))
			.execute();
	}

	private void replaceChildren(final UUID projectId, final UUID ruleDefinitionActionId, final RuleDefinitionActionDTO dto) {
		dslContext.deleteFrom(RULE_DEFINITION_ACTION_PARAMETER)
			.where(RULE_DEFINITION_ACTION_PARAMETER.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION_PARAMETER.RULE_DEFINITION_ACTION_ID.eq(ruleDefinitionActionId))
			.execute();

		if(dto.getParameters() != null) {
			for(int i = 0; i < dto.getParameters().size(); i++) {
				final var parameter = dto.getParameters().get(i);

				dslContext.insertInto(RULE_DEFINITION_ACTION_PARAMETER)
					.set(RULE_DEFINITION_ACTION_PARAMETER.RULE_DEFINITION_ACTION_ID, ruleDefinitionActionId)
					.set(RULE_DEFINITION_ACTION_PARAMETER.PROJECT_ID, projectId)
					.set(RULE_DEFINITION_ACTION_PARAMETER.PARAM_CODE, parameter.getId())
					.set(RULE_DEFINITION_ACTION_PARAMETER.LABEL, parameter.getLabel())
					.set(RULE_DEFINITION_ACTION_PARAMETER.DATA_ENTITY, parameter.getDataEntity())
					.set(RULE_DEFINITION_ACTION_PARAMETER.CONFIGURATION_ENTITY, parameter.getConfigurationEntity())
					.set(RULE_DEFINITION_ACTION_PARAMETER.OPTIONS, parameter.getOptions())
					.set(RULE_DEFINITION_ACTION_PARAMETER.SORT_ORDER, i)
					.execute();
			}
		}
	}

	private Map<UUID, List<RuleDefinitionActionParameterDTO>> loadParameters(final UUID projectId, final List<UUID> ruleDefinitionActionIds) {
		if(ruleDefinitionActionIds == null || ruleDefinitionActionIds.isEmpty()) {
			return Map.of();
		}

		final var records = dslContext
			.selectFrom(RULE_DEFINITION_ACTION_PARAMETER)
			.where(RULE_DEFINITION_ACTION_PARAMETER.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_ACTION_PARAMETER.RULE_DEFINITION_ACTION_ID.in(ruleDefinitionActionIds))
			.orderBy(RULE_DEFINITION_ACTION_PARAMETER.SORT_ORDER.asc())
			.fetch();

		if(records.isEmpty()) {
			return Map.of();
		}

		final Map<UUID, List<RuleDefinitionActionParameterDTO>> result = new HashMap<>();
		for(final var record : records) {
			final var dto = new RuleDefinitionActionParameterDTO();
			dto.setRuleDefinitionActionId(record.getRuleDefinitionActionId());
			dto.setId(record.getParamCode());
			dto.setLabel(record.getLabel());
			dto.setDataEntity(record.getDataEntity());
			dto.setConfigurationEntity(record.getConfigurationEntity());
			dto.setSortOrder(record.getSortOrder());
			result.computeIfAbsent(record.getRuleDefinitionActionId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private RuleDefinitionActionDTO mapToDTO(
		final RuleDefinitionActionRecord record,
		final Map<UUID, List<RuleDefinitionActionParameterDTO>> parameterMap
	) {
		final var dto = new RuleDefinitionActionDTO();
		dto.setRuleDefinitionActionId(record.getRuleDefinitionActionId());
		dto.setId(record.getCode());
		dto.setLabel(record.getLabel());
		dto.setEntity(record.getEntityId());
		dto.setParameters(parameterMap.getOrDefault(record.getRuleDefinitionActionId(), List.of()));
		return dto;
	}
}
