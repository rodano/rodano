package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.api.config.RuleDefinitionPropertyDTO;
import ch.rodano.core.model.jooq.tables.records.RuleDefinitionPropertyRecord;

import static ch.rodano.core.model.jooq.tables.RuleDefinitionProperty.RULE_DEFINITION_PROPERTY;

@Repository
public class RuleDefinitionPropertyDAOServiceImpl implements RuleDefinitionPropertyDAOService {

	private final DSLContext dslContext;

	public RuleDefinitionPropertyDAOServiceImpl(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "ruleDefinitionProperties", key = "#projectId.toString()")
	public List<RuleDefinitionPropertyDTO> getRuleDefinitionProperties(final UUID projectId) {
		final var records = dslContext
			.selectFrom(RULE_DEFINITION_PROPERTY)
			.where(RULE_DEFINITION_PROPERTY.PROJECT_ID.eq(projectId))
			.orderBy(RULE_DEFINITION_PROPERTY.CODE)
			.fetch();

		return records.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "ruleDefinitionProperty", key = "#projectId.toString() + ':' + #ruleDefinitionPropertyId.toString()")
	public RuleDefinitionPropertyDTO getRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId) {
		final var record = dslContext
			.selectFrom(RULE_DEFINITION_PROPERTY)
			.where(RULE_DEFINITION_PROPERTY.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_PROPERTY.RULE_DEFINITION_PROPERTY_ID.eq(ruleDefinitionPropertyId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = "ruleDefinitionProperties", key = "#projectId.toString()")
	public RuleDefinitionPropertyDTO createRuleDefinitionProperty(final UUID projectId, final RuleDefinitionPropertyDTO dto) {
		final var ruleDefinitionPropertyId = dto.getRuleDefinitionPropertyId() != null ? dto.getRuleDefinitionPropertyId() : UUID.randomUUID();

		dslContext.insertInto(RULE_DEFINITION_PROPERTY)
			.set(RULE_DEFINITION_PROPERTY.RULE_DEFINITION_PROPERTY_ID, ruleDefinitionPropertyId)
			.set(RULE_DEFINITION_PROPERTY.PROJECT_ID, projectId)
			.set(RULE_DEFINITION_PROPERTY.CODE, dto.getId())
			.set(RULE_DEFINITION_PROPERTY.LABEL, dto.getLabel())
			.set(RULE_DEFINITION_PROPERTY.ENTITY_ID, dto.getEntity())
			.set(RULE_DEFINITION_PROPERTY.TARGET, dto.getTarget())
			.set(RULE_DEFINITION_PROPERTY.TYPE, dto.getType())
			.set(RULE_DEFINITION_PROPERTY.CONFIGURATION_ENTITY, dto.getConfigurationEntity())
			.set(RULE_DEFINITION_PROPERTY.OPTIONS, dto.getOptions())
			.execute();

		return getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "ruleDefinitionProperties", key = "#projectId.toString()"),
		@CacheEvict(value = "ruleDefinitionProperty", key = "#projectId.toString() + ':' + #ruleDefinitionPropertyId.toString()")
	})
	public RuleDefinitionPropertyDTO updateRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId, final RuleDefinitionPropertyDTO dto) {
		dslContext.update(RULE_DEFINITION_PROPERTY)
			.set(RULE_DEFINITION_PROPERTY.CODE, dto.getId())
			.set(RULE_DEFINITION_PROPERTY.LABEL, dto.getLabel())
			.set(RULE_DEFINITION_PROPERTY.ENTITY_ID, dto.getEntity())
			.set(RULE_DEFINITION_PROPERTY.TARGET, dto.getTarget())
			.set(RULE_DEFINITION_PROPERTY.TYPE, dto.getType())
			.set(RULE_DEFINITION_PROPERTY.CONFIGURATION_ENTITY, dto.getConfigurationEntity())
			.set(RULE_DEFINITION_PROPERTY.OPTIONS, dto.getOptions())
			.where(RULE_DEFINITION_PROPERTY.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_PROPERTY.RULE_DEFINITION_PROPERTY_ID.eq(ruleDefinitionPropertyId))
			.execute();

		return getRuleDefinitionProperty(projectId, ruleDefinitionPropertyId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "ruleDefinitionProperties", key = "#projectId.toString()"),
		@CacheEvict(value = "ruleDefinitionProperty", key = "#projectId.toString() + ':' + #ruleDefinitionPropertyId.toString()")
	})
	public void deleteRuleDefinitionProperty(final UUID projectId, final UUID ruleDefinitionPropertyId) {
		dslContext.deleteFrom(RULE_DEFINITION_PROPERTY)
			.where(RULE_DEFINITION_PROPERTY.PROJECT_ID.eq(projectId))
			.and(RULE_DEFINITION_PROPERTY.RULE_DEFINITION_PROPERTY_ID.eq(ruleDefinitionPropertyId))
			.execute();
	}

	private RuleDefinitionPropertyDTO mapToDTO(final RuleDefinitionPropertyRecord record) {
		final var dto = new RuleDefinitionPropertyDTO();
		dto.setRuleDefinitionPropertyId(record.getRuleDefinitionPropertyId());
		dto.setId(record.getCode());
		dto.setLabel(record.getLabel());
		dto.setEntity(record.getEntityId());
		dto.setTarget(record.getTarget());
		dto.setType(record.getType());
		dto.setConfigurationEntity(record.getConfigurationEntity());
		dto.setOptions(record.getOptions());

		return dto;
	}
}
