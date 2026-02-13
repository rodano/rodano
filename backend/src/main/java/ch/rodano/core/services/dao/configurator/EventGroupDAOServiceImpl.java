package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.EventGroupDTO;
import ch.rodano.core.model.jooq.tables.records.EventGroupRecord;

import static ch.rodano.core.model.jooq.tables.EventGroup.EVENT_GROUP;

@Repository
public class EventGroupDAOServiceImpl implements EventGroupDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public EventGroupDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventGroups", key = "#projectId")
	public List<EventGroupDTO> getEventGroups(final UUID projectId) {
		final var eventGroups = dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.orderBy(EVENT_GROUP.CODE)
			.fetch();

		return eventGroups.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventGroup", key = "#projectId + '-' + #eventGroupId")
	public EventGroupDTO getEventGroup(final UUID projectId, final UUID eventGroupId) {
		final var record = dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.and(EVENT_GROUP.EVENT_GROUP_ID.eq(eventGroupId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "eventGroups", "eventGroup", "eventGroupsByScopeModel" }, allEntries = true)
	public EventGroupDTO createEventGroup(final UUID projectId, final EventGroupDTO eventGroup) {
		final var eventGroupId = eventGroup.getEventGroupId() != null
			? eventGroup.getEventGroupId()
			: UUID.randomUUID();

		dslContext.insertInto(EVENT_GROUP)
			.set(EVENT_GROUP.EVENT_GROUP_ID, eventGroupId)
			.set(EVENT_GROUP.PROJECT_ID, projectId)
			.set(EVENT_GROUP.CODE, eventGroup.getId())
			.set(EVENT_GROUP.SCOPE_MODEL_ID, eventGroup.getScopeModelId())
			.set(EVENT_GROUP.SHORTNAME, jsonMapperService.toJson(eventGroup.getShortname()))
			.set(EVENT_GROUP.LONGNAME, jsonMapperService.toJson(eventGroup.getLongname()))
			.set(EVENT_GROUP.DESCRIPTION, jsonMapperService.toJson(eventGroup.getDescription()))
			.execute();

		return getEventGroup(projectId, eventGroupId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "eventGroups", "eventGroup", "eventGroupsByScopeModel" }, allEntries = true)
	public EventGroupDTO updateEventGroup(final UUID projectId, final UUID eventGroupId, final EventGroupDTO eventGroup) {
		dslContext.update(EVENT_GROUP)
			.set(EVENT_GROUP.CODE, eventGroup.getId())
			.set(EVENT_GROUP.SCOPE_MODEL_ID, eventGroup.getScopeModelId())
			.set(EVENT_GROUP.SHORTNAME, jsonMapperService.toJson(eventGroup.getShortname()))
			.set(EVENT_GROUP.LONGNAME, jsonMapperService.toJson(eventGroup.getLongname()))
			.set(EVENT_GROUP.DESCRIPTION, jsonMapperService.toJson(eventGroup.getDescription()))
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.and(EVENT_GROUP.EVENT_GROUP_ID.eq(eventGroupId))
			.execute();

		return getEventGroup(projectId, eventGroupId);
	}

	@Override
	@Transactional
	@CacheEvict(value = { "eventGroups", "eventGroup", "eventGroupsByScopeModel" }, allEntries = true)
	public void deleteEventGroup(final UUID projectId, final UUID eventGroupId) {
		dslContext.deleteFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.and(EVENT_GROUP.EVENT_GROUP_ID.eq(eventGroupId))
			.execute();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventGroupsByScopeModel", key = "#projectId + '-' + #scopeModelId")
	public List<EventGroupDTO> getEventGroupsByScopeModel(final UUID projectId, final UUID scopeModelId) {
		final var eventGroups = dslContext.selectFrom(EVENT_GROUP)
			.where(EVENT_GROUP.PROJECT_ID.eq(projectId))
			.and(EVENT_GROUP.SCOPE_MODEL_ID.eq(scopeModelId))
			.orderBy(EVENT_GROUP.CODE)
			.fetch();

		return eventGroups.stream()
			.map(this::mapToDTO)
			.collect(Collectors.toList());
	}

	private EventGroupDTO mapToDTO(final EventGroupRecord record) {
		final var dto = new EventGroupDTO();
		dto.setEventGroupId(record.getEventGroupId());
		dto.setId(record.getCode());
		dto.setScopeModelId(record.getScopeModelId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));

		return dto;
	}
}
