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

import ch.rodano.api.config.ChartModelDTO;
import ch.rodano.api.config.ChartRangeDTO;
import ch.rodano.api.config.ChartStateFilterDTO;
import ch.rodano.configuration.model.chart.ChartType;
import ch.rodano.core.model.jooq.tables.records.ChartRecord;

import static ch.rodano.core.model.jooq.tables.Chart.CHART;
import static ch.rodano.core.model.jooq.tables.ChartColor.CHART_COLOR;
import static ch.rodano.core.model.jooq.tables.ChartRange.CHART_RANGE;
import static ch.rodano.core.model.jooq.tables.ChartStateFilter.CHART_STATE_FILTER;

@Repository
public class ChartDAOServiceImpl implements ChartDAOService {

	private static final String VIEW_SUMMARY = "summary";
	private static final String VIEW_FULL = "full";

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public ChartDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChartModelDTO> getCharts(final UUID projectId, final String view) {
		final var normalized = view == null ? VIEW_SUMMARY : view.trim().toLowerCase();
		return switch(normalized) {
			case VIEW_FULL -> getChartsFull(projectId);
			case VIEW_SUMMARY -> getChartsSummary(projectId);
			default -> getChartsSummary(projectId);
		};
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "charts", key = "#projectId.toString() + ':summary'")
	public List<ChartModelDTO> getChartsSummary(final UUID projectId) {
		final var records = dslContext.selectFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.orderBy(CHART.CODE)
			.fetch();

		return records.map(record -> mapToDTO(record, List.of(), List.of(), List.of()));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "charts", key = "#projectId.toString() + ':full'")
	public List<ChartModelDTO> getChartsFull(final UUID projectId) {
		final var records = dslContext.selectFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.orderBy(CHART.CODE)
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		final var chartIds = records.map(ChartRecord::getChartId);
		final var colorsMap = loadColors(projectId, chartIds);
		final var rangeMap = loadRanges(projectId, chartIds);
		final var stateFilterMap = loadStateFilters(projectId, chartIds);

		return records.map(record -> mapToDTO(
			record,
			colorsMap.getOrDefault(record.getChartId(), List.of()),
			rangeMap.getOrDefault(record.getChartId(), List.of()),
			stateFilterMap.getOrDefault(record.getChartId(), List.of())
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "chart", key = "#projectId.toString() + ':' + #chartId.toString()")
	public ChartModelDTO getChart(final UUID projectId, final UUID chartId) {
		final var record = dslContext.selectFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.and(CHART.CHART_ID.eq(chartId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var chartIds = List.of(chartId);
		final var colorsMap = loadColors(projectId, chartIds);
		final var rangeMap = loadRanges(projectId, chartIds);
		final var stateFilterMap = loadStateFilters(projectId, chartIds);

		return mapToDTO(
			record,
			colorsMap.getOrDefault(record.getChartId(), List.of()),
			rangeMap.getOrDefault(record.getChartId(), List.of()),
			stateFilterMap.getOrDefault(record.getChartId(), List.of())
		);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':full'")
	})
	public ChartModelDTO createChart(final UUID projectId, final ChartModelDTO dto) {
		final var chartId = dto.getChartId() != null ? dto.getChartId() : UUID.randomUUID();

		dslContext.insertInto(CHART)
			.set(CHART.CHART_ID, chartId)
			.set(CHART.PROJECT_ID, projectId)
			.set(CHART.CODE, dto.getId())
			.set(CHART.TYPE, ch.rodano.core.model.jooq.enums.ChartType.valueOf(dto.getType().name()))
			.set(CHART.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(CHART.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(CHART.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(CHART.TITLE, jsonMapperService.toJson(dto.getTitle()))
			.set(CHART.LEGEND_X, jsonMapperService.toJson(dto.getLegendX()))
			.set(CHART.LEGEND_Y, jsonMapperService.toJson(dto.getLegendY()))
			.set(CHART.OVERRIDE_USER_RIGHTS, dto.isOverrideUserRights())
			.set(CHART.WITH_STATISTICS, dto.isWithStatistics())
			.set(CHART.DISPLAY_EXPECTED, dto.isDisplayExpected())
			.set(CHART.WORKFLOW_ID, dto.getWorkflowId())
			.set(CHART.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(CHART.LEAF_SCOPE_MODEL_ID, dto.getLeafScopeModelId())
			.set(CHART.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(CHART.FIELD_MODEL_ID, dto.getFieldModelId())
			.execute();

		replaceRelations(projectId, chartId, dto);

		return getChart(projectId, chartId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "chart", key = "#projectId.toString() + ':' + #chartId.toString()")
	})
	public ChartModelDTO updateChart(final UUID projectId, final UUID chartId, final ChartModelDTO dto) {
		dslContext.update(CHART)
			.set(CHART.CODE, dto.getId())
			.set(CHART.TYPE, ch.rodano.core.model.jooq.enums.ChartType.valueOf(dto.getType().name()))
			.set(CHART.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(CHART.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(CHART.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(CHART.TITLE, jsonMapperService.toJson(dto.getTitle()))
			.set(CHART.LEGEND_X, jsonMapperService.toJson(dto.getLegendX()))
			.set(CHART.LEGEND_Y, jsonMapperService.toJson(dto.getLegendY()))
			.set(CHART.OVERRIDE_USER_RIGHTS, dto.isOverrideUserRights())
			.set(CHART.WITH_STATISTICS, dto.isWithStatistics())
			.set(CHART.DISPLAY_EXPECTED, dto.isDisplayExpected())
			.set(CHART.WORKFLOW_ID, dto.getWorkflowId())
			.set(CHART.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(CHART.LEAF_SCOPE_MODEL_ID, dto.getLeafScopeModelId())
			.set(CHART.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(CHART.FIELD_MODEL_ID, dto.getFieldModelId())
			.where(CHART.PROJECT_ID.eq(projectId))
			.and(CHART.CHART_ID.eq(chartId))
			.execute();

		replaceRelations(projectId, chartId, dto);

		return getChart(projectId, chartId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "charts", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "chart", key = "#projectId.toString() + ':' + #chartId.toString()")
	})
	public void deleteChart(final UUID projectId, final UUID chartId) {
		dslContext.deleteFrom(CHART_COLOR)
			.where(CHART_COLOR.PROJECT_ID.eq(projectId))
			.and(CHART_COLOR.CHART_ID.eq(chartId))
			.execute();

		dslContext.deleteFrom(CHART_RANGE)
			.where(CHART_RANGE.PROJECT_ID.eq(projectId))
			.and(CHART_RANGE.CHART_ID.eq(chartId))
			.execute();

		dslContext.deleteFrom(CHART_STATE_FILTER)
			.where(CHART_STATE_FILTER.PROJECT_ID.eq(projectId))
			.and(CHART_STATE_FILTER.CHART_ID.eq(chartId))
			.execute();

		dslContext.deleteFrom(CHART)
			.where(CHART.PROJECT_ID.eq(projectId))
			.and(CHART.CHART_ID.eq(chartId))
			.execute();
	}

	private void replaceRelations(final UUID projectId, final UUID chartId, final ChartModelDTO dto) {
		dslContext.deleteFrom(CHART_COLOR)
			.where(CHART_COLOR.PROJECT_ID.eq(projectId))
			.and(CHART_COLOR.CHART_ID.eq(chartId))
			.execute();

		dslContext.deleteFrom(CHART_RANGE)
			.where(CHART_RANGE.PROJECT_ID.eq(projectId))
			.and(CHART_RANGE.CHART_ID.eq(chartId))
			.execute();

		dslContext.deleteFrom(CHART_STATE_FILTER)
			.where(CHART_STATE_FILTER.PROJECT_ID.eq(projectId))
			.and(CHART_STATE_FILTER.CHART_ID.eq(chartId))
			.execute();

		batchInsert(projectId, chartId, dto);
	}

	private void batchInsert(final UUID projectId, final UUID chartId, final ChartModelDTO dto) {
		if(dto.getColors() != null) {
			for(int i = 0; i < dto.getColors().size(); i++) {
				final var color = dto.getColors().get(i);
				dslContext.insertInto(CHART_COLOR)
					.set(CHART_COLOR.PROJECT_ID, projectId)
					.set(CHART_COLOR.CHART_ID, chartId)
					.set(CHART_COLOR.COLOR, color)
					.set(CHART_COLOR.SORT_ORDER, i)
					.execute();
			}
		}

		if(dto.getRanges() != null) {
			for(int i = 0; i < dto.getRanges().size(); i++) {
				final var range = dto.getRanges().get(i);
				final var rangeId = range.getChartRangeId() != null ? range.getChartRangeId() : UUID.randomUUID();

				dslContext.insertInto(CHART_RANGE)
					.set(CHART_RANGE.CHART_RANGE_ID, rangeId)
					.set(CHART_RANGE.PROJECT_ID, projectId)
					.set(CHART_RANGE.CHART_ID, chartId)
					.set(CHART_RANGE.CODE, range.getId())
					.set(CHART_RANGE.VALUE, range.getValue())
					.set(CHART_RANGE.LABEL, jsonMapperService.toJson(range.getLabel()))
					.set(CHART_RANGE.MIN, range.getMin())
					.set(CHART_RANGE.MAX, range.getMax())
					.set(CHART_RANGE.IS_OTHER, range.isOther())
					.set(CHART_RANGE.SORT_ORDER, i)
					.execute();
			}
		}

		if(dto.getStateFilters() != null) {
			for(final var stateFilter : dto.getStateFilters()) {
				dslContext.insertInto(CHART_STATE_FILTER)
					.set(CHART_STATE_FILTER.PROJECT_ID, projectId)
					.set(CHART_STATE_FILTER.CHART_ID, chartId)
					.set(CHART_STATE_FILTER.WORKFLOW_STATE_ID, stateFilter.getWorkflowStateId())
					.set(CHART_STATE_FILTER.KIND, ch.rodano.core.model.jooq.enums.ChartStateFilterKind.valueOf(stateFilter.getKind().name()))
					.execute();
			}
		}
	}

	private ChartModelDTO mapToDTO(final ChartRecord record,
								   final List<String> colors,
								   final List<ChartRangeDTO> ranges,
								   final List<ChartStateFilterDTO> stateFilters) {
		final var dto = new ChartModelDTO();

		dto.setChartId(record.getChartId());
		dto.setId(record.getCode());
		dto.setType(ChartType.valueOf(record.getType().name()));

		dto.setOverrideUserRights(record.getOverrideUserRights());
		dto.setWithStatistics(record.getWithStatistics());
		dto.setDisplayExpected(record.getDisplayExpected());

		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setTitle(jsonMapperService.fromJson(record.getTitle(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLegendX(jsonMapperService.fromJson(record.getLegendX(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setLegendY(jsonMapperService.fromJson(record.getLegendY(), new TypeReference<TreeMap<String, String>>() {
		}));

		dto.setWorkflowId(record.getWorkflowId());
		dto.setScopeModelId(record.getScopeModelId());
		dto.setLeafScopeModelId(record.getLeafScopeModelId());
		dto.setDatasetModelId(record.getDatasetModelId());
		dto.setFieldModelId(record.getFieldModelId());

		dto.setColors(colors);
		dto.setRanges(ranges);
		dto.setStateFilters(stateFilters);

		return dto;
	}

	private Map<UUID, List<String>> loadColors(final UUID projectId, final List<UUID> chartIds) {
		if(chartIds == null || chartIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(CHART_COLOR.CHART_ID, CHART_COLOR.COLOR)
			.from(CHART_COLOR)
			.where(CHART_COLOR.PROJECT_ID.eq(projectId))
			.and(CHART_COLOR.CHART_ID.in(chartIds))
			.orderBy(CHART_COLOR.SORT_ORDER.asc())
			.fetch();

		final Map<UUID, List<String>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private Map<UUID, List<ChartRangeDTO>> loadRanges(final UUID projectId, final List<UUID> chartIds) {
		if(chartIds == null || chartIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.selectFrom(CHART_RANGE)
			.where(CHART_RANGE.PROJECT_ID.eq(projectId))
			.and(CHART_RANGE.CHART_ID.in(chartIds))
			.orderBy(CHART_RANGE.SORT_ORDER.asc(), CHART_RANGE.CODE.asc())
			.fetch();

		final Map<UUID, List<ChartRangeDTO>> result = new HashMap<>();
		for(final var r : rows) {
			final var dto = new ChartRangeDTO();
			dto.setChartRangeId(r.getChartRangeId());
			dto.setId(r.getCode());
			dto.setValue(r.getValue());
			dto.setLabel(jsonMapperService.fromJson(r.getLabel(), new TypeReference<TreeMap<String, String>>() {
			}));
			dto.setMin(r.getMin() != null ? r.getMin() : null);
			dto.setMax(r.getMax() != null ? r.getMax() : null);
			dto.setOther(r.getIsOther());
			dto.setSortOrder(r.getSortOrder());

			result.computeIfAbsent(r.getChartId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private Map<UUID, List<ChartStateFilterDTO>> loadStateFilters(final UUID projectId, final List<UUID> chartIds) {
		if(chartIds == null || chartIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.selectFrom(CHART_STATE_FILTER)
			.where(CHART_STATE_FILTER.PROJECT_ID.eq(projectId))
			.and(CHART_STATE_FILTER.CHART_ID.in(chartIds))
			.fetch();

		final Map<UUID, List<ChartStateFilterDTO>> result = new HashMap<>();
		for(final var r : rows) {
			final var dto = new ChartStateFilterDTO();
			dto.setWorkflowStateId(r.getWorkflowStateId());
			dto.setKind(r.getKind() != null ? ChartStateFilterDTO.Kind.valueOf(r.getKind().name()) : null);

			result.computeIfAbsent(r.getChartId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}
}
