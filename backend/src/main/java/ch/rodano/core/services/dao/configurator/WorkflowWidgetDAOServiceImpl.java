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

import ch.rodano.api.config.WorkflowWidgetColumnConfigDTO;
import ch.rodano.api.config.WorkflowWidgetConfigDTO;
import ch.rodano.core.model.jooq.tables.records.WorkflowWidgetRecord;

import static ch.rodano.core.model.jooq.tables.WorkflowState.WORKFLOW_STATE;
import static ch.rodano.core.model.jooq.tables.WorkflowWidget.WORKFLOW_WIDGET;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetColumn.WORKFLOW_WIDGET_COLUMN;
import static ch.rodano.core.model.jooq.tables.WorkflowWidgetStateSelector.WORKFLOW_WIDGET_STATE_SELECTOR;

@Repository
public class WorkflowWidgetDAOServiceImpl implements WorkflowWidgetDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public WorkflowWidgetDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowWidgets", key = "#projectId.toString()")
	public List<WorkflowWidgetConfigDTO> getWorkflowWidgets(final UUID projectId) {
		final var records = dslContext
			.selectFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.orderBy(WORKFLOW_WIDGET.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var widgetIds = records.map(WorkflowWidgetRecord::getWorkflowWidgetId);
		final var columnMap = loadColumnIds(projectId, widgetIds);
		final var workflowStateMap = loadWorkflowStateIds(projectId, widgetIds);

		return records.map(record -> mapToDTO(record, columnMap, workflowStateMap));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workflowWidget", key = "#projectId.toString() + ':' + #workflowWidgetId.toString()")
	public WorkflowWidgetConfigDTO getWorkflowWidget(final UUID projectId, final UUID workflowWidgetId) {
		final var record = dslContext
			.selectFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var widgetIds = List.of(workflowWidgetId);
		final var columnMap = loadColumnIds(projectId, widgetIds);
		final var workflowStateMap = loadWorkflowStateIds(projectId, widgetIds);

		return mapToDTO(record, columnMap, workflowStateMap);
	}

	@Override
	@Transactional
	@CacheEvict(value = "workflowWidgets", key = "#projectId.toString()")
	public WorkflowWidgetConfigDTO createWorkflowWidget(final UUID projectId, final WorkflowWidgetConfigDTO dto) {
		final var workflowWidgetId = dto.getWorkflowWidgetId() != null ? dto.getWorkflowWidgetId() : UUID.randomUUID();

		dslContext.insertInto(WORKFLOW_WIDGET)
			.set(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID, workflowWidgetId)
			.set(WORKFLOW_WIDGET.PROJECT_ID, projectId)
			.set(WORKFLOW_WIDGET.CODE, dto.getId())
			.set(WORKFLOW_WIDGET.WORKFLOW_ENTITY, dto.getWorkflowEntity())
			.set(WORKFLOW_WIDGET.FILTER_EXPECTED_EVENTS, dto.isFilterExpectedEvents())
			.set(WORKFLOW_WIDGET.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW_WIDGET.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW_WIDGET.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.execute();

		replaceChildren(projectId, workflowWidgetId, dto);

		return getWorkflowWidget(projectId, workflowWidgetId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowWidgets", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowWidget", key = "#projectId.toString() + ':' + #workflowWidgetId.toString()")
	})
	public WorkflowWidgetConfigDTO updateWorkflowWidget(final UUID projectId, final UUID workflowWidgetId, final WorkflowWidgetConfigDTO dto) {
		dslContext.update(WORKFLOW_WIDGET)
			.set(WORKFLOW_WIDGET.CODE, dto.getId())
			.set(WORKFLOW_WIDGET.WORKFLOW_ENTITY, dto.getWorkflowEntity())
			.set(WORKFLOW_WIDGET.FILTER_EXPECTED_EVENTS, dto.isFilterExpectedEvents())
			.set(WORKFLOW_WIDGET.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(WORKFLOW_WIDGET.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(WORKFLOW_WIDGET.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();

		replaceChildren(projectId, workflowWidgetId, dto);

		return getWorkflowWidget(projectId, workflowWidgetId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "workflowWidgets", key = "#projectId.toString()"),
		@CacheEvict(value = "workflowWidget", key = "#projectId.toString() + ':' + #workflowWidgetId.toString()")
	})
	public void deleteWorkflowWidget(final UUID projectId, final UUID workflowWidgetId) {
		dslContext.deleteFrom(WORKFLOW_WIDGET_STATE_SELECTOR)
			.where(WORKFLOW_WIDGET_STATE_SELECTOR.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_WIDGET_COLUMN)
			.where(WORKFLOW_WIDGET_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_WIDGET)
			.where(WORKFLOW_WIDGET.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();
	}

	private void replaceChildren(final UUID projectId, final UUID workflowWidgetId, final WorkflowWidgetConfigDTO dto) {
		dslContext.deleteFrom(WORKFLOW_WIDGET_STATE_SELECTOR)
			.where(WORKFLOW_WIDGET_STATE_SELECTOR.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();

		dslContext.deleteFrom(WORKFLOW_WIDGET_COLUMN)
			.where(WORKFLOW_WIDGET_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID.eq(workflowWidgetId))
			.execute();

		if(dto.getWorkflowStateIds() != null) {
			for(final var workflowStateId : dto.getWorkflowStateIds()) {
				final var workflowId = dslContext.select(WORKFLOW_STATE.WORKFLOW_ID)
					.from(WORKFLOW_STATE)
					.where(WORKFLOW_STATE.WORKFLOW_STATE_ID.eq(workflowStateId))
					.fetchOneInto(UUID.class);
				dslContext.insertInto(WORKFLOW_WIDGET_STATE_SELECTOR)
					.set(WORKFLOW_WIDGET_STATE_SELECTOR.PROJECT_ID, projectId)
					.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID, workflowWidgetId)
					.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_ID, workflowId)
					.set(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_STATE_ID, workflowStateId)
					.execute();
			}
		}

		if(dto.getColumns() != null) {
			for(int i = 0; i < dto.getColumns().size(); i++) {
				final var column = dto.getColumns().get(i);
				final var columnId = column.getWorkflowWidgetColumnId() != null
					? column.getWorkflowWidgetColumnId()
					: UUID.randomUUID();
				dslContext.insertInto(WORKFLOW_WIDGET_COLUMN)
					.set(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_COLUMN_ID, columnId)
					.set(WORKFLOW_WIDGET_COLUMN.PROJECT_ID, projectId)
					.set(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID, workflowWidgetId)
					.set(WORKFLOW_WIDGET_COLUMN.CODE, column.getId())
					.set(WORKFLOW_WIDGET_COLUMN.TYPE, column.getType())
					.set(WORKFLOW_WIDGET_COLUMN.WIDTH, column.getWidth())
					.set(WORKFLOW_WIDGET_COLUMN.SHORTNAME, jsonMapperService.toJson(column.getShortname()))
					.set(WORKFLOW_WIDGET_COLUMN.LONGNAME, jsonMapperService.toJson(column.getLongname()))
					.set(WORKFLOW_WIDGET_COLUMN.DESCRIPTION, jsonMapperService.toJson(column.getDescription()))
					.set(WORKFLOW_WIDGET_COLUMN.SORT_ORDER, i)
					.execute();
			}
		}
	}

	private Map<UUID, List<WorkflowWidgetColumnConfigDTO>> loadColumnIds(final UUID projectId, final List<UUID> widgetIds) {
		if(widgetIds == null || widgetIds.isEmpty()) {
			return Map.of();
		}

		final var records = dslContext
			.selectFrom(WORKFLOW_WIDGET_COLUMN)
			.where(WORKFLOW_WIDGET_COLUMN.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_COLUMN.WORKFLOW_WIDGET_ID.in(widgetIds))
			.orderBy(WORKFLOW_WIDGET_COLUMN.SORT_ORDER.asc())
			.fetch();

		final Map<UUID, List<WorkflowWidgetColumnConfigDTO>> result = new HashMap<>();
		for(final var record : records) {
			final var dto = new WorkflowWidgetColumnConfigDTO();
			dto.setWorkflowWidgetColumnId(record.getWorkflowWidgetColumnId());
			dto.setId(record.getCode());
			dto.setType(record.getType());
			dto.setWidth(record.getWidth());
			dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setSortOrder(record.getSortOrder());
			result.computeIfAbsent(record.getWorkflowWidgetId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private Map<UUID, List<UUID>> loadWorkflowStateIds(final UUID projectId, final List<UUID> widgetIds) {
		if(widgetIds == null || widgetIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID, WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_STATE_ID)
			.from(WORKFLOW_WIDGET_STATE_SELECTOR)
			.where(WORKFLOW_WIDGET_STATE_SELECTOR.PROJECT_ID.eq(projectId))
			.and(WORKFLOW_WIDGET_STATE_SELECTOR.WORKFLOW_WIDGET_ID.in(widgetIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private WorkflowWidgetConfigDTO mapToDTO(final WorkflowWidgetRecord record,
											 final Map<UUID, List<WorkflowWidgetColumnConfigDTO>> columnMap,
											 final Map<UUID, List<UUID>> workflowStateMap) {
		final var dto = new WorkflowWidgetConfigDTO();

		dto.setWorkflowWidgetId(record.getWorkflowWidgetId());
		dto.setId(record.getCode());
		dto.setWorkflowEntity(record.getWorkflowEntity());
		dto.setFilterExpectedEvents(record.getFilterExpectedEvents());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setColumns(columnMap.getOrDefault(record.getWorkflowWidgetId(), List.of()));
		dto.setWorkflowStateIds(workflowStateMap.getOrDefault(record.getWorkflowWidgetId(), List.of()));
		return dto;
	}
}
