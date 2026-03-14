package ch.rodano.batch.writer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rodano.batch.helper.ProjectScoped;
import ch.rodano.batch.pojo.TimelineGraph;
import ch.rodano.batch.pojo.TimelineGraphSection;
import ch.rodano.batch.pojo.TimelineGraphSectionReference;
import ch.rodano.batch.pojo.TimelineGraphSectionReferenceEntry;
import ch.rodano.core.model.jooq.enums.TimelineGraphSectionScalePosition;

import static ch.rodano.batch.helper.JsonWriter.toJson;
import static ch.rodano.batch.helper.ModelResolvers.resolveDatasetModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveEventModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveFieldModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveScopeModelId;
import static ch.rodano.batch.helper.ModelResolvers.resolveSectionId;
import static ch.rodano.configuration.jackson.DeterministicUuid.deterministic;
import static ch.rodano.core.model.jooq.tables.TimelineGraph.TIMELINE_GRAPH;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionEvent.TIMELINE_GRAPH_SECTION_EVENT;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionMetaField.TIMELINE_GRAPH_SECTION_META_FIELD;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;

public class TimelineGraphWriter extends BaseWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimelineGraphWriter.class);

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		initIfNeeded();

		dsl.transaction(cfg -> {
			final DSLContext tx = DSL.using(cfg);

			for(Object raw : list) {
				@SuppressWarnings("unchecked") final ProjectScoped<TimelineGraph> wrapped = (ProjectScoped<TimelineGraph>) raw;
				final UUID projectId = wrapped.getProjectId();
				final TimelineGraph graph = wrapped.getPayload();

				final String graphCode = graph.getId();
				final UUID graphId = deterministic(projectId, "TIMELINE_GRAPH", graphCode);

				final UUID scopeModelId = resolveScopeModelId(tx, projectId, graph.getScopeModelId());
				final UUID studyStartEventModelId = resolveEventModelId(tx, projectId, graph.getStudyStartEventModelId());
				final UUID studyEndEventModelId = resolveEventModelId(tx, projectId, graph.getStudyEndEventModelId());

				tx.insertInto(TIMELINE_GRAPH)
					.set(TIMELINE_GRAPH.PROJECT_ID, projectId)
					.set(TIMELINE_GRAPH.TIMELINE_GRAPH_ID, graphId)
					.set(TIMELINE_GRAPH.CODE, graphCode)
					.set(TIMELINE_GRAPH.SCOPE_MODEL_ID, scopeModelId)
					.set(TIMELINE_GRAPH.STUDY_START_EVENT_MODEL_ID, studyStartEventModelId)
					.set(TIMELINE_GRAPH.STUDY_END_EVENT_MODEL_ID, studyEndEventModelId)
					.set(TIMELINE_GRAPH.STUDY_PERIOD_IS_DEFAULT, graph.getStudyPeriodIsDefault())
					.set(TIMELINE_GRAPH.WIDTH, graph.getWidth())
					.set(TIMELINE_GRAPH.HEIGHT, graph.getHeight())
					.set(TIMELINE_GRAPH.LEGEND_WIDTH, graph.getLegendWidth())
					.set(TIMELINE_GRAPH.SCROLLER_HEIGHT, graph.getScrollerHeight())
					.set(TIMELINE_GRAPH.SHOW_SCROLLER, graph.getShowScroller())
					.set(TIMELINE_GRAPH.SHORTNAME, toJson(graph.getShortname()))
					.set(TIMELINE_GRAPH.LONGNAME, toJson(graph.getLongname()))
					.set(TIMELINE_GRAPH.DESCRIPTION, toJson(graph.getDescription()))
					.set(TIMELINE_GRAPH.FOOTNOTE, toJson(graph.getFootnote()))
					.onDuplicateKeyUpdate()
					.set(TIMELINE_GRAPH.SCOPE_MODEL_ID, scopeModelId)
					.set(TIMELINE_GRAPH.STUDY_START_EVENT_MODEL_ID, studyStartEventModelId)
					.set(TIMELINE_GRAPH.STUDY_END_EVENT_MODEL_ID, studyEndEventModelId)
					.set(TIMELINE_GRAPH.STUDY_PERIOD_IS_DEFAULT, graph.getStudyPeriodIsDefault())
					.set(TIMELINE_GRAPH.WIDTH, graph.getWidth())
					.set(TIMELINE_GRAPH.HEIGHT, graph.getHeight())
					.set(TIMELINE_GRAPH.LEGEND_WIDTH, graph.getLegendWidth())
					.set(TIMELINE_GRAPH.SCROLLER_HEIGHT, graph.getScrollerHeight())
					.set(TIMELINE_GRAPH.SHOW_SCROLLER, graph.getShowScroller())
					.set(TIMELINE_GRAPH.SHORTNAME, toJson(graph.getShortname()))
					.set(TIMELINE_GRAPH.LONGNAME, toJson(graph.getLongname()))
					.set(TIMELINE_GRAPH.DESCRIPTION, toJson(graph.getDescription()))
					.set(TIMELINE_GRAPH.FOOTNOTE, toJson(graph.getFootnote()))
					.execute();

				if(graph.getSections() != null && !graph.getSections().isEmpty()) {
					for(TimelineGraphSection section : graph.getSections()) {
						final String sectionCode = section.getId();
						final UUID sectionId = deterministic(projectId, "TIMELINE_GRAPH_SECTION", graphCode + "|" + sectionCode);

						final UUID datasetModelId = resolveDatasetModelId(tx, projectId, section.getDatasetModelId());
						final UUID dateFieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, section.getDateFieldModelId());
						final UUID endDateFieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, section.getEndDateFieldModelId());
						final UUID labelFieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, section.getLabelFieldModelId());
						final UUID valueFieldModelId = resolveFieldModelId(tx, projectId, datasetModelId, section.getValueFieldModelId());

						final var position = section.getPosition();
						final Integer positionStart = position != null ? position.getStart() : null;
						final Integer positionStop = position != null ? position.getStop() : null;

						final var scale = section.getScale();
						final BigDecimal scaleMin = scale != null ? scale.getMin() : null;
						final BigDecimal scaleMax = scale != null ? scale.getMax() : null;
						final Integer scaleDecimal = scale != null ? scale.getDecimal() : null;
						final BigDecimal scaleMarkInterval = scale != null ? scale.getMarkInterval() : null;
						final BigDecimal scaleLabelInterval = scale != null ? scale.getLabelInterval() : null;
						final TimelineGraphSectionScalePosition scalePosition = scale != null ? scale.getPosition() : null;

						tx.insertInto(TIMELINE_GRAPH_SECTION)
							.set(TIMELINE_GRAPH_SECTION.PROJECT_ID, projectId)
							.set(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID, graphId)
							.set(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID, sectionId)
							.set(TIMELINE_GRAPH_SECTION.CODE, sectionCode)
							.set(TIMELINE_GRAPH_SECTION.TYPE, section.getType())
							.set(TIMELINE_GRAPH_SECTION.DATASET_MODEL_ID, datasetModelId)
							.set(TIMELINE_GRAPH_SECTION.DATE_FIELD_ID, dateFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.END_DATE_FIELD_ID, endDateFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.LABEL_FIELD_ID, labelFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.VALUE_FIELD_ID, valueFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.HIDE_EXPECTED_EVENT, section.getHideExpectedEvent())
							.set(TIMELINE_GRAPH_SECTION.HIDE_DONE_EVENT, section.getHideDoneEvent())
							.set(TIMELINE_GRAPH_SECTION.USE_SCOPE_PATHS, section.getUseScopePaths())
							.set(TIMELINE_GRAPH_SECTION.UNIT, section.getUnit())
							.set(TIMELINE_GRAPH_SECTION.COLOR, section.getColor())
							.set(TIMELINE_GRAPH_SECTION.STROKE_COLOR, section.getStrokeColor())
							.set(TIMELINE_GRAPH_SECTION.OPACITY, section.getOpacity())
							.set(TIMELINE_GRAPH_SECTION.DASHED, section.getDashed())
							.set(TIMELINE_GRAPH_SECTION.MARK, section.getMark())
							.set(TIMELINE_GRAPH_SECTION.POSITION_START, positionStart)
							.set(TIMELINE_GRAPH_SECTION.POSITION_STOP, positionStop)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MIN, scaleMin)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MAX, scaleMax)
							.set(TIMELINE_GRAPH_SECTION.SCALE_DECIMAL, scaleDecimal)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MARK_INTERVAL, scaleMarkInterval)
							.set(TIMELINE_GRAPH_SECTION.SCALE_LABEL_INTERVAL, scaleLabelInterval)
							.set(TIMELINE_GRAPH_SECTION.SCALE_POSITION, scalePosition)
							.set(TIMELINE_GRAPH_SECTION.HIDDEN_LEGEND, section.getHiddenLegend())
							.set(TIMELINE_GRAPH_SECTION.HIDDEN, section.getHidden())
							.set(TIMELINE_GRAPH_SECTION.LABEL, toJson(section.getLabel()))
							.set(TIMELINE_GRAPH_SECTION.TOOLTIP, toJson(section.getTooltip()))
							.onDuplicateKeyUpdate()
							.set(TIMELINE_GRAPH_SECTION.TYPE, section.getType())
							.set(TIMELINE_GRAPH_SECTION.DATASET_MODEL_ID, datasetModelId)
							.set(TIMELINE_GRAPH_SECTION.DATE_FIELD_ID, dateFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.END_DATE_FIELD_ID, endDateFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.LABEL_FIELD_ID, labelFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.VALUE_FIELD_ID, valueFieldModelId)
							.set(TIMELINE_GRAPH_SECTION.HIDE_EXPECTED_EVENT, section.getHideExpectedEvent())
							.set(TIMELINE_GRAPH_SECTION.HIDE_DONE_EVENT, section.getHideDoneEvent())
							.set(TIMELINE_GRAPH_SECTION.USE_SCOPE_PATHS, section.getUseScopePaths())
							.set(TIMELINE_GRAPH_SECTION.UNIT, section.getUnit())
							.set(TIMELINE_GRAPH_SECTION.COLOR, section.getColor())
							.set(TIMELINE_GRAPH_SECTION.STROKE_COLOR, section.getStrokeColor())
							.set(TIMELINE_GRAPH_SECTION.OPACITY, section.getOpacity())
							.set(TIMELINE_GRAPH_SECTION.DASHED, section.getDashed())
							.set(TIMELINE_GRAPH_SECTION.MARK, section.getMark())
							.set(TIMELINE_GRAPH_SECTION.POSITION_START, positionStart)
							.set(TIMELINE_GRAPH_SECTION.POSITION_STOP, positionStop)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MIN, scaleMin)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MAX, scaleMax)
							.set(TIMELINE_GRAPH_SECTION.SCALE_DECIMAL, scaleDecimal)
							.set(TIMELINE_GRAPH_SECTION.SCALE_MARK_INTERVAL, scaleMarkInterval)
							.set(TIMELINE_GRAPH_SECTION.SCALE_LABEL_INTERVAL, scaleLabelInterval)
							.set(TIMELINE_GRAPH_SECTION.SCALE_POSITION, scalePosition)
							.set(TIMELINE_GRAPH_SECTION.HIDDEN_LEGEND, section.getHiddenLegend())
							.set(TIMELINE_GRAPH_SECTION.HIDDEN, section.getHidden())
							.set(TIMELINE_GRAPH_SECTION.LABEL, toJson(section.getLabel()))
							.set(TIMELINE_GRAPH_SECTION.TOOLTIP, toJson(section.getTooltip()))
							.execute();

						if(section.getEventModelIds() != null && !section.getEventModelIds().isEmpty()) {
							for(String eventCode : section.getEventModelIds()) {
								final UUID eventId = resolveEventModelId(tx, projectId, eventCode);
								if(eventId == null) {
									LOGGER.error("TimelineGraph '{}', section '{}': event_model '{}' not found — skipping.",
										graphCode, sectionCode, eventCode);
									continue;
								}
								tx.insertInto(TIMELINE_GRAPH_SECTION_EVENT)
									.set(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID, projectId)
									.set(TIMELINE_GRAPH_SECTION_EVENT.TIMELINE_GRAPH_ID, graphId)
									.set(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID, sectionId)
									.set(TIMELINE_GRAPH_SECTION_EVENT.EVENT_MODEL_ID, eventId)
									.onDuplicateKeyIgnore()
									.execute();
							}
						}

						if(section.getMetaFieldModelIds() != null && !section.getMetaFieldModelIds().isEmpty()) {
							for(String fieldCode : section.getMetaFieldModelIds()) {
								final UUID fieldId = resolveFieldModelId(tx, projectId, datasetModelId, fieldCode);
								tx.insertInto(TIMELINE_GRAPH_SECTION_META_FIELD)
									.set(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID, projectId)
									.set(TIMELINE_GRAPH_SECTION_META_FIELD.TIMELINE_GRAPH_ID, graphId)
									.set(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID, sectionId)
									.set(TIMELINE_GRAPH_SECTION_META_FIELD.FIELD_MODEL_ID, fieldId)
									.onDuplicateKeyIgnore()
									.execute();
							}
						}
					}
				}

				if(graph.getSections() != null && !graph.getSections().isEmpty()) {
					for(TimelineGraphSection section : graph.getSections()) {
						final String sectionCode = section.getId();
						final UUID sectionId = deterministic(projectId, "TIMELINE_GRAPH_SECTION", graphCode + "|" + sectionCode);

						if(section.getReferences() != null && !section.getReferences().isEmpty()) {
							int refOrder = 0;
							for(TimelineGraphSectionReference reference : section.getReferences()) {
								final UUID referenceId = deterministic(projectId, "TIMELINE_GRAPH_SECTION_REFERENCE", graphCode + "|" + sectionCode + "|" + refOrder);
								final UUID refSectionId = resolveSectionId(tx, projectId, graphId, reference.getReferenceSectionId());

								tx.insertInto(TIMELINE_GRAPH_SECTION_REFERENCE)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID, projectId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.TIMELINE_GRAPH_ID, graphId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID, sectionId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_REFERENCE_ID, referenceId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.COLOR, reference.getColor())
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.DASHED, reference.getDashed())
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.REFERENCE_SECTION_ID, refSectionId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.LABEL, toJson(reference.getLabel()))
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.TOOLTIP, toJson(reference.getTooltip()))
									.onDuplicateKeyUpdate()
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.COLOR, reference.getColor())
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.DASHED, reference.getDashed())
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.REFERENCE_SECTION_ID, refSectionId)
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.LABEL, toJson(reference.getLabel()))
									.set(TIMELINE_GRAPH_SECTION_REFERENCE.TOOLTIP, toJson(reference.getTooltip()))
									.execute();

								if(reference.getEntries() != null && !reference.getEntries().isEmpty()) {
									int entryOrder = 0;
									for(TimelineGraphSectionReferenceEntry entry : reference.getEntries()) {
										tx.insertInto(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID, projectId)
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMELINE_GRAPH_ID, graphId)
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_SECTION_ID, sectionId)
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_REFERENCE_ID, referenceId)
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMEPOINT, entry.getTimepoint())
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.VALUE, entry.getValue())
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.LABEL, entry.getLabel())
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.SORT_ORDER, entryOrder++)
											.onDuplicateKeyUpdate()
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMEPOINT, entry.getTimepoint())
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.VALUE, entry.getValue())
											.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.LABEL, entry.getLabel())
											.execute();
									}
								}
								refOrder++;
							}
						}
					}
				}
			}
		});
	}
}
