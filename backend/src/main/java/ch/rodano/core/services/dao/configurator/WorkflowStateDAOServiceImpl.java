package ch.rodano.core.services.dao.configurator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.workflow.WorkflowActionDTO;
import ch.rodano.api.workflow.WorkflowStateDTO;
import ch.rodano.core.model.jooq.tables.records.WorkflowStateRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowStatePossibleAction.WORKFLOW_STATE_POSSIBLE_ACTION;

@Service
@Transactional
public class WorkflowStateDAOServiceImpl implements WorkflowStateDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;
	private final WorkflowActionDAOService workflowActionDAOService;

	public WorkflowStateDAOServiceImpl(final DSLContext dslContext,
									   final JsonMapperService jsonMapperService,
									   final WorkflowActionDAOService workflowActionDAOService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
		this.workflowActionDAOService = workflowActionDAOService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkflowStateDTO> getWorkflowStates(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getWorkflowStatesFull(projectId);
			case VIEW_SUMMARY -> getWorkflowStatesSummary(projectId);
			default -> getWorkflowStatesSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowStates", key = "#projectId.toString() + ':summary'")
	public List<WorkflowStateDTO> getWorkflowStatesSummary(final UUID projectId) {
		final var workflowStateRecords = dslContext.selectFrom(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW_STATE.CODE)
			.fetch();

		if(workflowStateRecords.isEmpty()) {
			return List.of();
		}

		return workflowStateRecords.map(record -> mapToDTO(record, Map.of()));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowStates", key = "#projectId.toString() + ':full'")
	public List<WorkflowStateDTO> getWorkflowStatesFull(final UUID projectId) {
		final var workflowStateRecords = dslContext.selectFrom(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW_STATE.CODE)
			.fetch();

		if(workflowStateRecords.isEmpty()) {
			return List.of();
		}

		final var workflowStateIds = workflowStateRecords.map(WorkflowStateRecord::getWorkflowStateId);
		final var actionMap = loadPossibleActions(projectId, workflowStateIds);

		return workflowStateRecords.map(record -> mapToDTO(record, actionMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowState", key = "#projectId.toString() + ':' + #workflowStateId.toString()")
	public WorkflowStateDTO getWorkflowState(final UUID projectId, final UUID workflowStateId) {
		final var record = dslContext
			.selectFrom(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(workflowStateId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var workflowStateIds = List.of(workflowStateId);
		final var actionMap = loadPossibleActions(projectId, workflowStateIds);

		return mapToDTO(record, actionMap);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':full'")
	})
	public WorkflowStateDTO createWorkflowState(final UUID projectId, final WorkflowStateDTO dto) {
		final var workflowStateId = dto.getWorkflowStateId() != null ? dto.getWorkflowStateId() : UUID.randomUUID();

		dslContext.insertInto(WORKFLOW_STATE)
			.set(WORKFLOW_STATE.PROJECT_ID, projectId)
			.set(WORKFLOW_STATE.WORKFLOW_STATE_ID, workflowStateId)
			.set(WORKFLOW_STATE.WORKFLOW_ID, dto.getWorkflowId())
			.set(WORKFLOW_STATE.CODE, dto.getId())
			.set(WORKFLOW_STATE.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW_STATE.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW_STATE.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(WORKFLOW_STATE.IMPORTANT, dto.isImportant())
			.set(WORKFLOW_STATE.COLOR, dto.getColor())
			.set(WORKFLOW_STATE.ICON, dto.getIcon())
			.set(WORKFLOW_STATE.AGGREGATE_STATE_ID, dto.getAggregateStateId())
			.set(WORKFLOW_STATE.AGGREGATE_STATE_MATCHER, dto.getAggregateStateMatcher())
			.execute();

		savePossibleActions(projectId, workflowStateId, dto.getPossibleActions());
		return getWorkflowState(projectId, workflowStateId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "workflowState", key = "#projectId.toString() + ':' + #workflowStateId.toString()")
	})
	public WorkflowStateDTO updateWorkflowState(final UUID projectId, final UUID workflowStateId, final WorkflowStateDTO dto) {
		dslContext.update(WORKFLOW_STATE)
			.set(WORKFLOW_STATE.WORKFLOW_ID, dto.getWorkflowId())
			.set(WORKFLOW_STATE.CODE, dto.getId())
			.set(WORKFLOW_STATE.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW_STATE.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW_STATE.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(WORKFLOW_STATE.IMPORTANT, dto.isImportant())
			.set(WORKFLOW_STATE.COLOR, dto.getColor())
			.set(WORKFLOW_STATE.ICON, dto.getIcon())
			.set(WORKFLOW_STATE.AGGREGATE_STATE_ID, dto.getAggregateStateId())
			.set(WORKFLOW_STATE.AGGREGATE_STATE_MATCHER, dto.getAggregateStateMatcher())
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(workflowStateId))
			.execute();

		updatePossibleActions(projectId, workflowStateId, dto.getPossibleActions());
		return getWorkflowState(projectId, workflowStateId);
	}

	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "workflowStates", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "workflowState", key = "#projectId.toString() + ':' + #workflowStateId.toString()")
	})
	public void deleteWorkflowState(final UUID projectId, final UUID workflowStateId) {
		dslContext.deleteFrom(WORKFLOW_STATE_POSSIBLE_ACTION)
			.where(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID.eq(workflowStateId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_STATE)
			.where(WORKFLOW_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(workflowStateId))
			.execute();
	}

	private Map<UUID, List<WorkflowActionDTO>> loadPossibleActions(final UUID projectId, final List<UUID> workflowStateIds) {
		final var stateActionMap = dslContext
			.select(
				WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID,
				WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_ACTION_ID
			)
			.from(WORKFLOW_STATE_POSSIBLE_ACTION)
			.where(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID.in(workflowStateIds))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(
				Record2::value1,
				Collectors.mapping(Record2::value2, Collectors.toList())
			));

		if(stateActionMap.isEmpty()) {
			return Map.of();
		}

		final var allActionIds = stateActionMap.values().stream()
			.flatMap(List::stream)
			.distinct()
			.toList();

		final var allActions = workflowActionDAOService.getWorkflowActionsByIds(projectId, allActionIds);
		final var actionByIdMap = allActions.stream()
			.collect(Collectors.toMap(WorkflowActionDTO::getWorkflowActionId, a -> a));

		return stateActionMap.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream()
					.map(actionByIdMap::get)
					.filter(Objects::nonNull)
					.toList()
			));
	}

	private void savePossibleActions(final UUID projectId, final UUID workflowStateId, final List<WorkflowActionDTO> possibleActions) {
		if(possibleActions == null || possibleActions.isEmpty()) {
			return;
		}

		for(final var action : possibleActions) {
			dslContext.insertInto(WORKFLOW_STATE_POSSIBLE_ACTION)
				.set(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID, projectId)
				.set(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID, workflowStateId)
				.set(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_ACTION_ID, action.getWorkflowActionId())
				.execute();
		}
	}

	private void updatePossibleActions(final UUID projectId, final UUID workflowStateId, final List<WorkflowActionDTO> possibleActions) {
		dslContext.deleteFrom(WORKFLOW_STATE_POSSIBLE_ACTION)
			.where(WORKFLOW_STATE_POSSIBLE_ACTION.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_STATE_POSSIBLE_ACTION.WORKFLOW_STATE_ID.eq(workflowStateId))
			.execute();

		savePossibleActions(projectId, workflowStateId, possibleActions);
	}

	private WorkflowStateDTO mapToDTO(final WorkflowStateRecord record, final Map<UUID, List<WorkflowActionDTO>> actionMap) {
		final var dto = new WorkflowStateDTO();
		dto.setWorkflowId(record.getWorkflowId());
		dto.setId(record.getCode());
		dto.setWorkflowId(record.getWorkflowId());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setImportant(record.getImportant());
		dto.setColor(dto.getColor());
		dto.setIcon(record.getIcon());
		dto.setAggregateStateId(record.getAggregateStateId());
		dto.setAggregateStateMatcher(record.getAggregateStateMatcher());

		final var workflowStateId = record.getWorkflowStateId();
		dto.setPossibleActions(actionMap.getOrDefault(workflowStateId, List.of()));

		return dto;
	}
}
