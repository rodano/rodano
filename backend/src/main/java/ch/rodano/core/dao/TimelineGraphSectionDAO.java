package ch.rodano.core.dao;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.timelinegraph.TimelineGraphSection;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionAxisPosition;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionMark;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionPosition;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionScale;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionType;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphSectionRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionEvent.TIMELINE_GRAPH_SECTION_EVENT;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionMetaField.TIMELINE_GRAPH_SECTION_META_FIELD;

@Repository
public class TimelineGraphSectionDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final TimelineGraphSectionReferenceDAO referenceDAO;

	public TimelineGraphSectionDAO(final DSLContext dslContext, final MappingHelper mappingHelper, final TimelineGraphSectionReferenceDAO referenceDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.referenceDAO = referenceDAO;
	}

	public List<TimelineGraphSection> findByTimelineGraph(final UUID timelineGraphId) {
		return dslContext.selectFrom(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.fetch(this::mapToModel);
	}

	private TimelineGraphSection mapToModel(final TimelineGraphSectionRecord record) {
		if(record == null) {
			return null;
		}

		final TimelineGraphSection model = new TimelineGraphSection();

		model.setId(record.getCode());
		model.setGraphSectionId(record.getGraphSectionId());

		if(record.getType() != null) {
			model.setType(mappingHelper.parseEnum(TimelineGraphSectionType.class, record.getType().name(), "type"));
		}

		model.setUseScopePaths(record.getUseScopePaths() != null ? record.getUseScopePaths() : false);
		model.setHideExpectedEvent(record.getHideExpectedEvent() != null ? record.getHideExpectedEvent() : false);
		model.setHideDoneEvent(record.getHideDoneEvent() != null ? record.getHideDoneEvent() : false);
		model.setDashed(record.getDashed() != null ? record.getDashed() : false);
		model.setHiddenLegend(record.getHiddenLegend() != null ? record.getHiddenLegend() : false);
		model.setHidden(record.getHidden() != null ? record.getHidden() : false);
		model.setColor(record.getColor());
		model.setStrokeColor(record.getStrokeColor());

		if(record.getMark() != null) {
			model.setMark(mappingHelper.parseEnum(TimelineGraphSectionMark.class, record.getMark().name(), "mark"));
		}

		if(record.getOpacity() != null) {
			model.setOpacity(record.getOpacity().floatValue());
		}

		model.setLabel(mappingHelper.parseJsonToMap(record.getLabel()));
		model.setTooltip(mappingHelper.parseJsonToMap(record.getTooltip()));

		mapPosition(model, record);
		mapScale(model, record);

		if(record.getDatasetModelId() != null) {
			model.setDatasetModelId(getDatasetModelCode(record.getDatasetModelId()));

			if(record.getDateFieldId() != null) {
				model.setDateFieldModelId(getFieldModelCode(record.getDateFieldId()));
			}
			if(record.getEndDateFieldId() != null) {
				model.setEndDateFieldModelId(getFieldModelCode(record.getEndDateFieldId()));
			}
			if(record.getLabelFieldId() != null) {
				model.setLabelFieldModelId(getFieldModelCode(record.getLabelFieldId()));
			}
			if(record.getValueFieldId() != null) {
				model.setValueFieldModelId(getFieldModelCode(record.getValueFieldId()));
			}
		}

		model.setEventModelIds(new TreeSet<>(loadEventModelIds(record.getGraphSectionId())));
		model.setMetaFieldModelIds(loadMetaFieldModelIds(record.getGraphSectionId()));

		model.setReferences(referenceDAO.findBySection(record.getGraphSectionId()));

		return model;
	}

	private void mapPosition(final TimelineGraphSection model, final TimelineGraphSectionRecord record) {
		if(record.getPositionStart() != null || record.getPositionStop() != null) {
			final TimelineGraphSectionPosition position = new TimelineGraphSectionPosition();
			position.setStart(record.getPositionStart());
			position.setStop(record.getPositionStop());
			model.setPosition(position);
		}
	}

	private void mapScale(final TimelineGraphSection model, final TimelineGraphSectionRecord record) {
		if(record.getScaleMin() != null || record.getScaleMax() != null) {
			final TimelineGraphSectionScale scale = new TimelineGraphSectionScale();
			scale.setMin(record.getScaleMin() != null ? record.getScaleMin().intValue() : null);
			scale.setMax(record.getScaleMax() != null ? record.getScaleMax().intValue() : null);
			scale.setDecimal(record.getScaleDecimal() != null ? record.getScaleDecimal() : 0);
			scale.setMarkInterval(record.getScaleMarkInterval() != null ? record.getScaleMarkInterval().doubleValue() : 0.0);
			scale.setLabelInterval(record.getScaleLabelInterval() != null ? record.getScaleLabelInterval().doubleValue() : 0.0);

			if(record.getScalePosition() != null) {
				scale.setPosition(mappingHelper.parseEnum(TimelineGraphSectionAxisPosition.class, record.getScalePosition().name(), "scalePosition"));
			}
			model.setScale(scale);
		}
	}

	private List<String> loadEventModelIds(final UUID sectionId) {
		return dslContext.select(EVENT_MODEL.CODE)
			.from(TIMELINE_GRAPH_SECTION_EVENT)
			.join(EVENT_MODEL).on(EVENT_MODEL.EVENT_MODEL_ID.eq(TIMELINE_GRAPH_SECTION_EVENT.EVENT_MODEL_ID))
			.where(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID.eq(sectionId))
			.orderBy(TIMELINE_GRAPH_SECTION_EVENT.SORT_ORDER)
			.fetch(EVENT_MODEL.CODE);
	}

	private List<String> loadMetaFieldModelIds(final UUID sectionId) {
		return dslContext.select(FIELD_MODEL.CODE)
			.from(TIMELINE_GRAPH_SECTION_META_FIELD)
			.join(FIELD_MODEL).on(FIELD_MODEL.FIELD_MODEL_ID.eq(TIMELINE_GRAPH_SECTION_META_FIELD.FIELD_MODEL_ID))
			.where(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID.eq(sectionId))
			.orderBy(TIMELINE_GRAPH_SECTION_META_FIELD.SORT_ORDER)
			.fetch(FIELD_MODEL.CODE);
	}

	private String getDatasetModelCode(final UUID datasetModelId) {
		return dslContext.select(DATASET_MODEL.CODE)
			.from(DATASET_MODEL)
			.where(DATASET_MODEL.DATASET_MODEL_ID.eq(datasetModelId))
			.fetchOne(DATASET_MODEL.CODE);
	}

	private String getFieldModelCode(final UUID fieldModelId) {
		return dslContext.select(FIELD_MODEL.CODE)
			.from(FIELD_MODEL)
			.where(FIELD_MODEL.FIELD_MODEL_ID.eq(fieldModelId))
			.fetchOne(FIELD_MODEL.CODE);
	}
}
