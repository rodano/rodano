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

import ch.rodano.api.config.TimelineGraphSectionDTO;
import ch.rodano.api.config.TimelineGraphSectionReferenceDTO;
import ch.rodano.api.config.TimelineGraphSectionReferenceEntryDTO;
import ch.rodano.core.model.jooq.enums.TimelineGraphSectionMark;
import ch.rodano.core.model.jooq.enums.TimelineGraphSectionScalePosition;
import ch.rodano.core.model.jooq.enums.TimelineGraphSectionType;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphSectionRecord;
import ch.rodano.core.model.jooq.tables.records.TimelineGraphSectionReferenceRecord;

import static ch.rodano.core.model.jooq.tables.TimelineGraphSection.TIMELINE_GRAPH_SECTION;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionEvent.TIMELINE_GRAPH_SECTION_EVENT;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionMetaField.TIMELINE_GRAPH_SECTION_META_FIELD;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReference.TIMELINE_GRAPH_SECTION_REFERENCE;
import static ch.rodano.core.model.jooq.tables.TimelineGraphSectionReferenceEntry.TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY;

@Repository
public class TimelineGraphSectionDAOServiceImpl implements TimelineGraphSectionDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public TimelineGraphSectionDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timelineGraphSections", key = "#projectId.toString() + ':' + #timelineGraphId.toString()")
	public List<TimelineGraphSectionDTO> getSections(final UUID projectId, final UUID timelineGraphId) {
		final var sectionRecords = dslContext
			.selectFrom(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.orderBy(TIMELINE_GRAPH_SECTION.CODE)
			.fetch();

		if(sectionRecords.isEmpty()) {
			return List.of();
		}

		final var sectionIds = sectionRecords.map(TimelineGraphSectionRecord::getGraphSectionId);
		final var eventsBySectionId = loadEventModelIds(projectId, sectionIds);
		final var metaFieldsBySectionId = loadMetaFieldIds(projectId, sectionIds);
		final var referencesBySectionId = loadReferences(projectId, sectionIds);

		return sectionRecords.map(record -> mapToDTO(
			record,
			eventsBySectionId.getOrDefault(record.getGraphSectionId(), List.of()),
			metaFieldsBySectionId.getOrDefault(record.getGraphSectionId(), List.of()),
			referencesBySectionId.getOrDefault(record.getGraphSectionId(), List.of())
		));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timelineGraphSection", key = "#projectId.toString() + ':' + #timelineGraphId.toString() + ':' + #sectionId.toString()")
	public TimelineGraphSectionDTO getSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId) {
		final var record = dslContext
			.selectFrom(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID.eq(sectionId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		final var sectionIds = List.of(sectionId);
		final var eventsBySectionId = loadEventModelIds(projectId, sectionIds);
		final var metaFieldsBySectionId = loadMetaFieldIds(projectId, sectionIds);
		final var referencesBySectionId = loadReferences(projectId, sectionIds);

		return mapToDTO(
			record,
			eventsBySectionId.getOrDefault(sectionId, List.of()),
			metaFieldsBySectionId.getOrDefault(sectionId, List.of()),
			referencesBySectionId.getOrDefault(sectionId, List.of())
		);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "timelineGraphSections", key = "#projectId.toString() + ':' + #timelineGraphId.toString()"),
		@CacheEvict(value = "timelineGraph", key = "#projectId.toString() + ':' + #timelineGraphId.toString()"),
		@CacheEvict(value = "timelineGraphs", key = "#projectId.toString()")
	})
	public TimelineGraphSectionDTO createSection(final UUID projectId, final UUID timelineGraphId, final TimelineGraphSectionDTO dto) {
		final var sectionId = dto.getGraphSectionId() != null ? dto.getGraphSectionId() : UUID.randomUUID();

		insertSectionRecord(projectId, timelineGraphId, sectionId, dto);
		replaceChildData(projectId, timelineGraphId, sectionId, dto);

		return getSection(projectId, timelineGraphId, sectionId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "timelineGraphSections", key = "#projectId.toString() + ':' + #timelineGraphId.toString()"),
		@CacheEvict(value = "timelineGraphSection", key = "#projectId.toString() + ':' + #timelineGraphId.toString() + ':' + #sectionId.toString()")
	})
	public TimelineGraphSectionDTO updateSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final TimelineGraphSectionDTO dto) {
		dslContext.update(TIMELINE_GRAPH_SECTION)
			.set(TIMELINE_GRAPH_SECTION.CODE, dto.getId())
			.set(TIMELINE_GRAPH_SECTION.TYPE, TimelineGraphSectionType.valueOf(dto.getType()))
			.set(TIMELINE_GRAPH_SECTION.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(TIMELINE_GRAPH_SECTION.DATE_FIELD_ID, dto.getDateFieldId())
			.set(TIMELINE_GRAPH_SECTION.END_DATE_FIELD_ID, dto.getEndDateFieldId())
			.set(TIMELINE_GRAPH_SECTION.LABEL_FIELD_ID, dto.getLabelFieldId())
			.set(TIMELINE_GRAPH_SECTION.VALUE_FIELD_ID, dto.getValueFieldId())
			.set(TIMELINE_GRAPH_SECTION.HIDE_EXPECTED_EVENT, dto.isHideExpectedEvent())
			.set(TIMELINE_GRAPH_SECTION.HIDE_DONE_EVENT, dto.isHideDoneEvent())
			.set(TIMELINE_GRAPH_SECTION.USE_SCOPE_PATHS, dto.isUseScopePaths())
			.set(TIMELINE_GRAPH_SECTION.UNIT, dto.getUnit())
			.set(TIMELINE_GRAPH_SECTION.COLOR, dto.getColor())
			.set(TIMELINE_GRAPH_SECTION.STROKE_COLOR, dto.getStrokeColor())
			.set(TIMELINE_GRAPH_SECTION.OPACITY, dto.getOpacity())
			.set(TIMELINE_GRAPH_SECTION.DASHED, dto.isDashed())
			.set(TIMELINE_GRAPH_SECTION.MARK, dto.getMark() != null ? TimelineGraphSectionMark.valueOf(dto.getMark()) : null)
			.set(TIMELINE_GRAPH_SECTION.POSITION_START, dto.getPositionStart())
			.set(TIMELINE_GRAPH_SECTION.POSITION_STOP, dto.getPositionStop())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MIN, dto.getScaleMin())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MAX, dto.getScaleMax())
			.set(TIMELINE_GRAPH_SECTION.SCALE_DECIMAL, dto.getScaleDecimal())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MARK_INTERVAL, dto.getScaleMarkInterval())
			.set(TIMELINE_GRAPH_SECTION.SCALE_LABEL_INTERVAL, dto.getScaleLabelInterval())
			.set(TIMELINE_GRAPH_SECTION.SCALE_POSITION, dto.getScalePosition() != null ? TimelineGraphSectionScalePosition.valueOf(dto.getScalePosition()) : null)
			.set(TIMELINE_GRAPH_SECTION.HIDDEN_LEGEND, dto.isHiddenLegend())
			.set(TIMELINE_GRAPH_SECTION.HIDDEN, dto.isHidden())
			.set(TIMELINE_GRAPH_SECTION.LABEL, jsonMapperService.toJson(dto.getLabel()))
			.set(TIMELINE_GRAPH_SECTION.TOOLTIP, jsonMapperService.toJson(dto.getTooltip()))
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		replaceChildData(projectId, timelineGraphId, sectionId, dto);

		return getSection(projectId, timelineGraphId, sectionId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "timelineGraphSections", key = "#projectId.toString() + ':' + #timelineGraphId.toString()"),
		@CacheEvict(value = "timelineGraphSection", key = "#projectId.toString() + ':' + #timelineGraphId.toString() + ':' + #sectionId.toString()"),
		@CacheEvict(value = "timelineGraph", key = "#projectId.toString() + ':' + #timelineGraphId.toString()"),
		@CacheEvict(value = "timelineGraphs", key = "#projectId.toString()")
	})
	public void deleteSection(final UUID projectId, final UUID timelineGraphId, final UUID sectionId) {
		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_EVENT)
			.where(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_EVENT.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_META_FIELD)
			.where(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_META_FIELD.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION)
			.where(TIMELINE_GRAPH_SECTION.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID.eq(timelineGraphId))
			.and(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID.eq(sectionId))
			.execute();
	}

	private void replaceChildData(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final TimelineGraphSectionDTO dto) {
		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_REFERENCE)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_EVENT)
			.where(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		dslContext.deleteFrom(TIMELINE_GRAPH_SECTION_META_FIELD)
			.where(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID.eq(sectionId))
			.execute();

		insertSectionEvents(projectId, timelineGraphId, sectionId, dto.getEventModelIds());
		insertSectionMetaFields(projectId, timelineGraphId, sectionId, dto.getMetaFieldIds());
		insertSectionReferences(projectId, timelineGraphId, sectionId, dto.getReferences());
	}

	private void insertSectionEvents(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final List<UUID> eventModelIds) {
		if(eventModelIds == null || eventModelIds.isEmpty()) {
			return;
		}
		for(final var eventModelId : eventModelIds) {
			dslContext.insertInto(TIMELINE_GRAPH_SECTION_EVENT)
				.set(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID, projectId)
				.set(TIMELINE_GRAPH_SECTION_EVENT.TIMELINE_GRAPH_ID, timelineGraphId)
				.set(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID, sectionId)
				.set(TIMELINE_GRAPH_SECTION_EVENT.EVENT_MODEL_ID, eventModelId)
				.execute();
		}
	}

	private void insertSectionMetaFields(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final List<UUID> metaFieldIds) {
		if(metaFieldIds == null || metaFieldIds.isEmpty()) {
			return;
		}
		for(final var fieldModelId : metaFieldIds) {
			dslContext.insertInto(TIMELINE_GRAPH_SECTION_META_FIELD)
				.set(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID, projectId)
				.set(TIMELINE_GRAPH_SECTION_META_FIELD.TIMELINE_GRAPH_ID, timelineGraphId)
				.set(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID, sectionId)
				.set(TIMELINE_GRAPH_SECTION_META_FIELD.FIELD_MODEL_ID, fieldModelId)
				.execute();
		}
	}

	private void insertSectionReferences(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final List<TimelineGraphSectionReferenceDTO> references) {
		if(references == null || references.isEmpty()) {
			return;
		}
		for(final var refDTO : references) {
			final var referenceId = refDTO.getGraphReferenceId() != null ? refDTO.getGraphReferenceId() : UUID.randomUUID();

			dslContext.insertInto(TIMELINE_GRAPH_SECTION_REFERENCE)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_REFERENCE_ID, referenceId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID, projectId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.TIMELINE_GRAPH_ID, timelineGraphId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID, sectionId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.COLOR, refDTO.getColor())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.DASHED, refDTO.isDashed())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.REFERENCE_SECTION_ID, refDTO.getReferenceSectionId())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.LABEL, jsonMapperService.toJson(refDTO.getLabel()))
				.set(TIMELINE_GRAPH_SECTION_REFERENCE.TOOLTIP, jsonMapperService.toJson(refDTO.getTooltip()))
				.execute();

			insertReferenceEntries(projectId, timelineGraphId, sectionId, referenceId, refDTO.getEntries());
		}
	}

	private void insertReferenceEntries(final UUID projectId,
										final UUID timelineGraphId,
										final UUID sectionId,
										final UUID referenceId,
										final List<TimelineGraphSectionReferenceEntryDTO> entries) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		for(final var entry : entries) {
			dslContext.insertInto(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID, projectId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMELINE_GRAPH_ID, timelineGraphId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_SECTION_ID, sectionId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_REFERENCE_ID, referenceId)
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.TIMEPOINT, entry.getTimepoint())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.VALUE, entry.getValue())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.LABEL, entry.getLabel())
				.set(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.SORT_ORDER, entry.getSortOrder())
				.execute();
		}
	}

	private Map<UUID, List<UUID>> loadEventModelIds(final UUID projectId, final List<UUID> sectionIds) {
		final var rows = dslContext
			.select(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID, TIMELINE_GRAPH_SECTION_EVENT.EVENT_MODEL_ID)
			.from(TIMELINE_GRAPH_SECTION_EVENT)
			.where(TIMELINE_GRAPH_SECTION_EVENT.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_EVENT.GRAPH_SECTION_ID.in(sectionIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private Map<UUID, List<UUID>> loadMetaFieldIds(final UUID projectId, final List<UUID> sectionIds) {
		final var rows = dslContext
			.select(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID, TIMELINE_GRAPH_SECTION_META_FIELD.FIELD_MODEL_ID)
			.from(TIMELINE_GRAPH_SECTION_META_FIELD)
			.where(TIMELINE_GRAPH_SECTION_META_FIELD.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_META_FIELD.GRAPH_SECTION_ID.in(sectionIds))
			.fetch();

		final Map<UUID, List<UUID>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2());
		}
		return result;
	}

	private Map<UUID, List<TimelineGraphSectionReferenceDTO>> loadReferences(final UUID projectId, final List<UUID> sectionIds) {
		final var refRecords = dslContext
			.selectFrom(TIMELINE_GRAPH_SECTION_REFERENCE)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE.GRAPH_SECTION_ID.in(sectionIds))
			.fetch();

		if(refRecords.isEmpty()) {
			return Map.of();
		}

		final var referenceIds = refRecords.map(TimelineGraphSectionReferenceRecord::getGraphReferenceId);
		final var entriesByReferenceId = loadReferenceEntries(projectId, referenceIds);

		final Map<UUID, List<TimelineGraphSectionReferenceDTO>> result = new HashMap<>();
		for(final var record : refRecords) {
			final var dto = mapReferenceToDTO(record, entriesByReferenceId.getOrDefault(record.getGraphReferenceId(), List.of()));
			result.computeIfAbsent(record.getGraphSectionId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private Map<UUID, List<TimelineGraphSectionReferenceEntryDTO>> loadReferenceEntries(final UUID projectId, final List<UUID> referenceIds) {
		final var rows = dslContext
			.selectFrom(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY)
			.where(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.PROJECT_ID.eq(projectId))
			.and(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.GRAPH_REFERENCE_ID.in(referenceIds))
			.orderBy(TIMELINE_GRAPH_SECTION_REFERENCE_ENTRY.SORT_ORDER.asc().nullsLast())
			.fetch();

		final Map<UUID, List<TimelineGraphSectionReferenceEntryDTO>> result = new HashMap<>();
		for(final var row : rows) {
			final var entry = new TimelineGraphSectionReferenceEntryDTO();
			entry.setTimepoint(row.getTimepoint());
			entry.setValue(row.getValue());
			entry.setLabel(row.getLabel());
			entry.setSortOrder(row.getSortOrder());
			result.computeIfAbsent(row.getGraphReferenceId(), _ -> new ArrayList<>()).add(entry);
		}
		return result;
	}

	private void insertSectionRecord(final UUID projectId, final UUID timelineGraphId, final UUID sectionId, final TimelineGraphSectionDTO dto) {
		dslContext.insertInto(TIMELINE_GRAPH_SECTION)
			.set(TIMELINE_GRAPH_SECTION.GRAPH_SECTION_ID, sectionId)
			.set(TIMELINE_GRAPH_SECTION.PROJECT_ID, projectId)
			.set(TIMELINE_GRAPH_SECTION.TIMELINE_GRAPH_ID, timelineGraphId)
			.set(TIMELINE_GRAPH_SECTION.CODE, dto.getId())
			.set(TIMELINE_GRAPH_SECTION.TYPE, TimelineGraphSectionType.valueOf(dto.getType()))
			.set(TIMELINE_GRAPH_SECTION.DATASET_MODEL_ID, dto.getDatasetModelId())
			.set(TIMELINE_GRAPH_SECTION.DATE_FIELD_ID, dto.getDateFieldId())
			.set(TIMELINE_GRAPH_SECTION.END_DATE_FIELD_ID, dto.getEndDateFieldId())
			.set(TIMELINE_GRAPH_SECTION.LABEL_FIELD_ID, dto.getLabelFieldId())
			.set(TIMELINE_GRAPH_SECTION.VALUE_FIELD_ID, dto.getValueFieldId())
			.set(TIMELINE_GRAPH_SECTION.HIDE_EXPECTED_EVENT, dto.isHideExpectedEvent())
			.set(TIMELINE_GRAPH_SECTION.HIDE_DONE_EVENT, dto.isHideDoneEvent())
			.set(TIMELINE_GRAPH_SECTION.USE_SCOPE_PATHS, dto.isUseScopePaths())
			.set(TIMELINE_GRAPH_SECTION.UNIT, dto.getUnit())
			.set(TIMELINE_GRAPH_SECTION.COLOR, dto.getColor())
			.set(TIMELINE_GRAPH_SECTION.STROKE_COLOR, dto.getStrokeColor())
			.set(TIMELINE_GRAPH_SECTION.OPACITY, dto.getOpacity())
			.set(TIMELINE_GRAPH_SECTION.DASHED, dto.isDashed())
			.set(TIMELINE_GRAPH_SECTION.MARK, dto.getMark() != null ? TimelineGraphSectionMark.valueOf(dto.getMark()) : null)
			.set(TIMELINE_GRAPH_SECTION.POSITION_START, dto.getPositionStart())
			.set(TIMELINE_GRAPH_SECTION.POSITION_STOP, dto.getPositionStop())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MIN, dto.getScaleMin())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MAX, dto.getScaleMax())
			.set(TIMELINE_GRAPH_SECTION.SCALE_DECIMAL, dto.getScaleDecimal())
			.set(TIMELINE_GRAPH_SECTION.SCALE_MARK_INTERVAL, dto.getScaleMarkInterval())
			.set(TIMELINE_GRAPH_SECTION.SCALE_LABEL_INTERVAL, dto.getScaleLabelInterval())
			.set(TIMELINE_GRAPH_SECTION.SCALE_POSITION, dto.getScalePosition() != null ? TimelineGraphSectionScalePosition.valueOf(dto.getScalePosition()) : null)
			.set(TIMELINE_GRAPH_SECTION.HIDDEN_LEGEND, dto.isHiddenLegend())
			.set(TIMELINE_GRAPH_SECTION.HIDDEN, dto.isHidden())
			.set(TIMELINE_GRAPH_SECTION.LABEL, jsonMapperService.toJson(dto.getLabel()))
			.set(TIMELINE_GRAPH_SECTION.TOOLTIP, jsonMapperService.toJson(dto.getTooltip()))
			.execute();
	}

	private TimelineGraphSectionDTO mapToDTO(
		final TimelineGraphSectionRecord record,
		final List<UUID> eventModelIds,
		final List<UUID> metaFieldIds,
		final List<TimelineGraphSectionReferenceDTO> references
	) {
		final var dto = new TimelineGraphSectionDTO();
		dto.setGraphSectionId(record.getGraphSectionId());
		dto.setId(record.getCode());
		dto.setTimelineGraphId(record.getTimelineGraphId());
		dto.setType(record.getType().name());
		dto.setDatasetModelId(record.getDatasetModelId());
		dto.setDateFieldId(record.getDateFieldId());
		dto.setEndDateFieldId(record.getEndDateFieldId());
		dto.setLabelFieldId(record.getLabelFieldId());
		dto.setValueFieldId(record.getValueFieldId());
		dto.setHideExpectedEvent(record.getHideExpectedEvent());
		dto.setHideDoneEvent(record.getHideDoneEvent());
		dto.setUseScopePaths(record.getUseScopePaths());
		dto.setUnit(record.getUnit());
		dto.setColor(record.getColor());
		dto.setStrokeColor(record.getStrokeColor());
		dto.setOpacity(record.getOpacity());
		dto.setDashed(record.getDashed());
		dto.setMark(record.getMark() != null ? record.getMark().name() : null);
		dto.setPositionStart(record.getPositionStart());
		dto.setPositionStop(record.getPositionStop());
		dto.setScaleMin(record.getScaleMin());
		dto.setScaleMax(record.getScaleMax());
		dto.setScaleDecimal(record.getScaleDecimal());
		dto.setScaleMarkInterval(record.getScaleMarkInterval());
		dto.setScaleLabelInterval(record.getScaleLabelInterval());
		dto.setScalePosition(record.getScalePosition() != null ? record.getScalePosition().name() : null);
		dto.setHiddenLegend(record.getHiddenLegend());
		dto.setHidden(record.getHidden());
		dto.setLabel(jsonMapperService.fromJson(record.getLabel(), new TypeReference<>() {
		}));
		dto.setTooltip(jsonMapperService.fromJson(record.getTooltip(), new TypeReference<>() {
		}));
		dto.setEventModelIds(eventModelIds);
		dto.setMetaFieldIds(metaFieldIds);
		dto.setReferences(references);
		return dto;
	}

	private TimelineGraphSectionReferenceDTO mapReferenceToDTO(
		final TimelineGraphSectionReferenceRecord record,
		final List<TimelineGraphSectionReferenceEntryDTO> entries
	) {
		final var dto = new TimelineGraphSectionReferenceDTO();
		dto.setGraphReferenceId(record.getGraphReferenceId());
		dto.setColor(record.getColor());
		dto.setDashed(record.getDashed());
		dto.setReferenceSectionId(record.getReferenceSectionId());
		dto.setLabel(jsonMapperService.fromJson(record.getLabel(), new TypeReference<>() {
		}));
		dto.setTooltip(jsonMapperService.fromJson(record.getTooltip(), new TypeReference<>() {
		}));
		dto.setEntries(entries);
		return dto;
	}
}
