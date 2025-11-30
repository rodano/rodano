package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphRecord;

import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;

@Repository
public class TimelineGraphDAO implements BaseProjectDAO<TimelineGraph> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final TimelineGraphSectionDAO timelineGraphSectionDAO;


	public TimelineGraphDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final TimelineGraphSectionDAO timelineGraphSectionDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.timelineGraphSectionDAO = timelineGraphSectionDAO;
	}

	@Override
	public List<TimelineGraph> findByProject(final UUID projectId) {
		return dslContext.selectFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.fetch(this::mapToModel);
	}

	@Override
	public TimelineGraph findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public TimelineGraph findById(final UUID id) {
		return dslContext.selectFrom(TIMELINE_GRAPH)
			.where(TIMELINE_GRAPH.TIMELINE_GRAPH_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public TimelineGraph save(final TimelineGraph entity) {
		return null;
	}

	@Override
	public void delete(final UUID id) {

	}

	private TimelineGraph mapToModel(final TimelineGraphRecord record) {
		if(record == null) {
			return null;
		}

		final TimelineGraph model = new TimelineGraph();

		model.setId(record.getCode());
		model.setTimelineGraphId(record.getTimelineGraphId());

		model.setStudyPeriodIsDefault(record.getStudyPeriodIsDefault() != null ? record.getStudyPeriodIsDefault() : false);
		model.setHeight(record.getHeight() != null ? record.getHeight() : 0);
		model.setLegendWidth(record.getLegendWidth() != null ? record.getLegendWidth() : 0);
		model.setScrollerHeight(record.getScrollerHeight() != null ? record.getScrollerHeight() : 0);
		model.setShowScroller(record.getShowScroller() != null ? record.getShowScroller() : false);

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setFootNote(mappingHelper.parseJsonToMap(record.getFootnote()));

		if(record.getScopeModelId() != null) {
			model.setScopeModelId(getScopeModelCode(record.getScopeModelId()));
		}

		if(record.getStudyStartEventModelId() != null) {
			model.setStudyStartEventModelId(getEventModelCode(record.getStudyStartEventModelId()));
		}

		model.setSections(timelineGraphSectionDAO.findByTimelineGraph(record.getTimelineGraphId()));

		return model;
	}

	private String getScopeModelCode(final UUID scopeModelId) {
		return dslContext.select(SCOPE_MODEL.CODE)
			.from(SCOPE_MODEL)
			.where(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne(SCOPE_MODEL.CODE);
	}

	private String getEventModelCode(final UUID eventModelId) {
		return dslContext.select(EVENT_MODEL.CODE)
			.from(EVENT_MODEL)
			.where(EVENT_MODEL.EVENT_MODEL_ID.eq(eventModelId))
			.fetchOne(EVENT_MODEL.CODE);
	}
}
