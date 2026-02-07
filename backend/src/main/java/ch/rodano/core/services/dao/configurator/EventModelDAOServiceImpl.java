package ch.rodano.core.services.dao.configurator;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.configuration.model.event.DateAggregationFunction;
import ch.rodano.core.model.jooq.tables.records.EventModelRecord;

import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelBlockedEvent.EVENT_MODEL_BLOCKED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelDatasetModel.EVENT_MODEL_DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelDeadlineReference.EVENT_MODEL_DEADLINE_REFERENCE;
import static ch.rodano.core.model.jooq.tables.EventModelFormModel.EVENT_MODEL_FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModelImpliedEvent.EVENT_MODEL_IMPLIED_EVENT;
import static ch.rodano.core.model.jooq.tables.EventModelWorkflow.EVENT_MODEL_WORKFLOW;

@Repository
public class EventModelDAOServiceImpl implements EventModelDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public EventModelDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	public List<EventModelDTO> getEventModels(final UUID projectId) {
		final var eventModels = dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(EVENT_MODEL.CODE)
			.fetch();

		return eventModels.stream()
			.map(record -> mapToDTO(record, projectId))
			.collect(Collectors.toList());
	}

	@Override
	public EventModelDTO getEventModel(final UUID projectId, final UUID eventModelId) {
		final var record = dslContext.selectFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTO(record, projectId);
	}

	@Override
	public EventModelDTO createEventModel(final UUID projectId, final EventModelDTO eventModel) {
		final var eventModelId = eventModel.getEventModelId() != null
			? eventModel.getEventModelId()
			: UUID.randomUUID();

		dslContext.insertInto(EVENT_MODEL)
			.set(EVENT_MODEL.EVENT_MODEL_ID, eventModelId)
			.set(EVENT_MODEL.PROJECT_ID, projectId)
			.set(EVENT_MODEL.CODE, eventModel.getId())
			.set(EVENT_MODEL.EVENT_GROUP_ID, eventModel.getEventGroupId())
			.set(EVENT_MODEL.SCOPE_MODEL_ID, eventModel.getScopeModelId())
			.set(EVENT_MODEL.SHORTNAME, jsonMapperService.toJson(eventModel.getShortname()))
			.set(EVENT_MODEL.LONGNAME, jsonMapperService.toJson(eventModel.getLongname()))
			.set(EVENT_MODEL.DESCRIPTION, jsonMapperService.toJson(eventModel.getDescription()))
			.set(EVENT_MODEL.INCEPTIVE, eventModel.isInceptive())
			.set(EVENT_MODEL.NUMBER, eventModel.getNumber())
			.set(EVENT_MODEL.MANDATORY, eventModel.isMandatory())
			.set(EVENT_MODEL.MAX_OCCURRENCE, eventModel.getMaxOccurrence())
			.set(EVENT_MODEL.PREVENT_ADD, eventModel.isPreventAdd())
			.set(EVENT_MODEL.DEADLINE_VALUE, eventModel.getDeadline())
			.set(EVENT_MODEL.DEADLINE_UNIT, eventModel.getDeadlineUnit() != null ? eventModel.getDeadlineUnit().name() : null)
			.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, eventModel.getDeadlineAggregationFunction() != null ? eventModel.getDeadlineAggregationFunction().name() : null)
			.set(EVENT_MODEL.INTERVAL_VALUE, eventModel.getInterval())
			.set(EVENT_MODEL.INTERVAL_UNIT, eventModel.getIntervalUnit() != null ? eventModel.getIntervalUnit().name() : null)
			.set(EVENT_MODEL.LABEL_PATTERN, eventModel.getLabelPattern())
			.set(EVENT_MODEL.ICON, eventModel.getIcon())
			.execute();

		insertDatasetModels(projectId, eventModelId, eventModel.getDatasetModelIds());
		insertFormModels(projectId, eventModelId, eventModel.getFormModelIds());
		insertWorkflows(projectId, eventModelId, eventModel.getWorkflowIds());
		insertDeadlineReferences(projectId, eventModelId, eventModel.getDeadlineReferenceEventModelIds());
		insertBlockedEvents(projectId, eventModelId, eventModel.getBlockedEventModelIds());
		insertImpliedEvents(projectId, eventModelId, eventModel.getImpliedEventModelIds());

		return getEventModel(projectId, eventModelId);
	}

	@Override
	public EventModelDTO updateEventModel(final UUID projectId, final UUID eventModelId, final EventModelDTO eventModel) {
		dslContext.update(EVENT_MODEL)
			.set(EVENT_MODEL.CODE, eventModel.getId())
			.set(EVENT_MODEL.EVENT_GROUP_ID, eventModel.getEventGroupId())
			.set(EVENT_MODEL.SCOPE_MODEL_ID, eventModel.getScopeModelId())
			.set(EVENT_MODEL.SHORTNAME, jsonMapperService.toJson(eventModel.getShortname()))
			.set(EVENT_MODEL.LONGNAME, jsonMapperService.toJson(eventModel.getLongname()))
			.set(EVENT_MODEL.DESCRIPTION, jsonMapperService.toJson(eventModel.getDescription()))
			.set(EVENT_MODEL.INCEPTIVE, eventModel.isInceptive())
			.set(EVENT_MODEL.NUMBER, eventModel.getNumber())
			.set(EVENT_MODEL.MANDATORY, eventModel.isMandatory())
			.set(EVENT_MODEL.MAX_OCCURRENCE, eventModel.getMaxOccurrence())
			.set(EVENT_MODEL.PREVENT_ADD, eventModel.isPreventAdd())
			.set(EVENT_MODEL.DEADLINE_VALUE, eventModel.getDeadline())
			.set(EVENT_MODEL.DEADLINE_UNIT, eventModel.getDeadlineUnit() != null ? eventModel.getDeadlineUnit().name() : null)
			.set(EVENT_MODEL.DEADLINE_AGGR_FNCT, eventModel.getDeadlineAggregationFunction() != null ? eventModel.getDeadlineAggregationFunction().name() : null)
			.set(EVENT_MODEL.INTERVAL_VALUE, eventModel.getInterval())
			.set(EVENT_MODEL.INTERVAL_UNIT, eventModel.getIntervalUnit() != null ? eventModel.getIntervalUnit().name() : null)
			.set(EVENT_MODEL.LABEL_PATTERN, eventModel.getLabelPattern())
			.set(EVENT_MODEL.ICON, eventModel.getIcon())
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		deleteRelationships(projectId, eventModelId);

		insertDatasetModels(projectId, eventModelId, eventModel.getDatasetModelIds());
		insertFormModels(projectId, eventModelId, eventModel.getFormModelIds());
		insertWorkflows(projectId, eventModelId, eventModel.getWorkflowIds());
		insertDeadlineReferences(projectId, eventModelId, eventModel.getDeadlineReferenceEventModelIds());
		insertBlockedEvents(projectId, eventModelId, eventModel.getBlockedEventModelIds());
		insertImpliedEvents(projectId, eventModelId, eventModel.getImpliedEventModelIds());

		return getEventModel(projectId, eventModelId);
	}

	@Override
	public void deleteEventModel(final UUID projectId, final UUID eventModelId) {
		deleteRelationships(projectId, eventModelId);

		dslContext.deleteFrom(EVENT_MODEL)
			.where(EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.execute();
	}

	private void deleteRelationships(final UUID projectId, final UUID eventModelId) {
		dslContext.deleteFrom(EVENT_MODEL_BLOCKED_EVENT)
			.where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_BLOCKED_EVENT)
			.where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_IMPLIED_EVENT)
			.where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_IMPLIED_EVENT)
			.where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_DEADLINE_REFERENCE)
			.where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.eq(eventModelId))
			.execute();

		dslContext.deleteFrom(EVENT_MODEL_DEADLINE_REFERENCE)
			.where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID.eq(eventModelId))
			.execute();

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
	}

	private void insertDatasetModels(final UUID projectId, final UUID eventModelId, final List<UUID> datasetModelIds) {
		if(datasetModelIds == null || datasetModelIds.isEmpty()) {
			return;
		}

		for(final var datasetModelId : datasetModelIds) {
			dslContext.insertInto(EVENT_MODEL_DATASET_MODEL)
				.set(EVENT_MODEL_DATASET_MODEL.PROJECT_ID, projectId)
				.set(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID, datasetModelId)
				.execute();
		}
	}

	private void insertFormModels(final UUID projectId, final UUID eventModelId, final List<UUID> formModelIds) {
		if(formModelIds == null || formModelIds.isEmpty()) {
			return;
		}

		for(final var formModelId : formModelIds) {
			dslContext.insertInto(EVENT_MODEL_FORM_MODEL)
				.set(EVENT_MODEL_FORM_MODEL.PROJECT_ID, projectId)
				.set(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID, formModelId)
				.execute();
		}
	}

	private void insertWorkflows(final UUID projectId, final UUID eventModelId, final List<UUID> workflowIds) {
		if(workflowIds == null || workflowIds.isEmpty()) {
			return;
		}

		for(final var workflowId : workflowIds) {
			dslContext.insertInto(EVENT_MODEL_WORKFLOW)
				.set(EVENT_MODEL_WORKFLOW.PROJECT_ID, projectId)
				.set(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_WORKFLOW.WORKFLOW_ID, workflowId)
				.execute();
		}
	}

	private void insertDeadlineReferences(final UUID projectId, final UUID eventModelId, final List<UUID> deadlineReferenceIds) {
		if(deadlineReferenceIds == null || deadlineReferenceIds.isEmpty()) {
			return;
		}

		for(final UUID referenceEventModelId : deadlineReferenceIds) {
			dslContext.insertInto(EVENT_MODEL_DEADLINE_REFERENCE)
				.set(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID, projectId)
				.set(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID, referenceEventModelId)
				.execute();
		}
	}

	private void insertBlockedEvents(final UUID projectId, final UUID eventModelId, final List<UUID> blockedEventModelIds) {
		if(blockedEventModelIds == null || blockedEventModelIds.isEmpty()) {
			return;
		}

		for(final var blockedEventModelId : blockedEventModelIds) {
			dslContext.insertInto(EVENT_MODEL_BLOCKED_EVENT)
				.set(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID, projectId)
				.set(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID, blockedEventModelId)
				.execute();
		}
	}

	private void insertImpliedEvents(final UUID projectId, final UUID eventModelId, final List<UUID> impliedEventModelIds) {
		if(impliedEventModelIds == null || impliedEventModelIds.isEmpty()) {
			return;
		}

		for(final var impliedEventModelId : impliedEventModelIds) {
			dslContext.insertInto(EVENT_MODEL_IMPLIED_EVENT)
				.set(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID, projectId)
				.set(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID, eventModelId)
				.set(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID, impliedEventModelId)
				.execute();
		}
	}

	private EventModelDTO mapToDTO(final EventModelRecord record, final UUID projectId) {
		final var dto = new EventModelDTO();
		dto.setEventModelId(record.getEventModelId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {}));
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

		final var datasetModelIds = loadDatasetModelIds(projectId, record.getEventModelId());
		dto.setDatasetModelIds(datasetModelIds);

		final var formModelIds = loadFormModelIds(projectId, record.getEventModelId());
		dto.setFormModelIds(formModelIds);

		final var workflowIds = loadWorkflowIds(projectId, record.getEventModelId());
		dto.setWorkflowIds(workflowIds);

		final var deadlineReferenceIds = loadDeadlineReferenceIds(projectId, record.getEventModelId());
		dto.setDeadlineReferenceEventModelIds(deadlineReferenceIds);

		final var blockedEventModelIds = loadBlockedEventModelIds(projectId, record.getEventModelId());
		dto.setBlockedEventModelIds(blockedEventModelIds);

		final var impliedEventModelIds = loadImpliedEventModelIds(projectId, record.getEventModelId());
		dto.setImpliedEventModelIds(impliedEventModelIds);

		return dto;
	}

	private List<UUID> loadDatasetModelIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID)
			.from(EVENT_MODEL_DATASET_MODEL)
			.where(EVENT_MODEL_DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DATASET_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_DATASET_MODEL.DATASET_MODEL_ID);
	}

	private List<UUID> loadFormModelIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID)
			.from(EVENT_MODEL_FORM_MODEL)
			.where(EVENT_MODEL_FORM_MODEL.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_FORM_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_FORM_MODEL.FORM_MODEL_ID);
	}

	private List<UUID> loadWorkflowIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_WORKFLOW.WORKFLOW_ID)
			.from(EVENT_MODEL_WORKFLOW)
			.where(EVENT_MODEL_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_WORKFLOW.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_WORKFLOW.WORKFLOW_ID);
	}

	private List<UUID> loadDeadlineReferenceIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID)
			.from(EVENT_MODEL_DEADLINE_REFERENCE)
			.where(EVENT_MODEL_DEADLINE_REFERENCE.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_DEADLINE_REFERENCE.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_DEADLINE_REFERENCE.REFERENCE_EVENT_MODEL_ID);
	}

	private List<UUID> loadBlockedEventModelIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID)
			.from(EVENT_MODEL_BLOCKED_EVENT)
			.where(EVENT_MODEL_BLOCKED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_BLOCKED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_BLOCKED_EVENT.BLOCKED_EVENT_MODEL_ID);
	}

	private List<UUID> loadImpliedEventModelIds(final UUID projectId, final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID)
			.from(EVENT_MODEL_IMPLIED_EVENT)
			.where(EVENT_MODEL_IMPLIED_EVENT.PROJECT_ID.eq(projectId))
			.and(EVENT_MODEL_IMPLIED_EVENT.EVENT_MODEL_ID.eq(eventModelId))
			.fetch(EVENT_MODEL_IMPLIED_EVENT.IMPLIED_EVENT_MODEL_ID);
	}
}
