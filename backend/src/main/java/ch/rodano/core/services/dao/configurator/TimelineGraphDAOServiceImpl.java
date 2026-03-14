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

import com.fasterxml.jackson.core.type.TypeReference;

import ch.rodano.api.config.TimelineGraphDTO;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphRecord;

import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionEvent.TIMELINE_GRAPH_SECTION_EVENT;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionMetaField.TIMELINE_GRAPH_SECTION_META_FIELD;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;

@Repository
public class TimelineGraphDAOServiceImpl implements TimelineGraphDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public TimelineGraphDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timelineGraphs", key = "#projectId.toString()")
	public List<TimelineGraphDTO> getTimelineGraphs(final UUID projectId) {
		final var graphRecords = dslContext
			.selectFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.orderBy(TIMELINE_GRAPH.CODE)
			.fetch();

		if(graphRecords.isEmpty()) {
			return List.of();
		}

		final var graphIds = graphRecords.map(TimelineGraphRecord::getTimelineGraphId);
		final var sectionIdsByGraph = loadSectionIds(projectId, graphIds);

		return graphRecords.map(record -> mapToDTO(record, sectionIdsByGraph));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timelineGraph", key = "#projectId.toString() + ':' + #timelineGraphId.toString()")
	public TimelineGraphDTO getTimelineGraph(final UUID projectId, final UUID timelineGraphId) {
		final var record = dslContext
			.selectFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var sectionIdsByGraph = loadSectionIds(projectId, List.of(timelineGraphId));
		return mapToDTO(record, sectionIdsByGraph);
	}

	@Override
	@Transactional
	@CacheEvict(value = "timelineGraphs", key = "#projectId.toString()")
	public TimelineGraphDTO createTimelineGraph(final UUID projectId, final TimelineGraphDTO dto) {
		final var timelineGraphId = dto.getTimelineGraphId() != null ? dto.getTimelineGraphId() : UUID.randomUUID();

		dslContext.insertInto(TIMELINE_GRAPH)
			.set(TIMELINE_GRAPH.TIMELINE_GRAPH_ID, timelineGraphId)
			.set(TIMELINE_GRAPH.PROJECT_ID, projectId)
			.set(TIMELINE_GRAPH.CODE, dto.getId())
			.set(TIMELINE_GRAPH.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(TIMELINE_GRAPH.STUDY_START_EVENT_MODEL_ID, dto.getStudyStartEventModelId())
			.set(TIMELINE_GRAPH.STUDY_END_EVENT_MODEL_ID, dto.getStudyEndEventModelId())
			.set(TIMELINE_GRAPH.STUDY_PERIOD_IS_DEFAULT, dto.isStudyPeriodIsDefault())
			.set(TIMELINE_GRAPH.WIDTH, dto.getWidth())
			.set(TIMELINE_GRAPH.HEIGHT, dto.getHeight())
			.set(TIMELINE_GRAPH.LEGEND_WIDTH, dto.getLegendWidth())
			.set(TIMELINE_GRAPH.SCROLLER_HEIGHT, dto.getScrollerHeight())
			.set(TIMELINE_GRAPH.SHOW_SCROLLER, dto.isShowScroller())
			.set(TIMELINE_GRAPH.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(TIMELINE_GRAPH.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(TIMELINE_GRAPH.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(TIMELINE_GRAPH.FOOTNOTE, jsonMapperService.toJson(dto.getFootnote()))
			.execute();

		return getTimelineGraph(projectId, timelineGraphId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "timelineGraphs", key = "#projectId.toString()"),
		@CacheEvict(value = "timelineGraph", key = "#projectId.toString() + ':' + #timelineGraphId.toString()")
	})
	public TimelineGraphDTO updateTimelineGraph(final UUID projectId, final UUID timelineGraphId, final TimelineGraphDTO dto) {
		dslContext.update(TIMELINE_GRAPH)
			.set(TIMELINE_GRAPH.CODE, dto.getId())
			.set(TIMELINE_GRAPH.SCOPE_MODEL_ID, dto.getScopeModelId())
			.set(TIMELINE_GRAPH.STUDY_START_EVENT_MODEL_ID, dto.getStudyStartEventModelId())
			.set(TIMELINE_GRAPH.STUDY_END_EVENT_MODEL_ID, dto.getStudyEndEventModelId())
			.set(TIMELINE_GRAPH.STUDY_PERIOD_IS_DEFAULT, dto.isStudyPeriodIsDefault())
			.set(TIMELINE_GRAPH.WIDTH, dto.getWidth())
			.set(TIMELINE_GRAPH.HEIGHT, dto.getHeight())
			.set(TIMELINE_GRAPH.LEGEND_WIDTH, dto.getLegendWidth())
			.set(TIMELINE_GRAPH.SCROLLER_HEIGHT, dto.getScrollerHeight())
			.set(TIMELINE_GRAPH.SHOW_SCROLLER, dto.isShowScroller())
			.set(TIMELINE_GRAPH.SHORTNAME, jsonMapperService.toJson(dto.getShortname()))
			.set(TIMELINE_GRAPH.LONGNAME, jsonMapperService.toJson(dto.getLongname()))
			.set(TIMELINE_GRAPH.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(TIMELINE_GRAPH.FOOTNOTE, jsonMapperService.toJson(dto.getFootnote()))
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		return getTimelineGraph(projectId, timelineGraphId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "timelineGraphs", key = "#projectId.toString()"),
		@CacheEvict(value = "timelineGraph", key = "#projectId.toString() + ':' + #timelineGraphId.toString()")
	})
	public void deleteTimelineGraph(final UUID projectId, final UUID timelineGraphId) {
		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_EVENT)
			.where(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_EVENT.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_META_FIELD)
			.where(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_META_FIELD.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.execute();
	}

	private Map<UUID, List<UUID>> loadSectionIds(final UUID projectId, final List<UUID> graphIds) {
		final var rows = dslContext
			.select(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID, TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID)
			.from(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.in(graphIds))
			.orderBy(TIMELINE_GRAPH_SECTION.CODE)
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private TimelineGraphDTO mapToDTO(final TimelineGraphRecord record, final Map<UUID, List<UUID>> sectionIdsByGraph) {
		final var dto = new TimelineGraphDTO();
		dto.setTimelineGraphId(record.getTimelineGraphId());
		dto.setId(record.getCode());
		dto.setShortname(jsonMapperService.fromJson(record.getShortname(), new TypeReference<>() {
		}));
		dto.setLongname(jsonMapperService.fromJson(record.getLongname(), new TypeReference<>() {
		}));
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<>() {
		}));
		dto.setFootnote(jsonMapperService.fromJson(record.getFootnote(), new TypeReference<>() {
		}));
		dto.setScopeModelId(record.getScopeModelId());
		dto.setStudyStartEventModelId(record.getStudyStartEventModelId());
		dto.setStudyEndEventModelId(record.getStudyEndEventModelId());
		dto.setStudyPeriodIsDefault(record.getStudyPeriodIsDefault());
		dto.setWidth(record.getWidth());
		dto.setHeight(record.getHeight());
		dto.setLegendWidth(record.getLegendWidth());
		dto.setScrollerHeight(record.getScrollerHeight());
		dto.setShowScroller(record.getShowScroller());
		dto.setSections(sectionIdsByGraph.getOrDefault(record.getTimelineGraphId(), List.of()));
		return dto;
	}
}
