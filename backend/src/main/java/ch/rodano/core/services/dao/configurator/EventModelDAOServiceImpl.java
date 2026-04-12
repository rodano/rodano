package ch.rodano.core.services.dao.configurator;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.configuration.model.event.DateAggregationFunction;
import ch.rodano.core.model.jooq.tables.records.EventModelRecord;

import static ch.rodano.core.model.jooq.tables.Event.EVENT;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;

@Repository
public class EventModelDAOServiceImpl implements EventModelDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public EventModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<EventModelDTO> getEventModels(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getEventModelsFull(projectId);
			case VIEW_SUMMARY -> getEventModelsSummary(projectId);
			default -> getEventModelsSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventModels", key = "#projectId.toString() + ':summary'")
	public List<EventModelDTO> getEventModelsSummary(final UUID projectId) {
		final var eventModelRecords = dslContext
			.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(EVENT_MODEL.NUMBER.asc(), EVENT_MODEL.CODE.asc())
			.fetch();

		if(eventModelRecords.isEmpty()) {
			return List.of();
		}

		return eventModelRecords.map(record -> mapToDTO(
			record, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventModels", key = "#projectId.toString() + ':full'")
	public List<EventModelDTO> getEventModelsFull(final UUID projectId) {
		final var eventModelRecords = dslContext
			.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(EVENT_MODEL.NUMBER.asc(), EVENT_MODEL.CODE.asc())
			.fetch();

		if(eventModelRecords.isEmpty()) {
			return List.of();
		}

		final var eventModelIds = eventModelRecords.map(EventModelRecord::getEventModelId);
		final var datasetMap = loadDatasetModelIds(projectId, eventModelIds);
		final var formMap = loadFormModelIds(projectId, eventModelIds);
		final var workflowMap = loadWorkflowIds(projectId, eventModelIds);
		final var deadlineRefMap = loadDeadlineReferenceIds(projectId, eventModelIds);
		final var blockedMap = loadBlockedEventModelIds(projectId, eventModelIds);
		final var impliedMap = loadImpliedEventModelIds(projectId, eventModelIds);

		return eventModelRecords.map(record -> mapToDTO(
			record, datasetMap, formMap, workflowMap, deadlineRefMap, blockedMap, impliedMap
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "eventModel", key = "#projectId.toString() + ':' + #eventModelId.toString()")
	public EventModelDTO getEventModel(final UUID projectId, final UUID eventModelId) {
		final var record = dslContext
			.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var eventModelIds = List.of(eventModelId);
		final var datasetMap = loadDatasetModelIds(projectId, eventModelIds);
		final var formMap = loadFormModelIds(projectId, eventModelIds);
		final var workflowMap = loadWorkflowIds(projectId, eventModelIds);
		final var deadlineRefMap = loadDeadlineReferenceIds(projectId, eventModelIds);
		final var blockedMap = loadBlockedEventModelIds(projectId, eventModelIds);
		final var impliedMap = loadImpliedEventModelIds(projectId, eventModelIds);

		return mapToDTO(record, datasetMap, formMap, workflowMap, deadlineRefMap, blockedMap, impliedMap);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':full'")
	})
	public EventModelDTO createEventModel(final UUID projectId, final EventModelDTO dto) {
		final var eventModelId = dto.getEventModelId() != null ? dto.getEventModelId() : UUID.randomUUID();

		dslContext.insertInto(EVENT_MODEL)
			.set(EVENT_MODEL.PROJECT_ID, projectId)
			.set(EVENT_MODEL.EVENT_MODEL_ID, eventModelId)
			.set(EVENT_MODEL.CODE, dto.getId())
			.set(EVENT_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(EVENT_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(EVENT_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(EVENT_MODEL.EVENT_GROUP_ID, dto.getEventGroupId())
			.set(EVENT_MODEL.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(EVENT_MODEL.INCEPTIVE, dto.isInceptive())
			.set(EVENT_MODEL.NUMBER, dto.getNumber())
			.set(EVENT_MODEL.MANDATORY, dto.isMandatory())
			.set(EVENT_MODEL.MAX_OCCURRENCE, dto.getMaxOccurrence())
			.set(EVENT_MODEL.PREVENT_ADD, dto.isPreventAdd())
			.set(EVENT_MODEL.DEADLINE_VALUE, dto.getDeadline())
			.set(EVENT_MODEL.DEADLINE_UNIT, dto.getDeadlineUnit() != null ? dto.getDeadlineUnit().name() : null)
			.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, dto.getDeadlineAggregationFunction() != null ? dto.getDeadlineAggregationFunction().name() : null)
			.set(EVENT_MODEL.INTERVAL_VALUE, dto.getInterval())
			.set(EVENT_MODEL.INTERVAL_UNIT, dto.getIntervalUnit() != null ? dto.getIntervalUnit().name() : null)
			.set(EVENT_MODEL.LABEL_PATTERN, dto.getLabelPattern())
			.set(EVENT_MODEL.ICON, dto.getIcon())
			.execute();

		replaceRelations(projectId, eventModelId, dto);
		return getEventModel(projectId, eventModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "eventModel", key = "#projectId.toString() + ':' + #eventModelId.toString()")
	})
	public EventModelDTO updateEventModel(final UUID projectId, final UUID eventModelId, final EventModelDTO dto) {
		dslContext.update(EVENT_MODEL)
			.set(EVENT_MODEL.CODE, dto.getId())
			.set(EVENT_MODEL.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(EVENT_MODEL.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(EVENT_MODEL.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(EVENT_MODEL.EVENT_GROUP_ID, dto.getEventGroupId())
			.set(EVENT_MODEL.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(EVENT_MODEL.INCEPTIVE, dto.isInceptive())
			.set(EVENT_MODEL.NUMBER, dto.getNumber())
			.set(EVENT_MODEL.MANDATORY, dto.isMandatory())
			.set(EVENT_MODEL.MAX_OCCURRENCE, dto.getMaxOccurrence())
			.set(EVENT_MODEL.PREVENT_ADD, dto.isPreventAdd())
			.set(EVENT_MODEL.DEADLINE_VALUE, dto.getDeadline())
			.set(EVENT_MODEL.DEADLINE_UNIT, dto.getDeadlineUnit() != null ? dto.getDeadlineUnit().name() : null)
			.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, dto.getDeadlineAggregationFunction() != null ? dto.getDeadlineAggregationFunction().name() : null)
			.set(EVENT_MODEL.INTERVAL_VALUE, dto.getInterval())
			.set(EVENT_MODEL.INTERVAL_UNIT, dto.getIntervalUnit() != null ? dto.getIntervalUnit().name() : null)
			.set(EVENT_MODEL.LABEL_PATTERN, dto.getLabelPattern())
			.set(EVENT_MODEL.ICON, dto.getIcon())
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		replaceRelations(projectId, eventModelId, dto);
		return getEventModel(projectId, eventModelId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "eventModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "eventModel", key = "#projectId.toString() + ':' + #eventModelId.toString()")
	})
	public void deleteEventModel(final UUID projectId, final UUID eventModelId) {
		dslContext.deleteFrom(EVENT_MODEL_DATASET_MODEL).where(EVENT_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.eq(eventModelId)).execute();
		dslContext.deleteFrom(EVENT_MODEL_FORM_MODEL).where(EVENT_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.eq(eventModelId)).execute();
		dslContext.deleteFrom(EVENT_MODEL_WORKFLOW).where(EVENT_MODEL_WORKFLOW.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.eq(eventModelId)).execute();
		dslContext.deleteFrom(EVENT_MODEL_DEADLINE_REFERENCE).where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.eq(eventModelId)).execute();
		dslContext.deleteFrom(EVENT_MODEL_BLOCKED_EVENT).where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.eq(eventModelId)).execute();
		dslContext.deleteFrom(EVENT_MODEL_IMPLIED_EVENT).where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId)).and(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.eq(eventModelId)).execute();

		dslContext.deleteFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPatientData(final UUID projectId, final UUID eventModelId) {
		return dslContext.fetchExists(
			dslContext.selectOne()
				.from(EVENT)
				.where(EVENT.PROJECT_ID.eq(projectId))
				.and(EVENT.EVENT_MODEL_ID.eq(eventModelId))
		);
	}

	private void replaceRelations(final UUID projectId, final UUID eventModelId, final EventModelDTO dto) {
		dslContext.deleteFrom(EVENT_MODEL_DATASET_MODEL)
			.where(EVENT_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_FORM_MODEL)
			.where(EVENT_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_WORKFLOW)
			.where(EVENT_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_DEADLINE_REFERENCE)
			.where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_BLOCKED_EVENT)
			.where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_IMPLIED_EVENT)
			.where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		batchInsert(projectId, eventModelId, dto);
	}

	private void batchInsert(final UUID projectId, final UUID eventModelId, final EventModelDTO dto) {
		if(dto.getDatasetModelIds() != null) {
			for(final var datasetId : dto.getDatasetModelIds()) {
				dslContext.insertInto(EVENT_MODEL_DATASET_MODEL)
					.set(EVENT_MODEL_DATASET_MODEL.PROJECT_ID, projectId)
					.set(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID, datasetId)
					.execute();
			}
		}

		if(dto.getFormModelIds() != null) {
			for(final var formId : dto.getFormModelIds()) {
				dslContext.insertInto(EVENT_MODEL_FORM_MODEL)
					.set(EVENT_MODEL_FORM_MODEL.PROJECT_ID, projectId)
					.set(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID, formId)
					.execute();
			}
		}

		if(dto.getWorkflowIds() != null) {
			for(final var workflowId : dto.getWorkflowIds()) {
				dslContext.insertInto(EVENT_MODEL_WORKFLOW)
					.set(EVENT_MODEL_WORKFLOW.PROJECT_ID, projectId)
					.set(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
					.execute();
			}
		}

		if(dto.getDeadlineReferenceEventModelIds() != null) {
			for(final var refId : dto.getDeadlineReferenceEventModelIds()) {
				dslContext.insertInto(EVENT_MODEL_DEADLINE_REFERENCE)
					.set(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID, projectId)
					.set(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID, refId)
					.execute();
			}
		}

		if(dto.getBlockedEventModelIds() != null) {
			for(final var blockedId : dto.getBlockedEventModelIds()) {
				dslContext.insertInto(EVENT_MODEL_BLOCKED_EVENT)
					.set(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID, projectId)
					.set(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID, blockedId)
					.execute();
			}
		}

		if(dto.getImpliedEventModelIds() != null) {
			for(final var impliedId : dto.getImpliedEventModelIds()) {
				dslContext.insertInto(EVENT_MODEL_IMPLIED_EVENT)
					.set(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID, projectId)
					.set(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID, eventModelId)
					.set(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID, impliedId)
					.execute();
			}
		}
	}

	private EventModelDTO mapToDTO(
		final EventModelRecord record,
		final Map<UUID, List<UUID>> datasetMap,
		final Map<UUID, List<UUID>> formMap,
		final Map<UUID, List<UUID>> workflowMap,
		final Map<UUID, List<UUID>> deadlineRefMap,
		final Map<UUID, List<UUID>> blockedMap,
		final Map<UUID, List<UUID>> impliedMap
	) {
		final var dto = new EventModelDTO();

		dto.setEventModelId(record.getEventModelId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));

		dto.setEventGroupId(record.getEventGroupId());
		dto.setScopeModelId(record.getScopeModelId());

		dto.setInceptive(record.getInceptive());
		dto.setNumber(record.getNumber());
		dto.setMandatory(record.getMandatory());
		dto.setMaxOccurrence(record.getMaxOccurrence());
		dto.setPreventAdd(record.getPreventAdd());

		dto.setDeadline(record.getDeadlineValue());
		dto.setDeadlineUnit(record.getDeadlineUnit() != null ? ChronoUnit.valueOf(record.getDeadlineUnit()) : null);
		dto.setDeadlineAggregationFunction(record.getDeadlineAggrFnct() != null ? DateAggregationFunction.valueOf(record.getDeadlineAggrFnct()) : null);

		dto.setInterval(record.getIntervalValue());
		dto.setIntervalUnit(record.getIntervalUnit() != null ? ChronoUnit.valueOf(record.getIntervalUnit()) : null);

		dto.setLabelPattern(record.getLabelPattern());
		dto.setIcon(record.getIcon());

		final var eventModelId = record.getEventModelId();
		dto.setDatasetModelIds(datasetMap.getOrDefault(eventModelId, List.of()));
		dto.setFormModelIds(formMap.getOrDefault(eventModelId, List.of()));
		dto.setWorkflowIds(workflowMap.getOrDefault(eventModelId, List.of()));
		dto.setDeadlineReferenceEventModelIds(deadlineRefMap.getOrDefault(eventModelId, List.of()));
		dto.setBlockedEventModelIds(blockedMap.getOrDefault(eventModelId, List.of()));
		dto.setImpliedEventModelIds(impliedMap.getOrDefault(eventModelId, List.of()));

		return dto;
	}

	private Map<UUID, List<UUID>> loadDatasetModelIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_DATASET_MODEL,
			EVENT_MODEL_DATASET_MODEL.PROJECT_ID,
			EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID,
			EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadFormModelIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_FORM_MODEL,
			EVENT_MODEL_FORM_MODEL.PROJECT_ID,
			EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID,
			EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadWorkflowIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_WORKFLOW,
			EVENT_MODEL_WORKFLOW.PROJECT_ID,
			EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID,
			EVENT_MODEL_WORKFLOW.WORKFLOW_ID
		);
	}

	private Map<UUID, List<UUID>> loadDeadlineReferenceIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_DEADLINE_REFERENCE,
			EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID,
			EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID,
			EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadBlockedEventModelIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_BLOCKED_EVENT,
			EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID,
			EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID,
			EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID
		);
	}

	private Map<UUID, List<UUID>> loadImpliedEventModelIds(final UUID projectId, final List<UUID> eventModelIds) {
		return fetchGroupedIds(
			projectId,
			eventModelIds,
			EVENT_MODEL_IMPLIED_EVENT,
			EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID,
			EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID,
			EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID
		);
	}

	private <R extends Record, T extends Table<R>> Map<UUID, List<UUID>> fetchGroupedIds(
		final UUID projectId,
		final List<UUID> eventModelIds,
		final T table,
		final TableField<R, UUID> projectIdField,
		final TableField<R, UUID> eventModelIdField,
		final TableField<R, UUID> valueField
	) {
		if(eventModelIds == null || eventModelIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(eventModelIdField, valueField)
			.from(table)
			.where(projectIdField.eq(projectId))
			.and(eventModelIdField.in(eventModelIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}

		result.replaceAll((_, v) -> {
			final var copy = new ArrayList<>(v);
			Collections.sort(copy);
			return copy;
		});

		return result;
	}
}
