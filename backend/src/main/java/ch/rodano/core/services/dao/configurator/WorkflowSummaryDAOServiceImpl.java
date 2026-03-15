package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.WorkflowSummaryColumnDTO;
import ch.rodano.api.config.WorkflowSummaryDTO;
import ch.rodano.core.model.jooq.tables.records.WorkflowSummaryColumnRecord;
import ch.rodano.core.model.jooq.tables.records.WorkflowSummaryRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowSummary.WORKFLOW_SUMMARY;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumn.WORKFLOW_SUMMARY_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryColumnState.WORKFLOW_SUMMARY_COLUMN_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryFilterEventModel.WORKFLOW_SUMMARY_FILTER_EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.WorkflowSummaryWorkflow.WORKFLOW_SUMMARY_WORKFLOW;

@Repository
public class WorkflowSummaryDAOServiceImpl implements WorkflowSummaryDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public WorkflowSummaryDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowSummaries", key = "#projectId.toString()")
	public List<WorkflowSummaryDTO> getWorkflowSummaries(final UUID projectId) {
		final var records = dslContext
			.selectFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW_SUMMARY.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var summaryIds = records.map(WorkflowSummaryRecord::getWorkflowSummaryId);
		final var columnMap = loadColumnIds(projectId, summaryIds);
		final var workflowMap = loadWorkflowIds(projectId, summaryIds);
		final var eventModelMap = loadEventModelIds(projectId, summaryIds);

		return records.map(record -> mapToDTO(record, columnMap, workflowMap, eventModelMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowSummary", key = "#projectId.toString() + ':' + #workflowSummaryId.toString()")
	public WorkflowSummaryDTO getWorkflowSummary(final UUID projectId, final UUID workflowSummaryId) {
		final var record = dslContext
			.selectFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var summaryIds = List.of(workflowSummaryId);
		final var columnMap = loadColumnIds(projectId, summaryIds);
		final var workflowMap = loadWorkflowIds(projectId, summaryIds);
		final var eventModelMap = loadEventModelIds(projectId, summaryIds);

		return mapToDTO(record, columnMap, workflowMap, eventModelMap);
	}

	@Override
	@Transactional
	@CacheEvict(value = "workflowSummaries", key = "#projectId.toString()")
	public WorkflowSummaryDTO createWorkflowSummary(final UUID projectId, final WorkflowSummaryDTO dto) {
		final var workflowSummaryId = dto.getWorkflowSummaryId() != null ? dto.getWorkflowSummaryId() : UUID.randomUUID();

		dslContext.insertInto(WORKFLOW_SUMMARY)
			.set(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID, workflowSummaryId)
			.set(WORKFLOW_SUMMARY.PROJECT_ID, projectId)
			.set(WORKFLOW_SUMMARY.CODE, dto.getId())
			.set(WORKFLOW_SUMMARY.WORKFLOW_ENTITY, dto.getWorkflowEntity())
			.set(WORKFLOW_SUMMARY.LEAF_SCOPE_MODEL_ID, dto.getLeafScopeModelId())
			.set(WORKFLOW_SUMMARY.FILTER_EXPECTED_EVENTS, dto.isFilterExpectedEvents())
			.set(WORKFLOW_SUMMARY.DISPLAY_LEGEND, dto.isDisplayLegend())
			.set(WORKFLOW_SUMMARY.DISPLAY_COLUMN_EXPORT, dto.isDisplayColumnExport())
			.set(WORKFLOW_SUMMARY.TITLE, jsonMapperService.toJson(dto.getTitle()))
			.execute();

		replaceChildren(projectId, workflowSummaryId, dto);

		return getWorkflowSummary(projectId, workflowSummaryId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowSummaries", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowSummary", key = "#projectId.toString() + ':' + #workflowSummaryId.toString()")
	})
	public WorkflowSummaryDTO updateWorkflowSummary(final UUID projectId, final UUID workflowSummaryId, final WorkflowSummaryDTO dto) {
		dslContext.update(WORKFLOW_SUMMARY)
			.set(WORKFLOW_SUMMARY.CODE, dto.getId())
			.set(WORKFLOW_SUMMARY.WORKFLOW_ENTITY, dto.getWorkflowEntity())
			.set(WORKFLOW_SUMMARY.LEAF_SCOPE_MODEL_ID, dto.getLeafScopeModelId())
			.set(WORKFLOW_SUMMARY.FILTER_EXPECTED_EVENTS, dto.isFilterExpectedEvents())
			.set(WORKFLOW_SUMMARY.DISPLAY_LEGEND, dto.isDisplayLegend())
			.set(WORKFLOW_SUMMARY.DISPLAY_COLUMN_EXPORT, dto.isDisplayColumnExport())
			.set(WORKFLOW_SUMMARY.TITLE, jsonMapperService.toJson(dto.getTitle()))
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		replaceChildren(projectId, workflowSummaryId, dto);

		return getWorkflowSummary(projectId, workflowSummaryId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowSummaries", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowSummary", key = "#projectId.toString() + ':' + #workflowSummaryId.toString()")
	})
	public void deleteWorkflowSummary(final UUID projectId, final UUID workflowSummaryId) {
		final var columnIds = dslContext
			.select(WORKFLOW_SUMMARY_COLUMN.SUMMARY_COLUMN_ID)
			.from(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.fetchInto(UUID.class);

		if(!columnIds.isEmpty()) {
			dslContext.deleteFrom(WORKFLOW_SUMMARY_COLUMN_STATE)
				.where(WORKFLOW_SUMMARY_COLUMN_STATE.PROJECT_ID.eq(projectId))
				.and(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID.in(columnIds))
				.execute();
		}

		dslContext.deleteFrom(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
			.where(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_SUMMARY_WORKFLOW)
			.where(WORKFLOW_SUMMARY_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_SUMMARY)
			.where(WORKFLOW_SUMMARY.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();
	}

	private void replaceChildren(final UUID projectId, final UUID workflowSummaryId, final WorkflowSummaryDTO dto) {
		final var existingColumnIds = dslContext
			.select(WORKFLOW_SUMMARY_COLUMN.SUMMARY_COLUMN_ID)
			.from(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.fetchInto(UUID.class);

		if(!existingColumnIds.isEmpty()) {
			dslContext.deleteFrom(WORKFLOW_SUMMARY_COLUMN_STATE)
				.where(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID.in(existingColumnIds))
				.execute();
		}

		dslContext.deleteFrom(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
			.where(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_SUMMARY_WORKFLOW)
			.where(WORKFLOW_SUMMARY_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID.eq(workflowSummaryId))
			.execute();

		if(dto.getWorkflowIds() != null) {
			for(final var workflowId : dto.getWorkflowIds()) {
				dslContext.insertInto(WORKFLOW_SUMMARY_WORKFLOW)
					.set(WORKFLOW_SUMMARY_WORKFLOW.PROJECT_ID, projectId)
					.set(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID, workflowSummaryId)
					.set(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_ID, workflowId)
					.execute();
			}
		}

		if(dto.getEventModelIds() != null) {
			for(final var eventModelId : dto.getEventModelIds()) {
				dslContext.insertInto(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
					.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.PROJECT_ID, projectId)
					.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID, workflowSummaryId)
					.set(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.EVENT_MODEL_ID, eventModelId)
					.execute();
			}
		}

		if(dto.getColumns() != null) {
			for(int i = 0; i < dto.getColumns().size(); i++) {
				final var column = dto.getColumns().get(i);
				final var columnId = column.getSummaryColumnId() != null
					? column.getSummaryColumnId()
					: UUID.randomUUID();

				dslContext.insertInto(WORKFLOW_SUMMARY_COLUMN)
					.set(WORKFLOW_SUMMARY_COLUMN.SUMMARY_COLUMN_ID, columnId)
					.set(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID, projectId)
					.set(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID, workflowSummaryId)
					.set(WORKFLOW_SUMMARY_COLUMN.SORT_ORDER, i)
					.set(WORKFLOW_SUMMARY_COLUMN.TOTAL, column.isTotal())
					.set(WORKFLOW_SUMMARY_COLUMN.PERCENT, column.isPercent())
					.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_COLOR, column.getNonNullColor())
					.set(WORKFLOW_SUMMARY_COLUMN.NON_NULL_BG_COLOR, column.getNonNullBgColor())
					.set(WORKFLOW_SUMMARY_COLUMN.LABEL, jsonMapperService.toJson(column.getLabel()))
					.set(WORKFLOW_SUMMARY_COLUMN.DESCRIPTION, jsonMapperService.toJson(column.getDescription()))
					.execute();

				if(column.getWorkflowStateIds() != null) {
					for(final var workflowStateId : column.getWorkflowStateIds()) {
						dslContext.insertInto(WORKFLOW_SUMMARY_COLUMN_STATE)
							.set(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID, columnId)
							.set(WORKFLOW_SUMMARY_COLUMN_STATE.PROJECT_ID, projectId)
							.set(WORKFLOW_SUMMARY_COLUMN_STATE.WORKFLOW_STATE_ID, workflowStateId)
							.execute();
					}
				}
			}
		}
	}

	private Map<UUID, List<WorkflowSummaryColumnDTO>> loadColumnIds(final UUID projectId, final List<UUID> summaryIds) {
		if(summaryIds == null || summaryIds.isEmpty()) {
			return Map.of();
		}

		final var records = dslContext
			.selectFrom(WORKFLOW_SUMMARY_COLUMN)
			.where(WORKFLOW_SUMMARY_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN.WORKFLOW_SUMMARY_ID.in(summaryIds))
			.orderBy(WORKFLOW_SUMMARY_COLUMN.SORT_ORDER.asc())
			.fetch();

		if(records.isEmpty()) {
			return Map.of();
		}

		final var columnIds = records.map(WorkflowSummaryColumnRecord::getSummaryColumnId);
		final var statesByColumn = loadColumnStateIds(projectId, columnIds);

		final Map<UUID, List<WorkflowSummaryColumnDTO>> result = new HashMap<>();
		for(final var record : records) {
			final var dto = new WorkflowSummaryColumnDTO();
			dto.setSummaryColumnId(record.getSummaryColumnId());
			dto.setWorkflowSummaryId(record.getWorkflowSummaryId());
			dto.setSortOrder(record.getSortOrder());
			dto.setTotal(record.getTotal());
			dto.setPercent(record.getPercent());
			dto.setNonNullColor(record.getNonNullColor());
			dto.setNonNullBgColor(record.getNonNullBgColor());
			dto.setLabel(jsonMapperService.fromJson(record.getLabel(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setWorkflowStateIds(statesByColumn.getOrDefault(record.getSummaryColumnId(), List.of()));
			result.computeIfAbsent(record.getWorkflowSummaryId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private Map<UUID, List<UUID>> loadColumnStateIds(final UUID projectId, final List<UUID> columnIds) {
		if(columnIds == null || columnIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID, WORKFLOW_SUMMARY_COLUMN_STATE.WORKFLOW_STATE_ID)
			.from(WORKFLOW_SUMMARY_COLUMN_STATE)
			.where(WORKFLOW_SUMMARY_COLUMN_STATE.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_COLUMN_STATE.SUMMARY_COLUMN_ID.in(columnIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private Map<UUID, List<UUID>> loadWorkflowIds(final UUID projectId, final List<UUID> summaryIds) {
		if(summaryIds == null || summaryIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID, WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_ID)
			.from(WORKFLOW_SUMMARY_WORKFLOW)
			.where(WORKFLOW_SUMMARY_WORKFLOW.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_WORKFLOW.WORKFLOW_SUMMARY_ID.in(summaryIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private Map<UUID, List<UUID>> loadEventModelIds(final UUID projectId, final List<UUID> summaryIds) {
		if(summaryIds == null || summaryIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID, WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.EVENT_MODEL_ID)
			.from(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL)
			.where(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_SUMMARY_FILTER_EVENT_MODEL.WORKFLOW_SUMMARY_ID.in(summaryIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private WorkflowSummaryDTO mapToDTO(
		final WorkflowSummaryRecord record,
		final Map<UUID, List<WorkflowSummaryColumnDTO>> columnMap,
		final Map<UUID, List<UUID>> workflowMap,
		final Map<UUID, List<UUID>> eventModelMap
	) {
		final var dto = new WorkflowSummaryDTO();
		dto.setWorkflowSummaryId(record.getWorkflowSummaryId());
		dto.setId(record.getCode());
		dto.setWorkflowEntity(record.getWorkflowEntity());
		dto.setLeafScopeModelId(record.getLeafScopeModelId());
		dto.setFilterExpectedEvents(record.getFilterExpectedEvents());
		dto.setDisplayLegend(record.getDisplayLegend());
		dto.setDisplayColumnExport(record.getDisplayColumnExport());
		dto.setTitle(jsonMapperService.fromJson(record.getTitle(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setColumns(columnMap.getOrDefault(record.getWorkflowSummaryId(), List.of()));
		dto.setWorkflowIds(workflowMap.getOrDefault(record.getWorkflowSummaryId(), List.of()));
		dto.setEventModelIds(eventModelMap.getOrDefault(record.getWorkflowSummaryId(), List.of()));
		return dto;
	}
}
