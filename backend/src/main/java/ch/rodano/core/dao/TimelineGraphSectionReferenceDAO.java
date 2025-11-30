package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionReference;
import ch.rodano.configuration.model.timelinegraph.TimelineGraphSectionReferenceEntry;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphSectionReferenceEntryRecord;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphSectionReferenceRecord;

import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;

@Repository
public class TimelineGraphSectionReferenceDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public TimelineGraphSectionReferenceDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<TimelineGraphSectionReference> findBySection(final UUID sectionId) {
		return dslContext.selectFrom(TIMELINE_GRAPH_SECTION_REFERENCE)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID.eq(sectionId))
			.fetch(this::mapToModel);
	}

	private TimelineGraphSectionReference mapToModel(final TimelineGraphSectionReferenceRecord record) {
		if(record == null) {
			return null;
		}

		final TimelineGraphSectionReference model = new TimelineGraphSectionReference();

		model.setColor(record.getColor());
		model.setDashed(record.getDashed() != null ? record.getDashed() : false);

		model.setLabel(mappingHelper.parseJsonToMap(record.getLabel()));
		model.setTooltip(mappingHelper.parseJsonToMap(record.getTooltip()));

		if(record.getReferenceSectionId() != null) {
			model.setReferenceSectionId(getSectionCode(record.getReferenceSectionId()));
		}

		model.setEntries(loadEntries(record.getGraphReferenceId()));

		return model;
	}

	private List<TimelineGraphSectionReferenceEntry> loadEntries(final UUID referenceId) {
		return dslContext.selectFrom(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_REFERENCE_ID.eq(referenceId))
			.orderBy(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.SORT_ORDER)
			.fetch(this::mapEntryToModel);
	}

	private TimelineGraphSectionReferenceEntry mapEntryToModel(final TimelineGraphSectionReferenceEntryRecord record) {
		if(record == null) {
			return null;
		}

		final TimelineGraphSectionReferenceEntry model = new TimelineGraphSectionReferenceEntry();

		model.setTimepoint(record.getTimepoint());
		model.setLabel(record.getLabel());
		if(record.getValue() != null) {
			model.setValue(record.getValue().floatValue());
		}

		return model;
	}

	private String getSectionCode(final UUID sectionId) {
		return dslContext.select(TIMELINE_GRAPH_SECTION.CODE)
			.from(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID.eq(sectionId))
			.fetchOne(TIMELINE_GRAPH_SECTION.CODE);
	}
}
