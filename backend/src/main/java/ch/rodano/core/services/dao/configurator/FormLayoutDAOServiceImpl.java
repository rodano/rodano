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

import ch.rodano.api.config.CellDTO;
import ch.rodano.api.config.ColumnHeaderDTO;
import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.api.config.LayoutDTO;
import ch.rodano.api.config.LayoutLineDTO;
import ch.rodano.api.config.VisibilityCriteriaDTO;
import ch.rodano.configuration.model.layout.LayoutType;
import ch.rodano.configuration.model.layout.VisibilityCriterionAction;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.core.model.jooq.enums.FormCellVisibilityCriteriaAction;
import ch.rodano.core.model.jooq.enums.FormCellVisibilityCriteriaOperator;
import ch.rodano.core.model.jooq.enums.FormLayoutType;
import ch.rodano.core.model.jooq.tables.records.FormCellVisibilityCriteriaRecord;
import ch.rodano.core.model.jooq.tables.records.FormLayoutCellRecord;
import ch.rodano.core.model.jooq.tables.records.FormLayoutLineRecord;
import ch.rodano.core.model.jooq.tables.records.FormLayoutRecord;

import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteria.FORM_CELL_VISIBILITY_CRITERIA;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetCell.FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetLayout.FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaValue.FORM_CELL_VISIBILITY_CRITERIA_VALUE;
import static ch.rodano.core.model.jooq.tables.FormLayout.FORM_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;
import static ch.rodano.core.model.jooq.tables.FormLayoutColumn.FORM_LAYOUT_COLUMN;
import static ch.rodano.core.model.jooq.tables.FormLayoutLine.FORM_LAYOUT_LINE;

@Repository
public class FormLayoutDAOServiceImpl implements FormLayoutDAOService {

	private final DSLContext dslContext;
	private final JsonMapperService jsonMapperService;

	public FormLayoutDAOServiceImpl(final DSLContext dslContext, final JsonMapperService jsonMapperService) {
		this.dslContext = dslContext;
		this.jsonMapperService = jsonMapperService;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "layouts", key = "#projectId.toString() + ':' + #formModelId.toString()")
	public List<LayoutDTO> getLayouts(final UUID projectId, final UUID formModelId) {
		final var records = dslContext
			.selectFrom(FORM_LAYOUT)
			.where(FORM_LAYOUT.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT.FORM_MODEL_ID.eq(formModelId))
			.orderBy(FORM_LAYOUT.SORT_ORDER.asc())
			.fetch();

		if(records.isEmpty()) {
			return List.of();
		}

		return mapToDTOs(projectId, records);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "layout", key = "#projectId.toString() + ':' + #formModelId.toString() + ':' + #formLayoutId.toString()")
	public LayoutDTO getLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId) {
		final var record = dslContext
			.selectFrom(FORM_LAYOUT)
			.where(FORM_LAYOUT.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT.FORM_MODEL_ID.eq(formModelId))
			.and(FORM_LAYOUT.FORM_LAYOUT_ID.eq(formLayoutId))
			.fetchOne();

		if(record == null) {
			return null;
		}

		return mapToDTOs(projectId, List.of(record)).getFirst();
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "layouts", key = "#projectId.toString() + ':' + #formModelId.toString()")
	})
	public LayoutDTO createLayout(final UUID projectId, final UUID formModelId, final LayoutDTO dto) {
		final var formLayoutId = dto.getFormLayoutId() != null ? dto.getFormLayoutId() : UUID.randomUUID();

		dslContext.insertInto(FORM_LAYOUT)
			.set(FORM_LAYOUT.FORM_LAYOUT_ID, formLayoutId)
			.set(FORM_LAYOUT.PROJECT_ID, projectId)
			.set(FORM_LAYOUT.FORM_MODEL_ID, formModelId)
			.set(FORM_LAYOUT.CODE, dto.getId())
			.set(FORM_LAYOUT.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FORM_LAYOUT.TYPE, dto.getType() != null ? FormLayoutType.valueOf(dto.getType().name()) : null)
			.set(FORM_LAYOUT.DATASET_MODEL_ID, dto.getDatasetModel() != null ? dto.getDatasetModel().getDatasetModelId() : null)
			.set(FORM_LAYOUT.DEFAULT_SORT_FIELD_MODEL_ID, dto.getDefaultSortFieldModelId() != null ? UUID.fromString(dto.getDefaultSortFieldModelId()) : null)
			.set(FORM_LAYOUT.TEXT_BEFORE, jsonMapperService.toJson(dto.getTextBefore()))
			.set(FORM_LAYOUT.TEXT_AFTER, jsonMapperService.toJson(dto.getTextAfter()))
			.set(FORM_LAYOUT.CSS_CODE, dto.getCssCode())
			.set(FORM_LAYOUT.SORT_ORDER, dto.getSortOrder())
			.execute();

		replaceLayoutContent(projectId, formModelId, formLayoutId, dto);

		return getLayout(projectId, formModelId, formLayoutId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "layouts", key = "#projectId.toString() + ':' + #formModelId.toString()"),
		@CacheEvict(value = "layout", key = "#projectId.toString() + ':' + #formModelId.toString() + ':' + #formLayoutId.toString()")
	})
	public LayoutDTO updateLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId, final LayoutDTO dto) {
		dslContext.update(FORM_LAYOUT)
			.set(FORM_LAYOUT.CODE, dto.getId())
			.set(FORM_LAYOUT.DESCRIPTION, jsonMapperService.toJson(dto.getDescription()))
			.set(FORM_LAYOUT.TYPE, dto.getType() != null ? FormLayoutType.valueOf(dto.getType().name()) : null)
			.set(FORM_LAYOUT.DATASET_MODEL_ID, dto.getDatasetModel() != null ? dto.getDatasetModel().getDatasetModelId() : null)
			.set(FORM_LAYOUT.DEFAULT_SORT_FIELD_MODEL_ID, dto.getDefaultSortFieldModelId() != null ? UUID.fromString(dto.getDefaultSortFieldModelId()) : null)
			.set(FORM_LAYOUT.TEXT_BEFORE, jsonMapperService.toJson(dto.getTextBefore()))
			.set(FORM_LAYOUT.TEXT_AFTER, jsonMapperService.toJson(dto.getTextAfter()))
			.set(FORM_LAYOUT.CSS_CODE, dto.getCssCode())
			.set(FORM_LAYOUT.SORT_ORDER, dto.getSortOrder())
			.where(FORM_LAYOUT.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT.FORM_LAYOUT_ID.eq(formLayoutId))
			.execute();

		replaceLayoutContent(projectId, formModelId, formLayoutId, dto);

		return getLayout(projectId, formModelId, formLayoutId);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "layouts", key = "#projectId.toString() + ':' + #formModelId.toString()"),
		@CacheEvict(value = "layout", key = "#projectId.toString() + ':' + #formModelId.toString() + ':' + #formLayoutId.toString()")
	})
	public void deleteLayout(final UUID projectId, final UUID formModelId, final UUID formLayoutId) {
		deleteLayoutContent(projectId, formModelId, formLayoutId);

		dslContext.deleteFrom(FORM_LAYOUT)
			.where(FORM_LAYOUT.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT.FORM_LAYOUT_ID.eq(formLayoutId))
			.execute();
	}

	private void replaceLayoutContent(final UUID projectId, final UUID formModelId, final UUID formLayoutId, final LayoutDTO dto) {
		deleteLayoutContent(projectId, formModelId, formLayoutId);

		if(dto.getColumns() != null) {
			for(int i = 0; i < dto.getColumns().size(); i++) {
				final var col = dto.getColumns().get(i);
				dslContext.insertInto(FORM_LAYOUT_COLUMN)
					.set(FORM_LAYOUT_COLUMN.PROJECT_ID, projectId)
					.set(FORM_LAYOUT_COLUMN.FORM_MODEL_ID, formModelId)
					.set(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID, formLayoutId)
					.set(FORM_LAYOUT_COLUMN.COL_ORDER, i)
					.set(FORM_LAYOUT_COLUMN.CSS_CODE, col.cssCode())
					.execute();
			}
		}

		if(dto.getLines() != null) {
			for(int lineIdx = 0; lineIdx < dto.getLines().size(); lineIdx++) {
				final var line = dto.getLines().get(lineIdx);
				final var lineId = line.getFormLayoutLineId() != null ? line.getFormLayoutLineId() : UUID.randomUUID();

				dslContext.insertInto(FORM_LAYOUT_LINE)
					.set(FORM_LAYOUT_LINE.FORM_LAYOUT_LINE_ID, lineId)
					.set(FORM_LAYOUT_LINE.PROJECT_ID, projectId)
					.set(FORM_LAYOUT_LINE.FORM_MODEL_ID, formModelId)
					.set(FORM_LAYOUT_LINE.FORM_LAYOUT_ID, formLayoutId)
					.set(FORM_LAYOUT_LINE.LINE_ORDER, lineIdx)
					.execute();

				if(line.getCells() != null) {
					for(int cellIdx = 0; cellIdx < line.getCells().size(); cellIdx++) {
						final var cell = line.getCells().get(cellIdx);
						final var cellId = cell.getFormLayoutCellId() != null ? cell.getFormLayoutCellId() : UUID.randomUUID();

						dslContext.insertInto(FORM_LAYOUT_CELL)
							.set(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID, cellId)
							.set(FORM_LAYOUT_CELL.PROJECT_ID, projectId)
							.set(FORM_LAYOUT_CELL.FORM_MODEL_ID, formModelId)
							.set(FORM_LAYOUT_CELL.FORM_LAYOUT_ID, formLayoutId)
							.set(FORM_LAYOUT_CELL.FORM_LAYOUT_LINE_ID, lineId)
							.set(FORM_LAYOUT_CELL.LINE_ORDER, cellIdx)
							.set(FORM_LAYOUT_CELL.CODE, cell.getId())
							.set(FORM_LAYOUT_CELL.DATASET_MODEL_ID, cell.getDatasetModelId())
							.set(FORM_LAYOUT_CELL.FIELD_MODEL_ID, cell.getFieldModelId())
							.set(FORM_LAYOUT_CELL.TEXT_BEFORE, jsonMapperService.toJson(cell.getTextBefore()))
							.set(FORM_LAYOUT_CELL.TEXT_AFTER, jsonMapperService.toJson(cell.getTextAfter()))
							.set(FORM_LAYOUT_CELL.DISPLAY_LABEL, cell.getDisplayLabel())
							.set(FORM_LAYOUT_CELL.DISPLAY_POSSIBLE_VALUE_LABELS, cell.isDisplayPossibleValueLabels())
							.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_NUMBER, cell.getPossibleValuesColumnNumber())
							.set(FORM_LAYOUT_CELL.POSSIBLE_VALUES_COLUMN_WIDTH, cell.getPossibleValuesColumnWidth())
							.set(FORM_LAYOUT_CELL.COLSPAN, cell.getColspan())
							.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_LABEL, cell.getCssCodeForLabel())
							.set(FORM_LAYOUT_CELL.CSS_CODE_FOR_INPUT, cell.getCssCodeForInput())
							.execute();

						if(cell.getVisibilityCriteria() != null) {
							for(int criteriaIdx = 0; criteriaIdx < cell.getVisibilityCriteria().size(); criteriaIdx++) {
								insertVisibilityCriteria(projectId, cellId, cell.getVisibilityCriteria().get(criteriaIdx), criteriaIdx);
							}
						}
					}
				}
			}
		}
	}

	private void insertVisibilityCriteria(final UUID projectId,
										  final UUID cellId,
										  final VisibilityCriteriaDTO criteria,
										  final int order) {
		final var criteriaId = criteria.getFormCellVisibilityCriteriaId() != null
			? criteria.getFormCellVisibilityCriteriaId()
			: UUID.randomUUID();

		dslContext.insertInto(FORM_CELL_VISIBILITY_CRITERIA)
			.set(FORM_CELL_VISIBILITY_CRITERIA.FORM_CELL_VISIBLE_CRITERIA_ID, criteriaId)
			.set(FORM_CELL_VISIBILITY_CRITERIA.PROJECT_ID, projectId)
			.set(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID, cellId)
			.set(FORM_CELL_VISIBILITY_CRITERIA.LINE_ORDER, order)
			.set(FORM_CELL_VISIBILITY_CRITERIA.OPERATOR, criteria.getOperator() != null ? FormCellVisibilityCriteriaOperator.valueOf(criteria.getOperator().name()) : null)
			.set(FORM_CELL_VISIBILITY_CRITERIA.ACTION, criteria.getAction() != null ? FormCellVisibilityCriteriaAction.valueOf(criteria.getAction().name()) : null)
			.execute();

		if(criteria.getValues() != null) {
			for(int i = 0; i < criteria.getValues().size(); i++) {
				final var raw = criteria.getValues().get(i);
				UUID possibleValueId = null;
				String rawValue = null;
				try {
					possibleValueId = UUID.fromString(raw);
				}
				catch(IllegalArgumentException e) {
					rawValue = raw;
				}
				dslContext.insertInto(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID, criteriaId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.PROJECT_ID, projectId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_LAYOUT_CELL_ID, cellId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.POSSIBLE_VALUE_ID, possibleValueId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.VALUE, rawValue)
					.set(FORM_CELL_VISIBILITY_CRITERIA_VALUE.LINE_ORDER, i)
					.execute();
			}
		}

		if(criteria.getTargetLayoutIds() != null) {
			for(int i = 0; i < criteria.getTargetLayoutIds().size(); i++) {
				dslContext.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID, criteriaId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID, projectId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_LAYOUT_CELL_ID, cellId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER, i)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.TARGET_LAYOUT_ID, UUID.fromString(criteria.getTargetLayoutIds().get(i)))
					.execute();
			}
		}

		if(criteria.getTargetCellIds() != null) {
			for(int i = 0; i < criteria.getTargetCellIds().size(); i++) {
				dslContext.insertInto(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID, criteriaId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID, projectId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_LAYOUT_CELL_ID, cellId)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER, i)
					.set(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.TARGET_CELL_ID, UUID.fromString(criteria.getTargetCellIds().get(i)))
					.execute();
			}
		}
	}

	private void deleteLayoutContent(final UUID projectId, final UUID formModelId, final UUID formLayoutId) {
		final var colOrders = dslContext
			.select(FORM_LAYOUT_COLUMN.COL_ORDER)
			.from(FORM_LAYOUT_COLUMN)
			.where(FORM_LAYOUT_COLUMN.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT_COLUMN.FORM_MODEL_ID.eq(formModelId))
			.and(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID.eq(formLayoutId))
			.fetchInto(Integer.class);

		final var lineIds = dslContext
			.select(FORM_LAYOUT_LINE.FORM_LAYOUT_LINE_ID)
			.from(FORM_LAYOUT_LINE)
			.where(FORM_LAYOUT_LINE.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT_LINE.FORM_MODEL_ID.eq(formModelId))
			.and(FORM_LAYOUT_LINE.FORM_LAYOUT_ID.eq(formLayoutId))
			.fetchInto(UUID.class);

		final var cellIds = lineIds.isEmpty() ? List.<UUID> of() :
			dslContext
				.select(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID)
				.from(FORM_LAYOUT_CELL)
				.where(FORM_LAYOUT_CELL.PROJECT_ID.eq(projectId))
				.and(FORM_LAYOUT_CELL.FORM_MODEL_ID.eq(formModelId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_ID.eq(formLayoutId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_LINE_ID.in(lineIds))
				.fetchInto(UUID.class);

		final var criteriaIds = cellIds.isEmpty() ? List.<UUID> of() :
			dslContext
				.select(FORM_CELL_VISIBILITY_CRITERIA.FORM_CELL_VISIBLE_CRITERIA_ID)
				.from(FORM_CELL_VISIBILITY_CRITERIA)
				.where(FORM_CELL_VISIBILITY_CRITERIA.PROJECT_ID.eq(projectId))
				.and(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID.in(cellIds))
				.fetchInto(UUID.class);

		if(!criteriaIds.isEmpty()) {
			final var valueRecords = dslContext
				.selectFrom(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
				.where(FORM_CELL_VISIBILITY_CRITERIA_VALUE.PROJECT_ID.eq(projectId))
				.and(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
				.fetch();

			for(final var row : valueRecords) {
				dslContext.deleteFrom(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
					.where(FORM_CELL_VISIBILITY_CRITERIA_VALUE.PROJECT_ID.eq(row.getProjectId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID.eq(row.getFormCellVisibleCriteriaId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_LAYOUT_CELL_ID.eq(row.getFormLayoutCellId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_VALUE.LINE_ORDER.eq(row.getLineOrder()))
					.execute();
			}

			final var targetLayoutRecords = dslContext
				.selectFrom(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
				.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID.eq(projectId))
				.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
				.fetch();

			for(final var row : targetLayoutRecords) {
				dslContext.deleteFrom(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
					.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID.eq(row.getProjectId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID.eq(row.getFormCellVisibleCriteriaId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_LAYOUT_CELL_ID.eq(row.getFormLayoutCellId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER.eq(row.getLineOrder()))
					.execute();
			}

			final var targetCellRecords = dslContext
				.selectFrom(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
				.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID.eq(projectId))
				.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
				.fetch();

			for(final var row : targetCellRecords) {
				dslContext.deleteFrom(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
					.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID.eq(row.getProjectId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID.eq(row.getFormCellVisibleCriteriaId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_LAYOUT_CELL_ID.eq(row.getFormLayoutCellId()))
					.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER.eq(row.getLineOrder()))
					.execute();
			}
		}

		if(!cellIds.isEmpty()) {
			dslContext.deleteFrom(FORM_CELL_VISIBILITY_CRITERIA)
				.where(FORM_CELL_VISIBILITY_CRITERIA.PROJECT_ID.eq(projectId))
				.and(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID.in(cellIds))
				.execute();

			dslContext.deleteFrom(FORM_LAYOUT_CELL)
				.where(FORM_LAYOUT_CELL.PROJECT_ID.eq(projectId))
				.and(FORM_LAYOUT_CELL.FORM_MODEL_ID.eq(formModelId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_ID.in(formLayoutId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID.in(cellIds))
				.execute();
		}

		if(!colOrders.isEmpty()) {
			dslContext.deleteFrom(FORM_LAYOUT_COLUMN)
				.where(FORM_LAYOUT_COLUMN.PROJECT_ID.eq(projectId))
				.and(FORM_LAYOUT_COLUMN.FORM_MODEL_ID.eq(formModelId))
				.and(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID.eq(formLayoutId))
				.and(FORM_LAYOUT_COLUMN.COL_ORDER.in(colOrders))
				.execute();
		}

		if(!lineIds.isEmpty()) {
			dslContext.deleteFrom(FORM_LAYOUT_LINE)
				.where(FORM_LAYOUT_LINE.PROJECT_ID.eq(projectId))
				.and(FORM_LAYOUT_LINE.FORM_MODEL_ID.eq(formModelId))
				.and(FORM_LAYOUT_LINE.FORM_LAYOUT_ID.eq(formLayoutId))
				.execute();
		}
	}

	private List<LayoutDTO> mapToDTOs(final UUID projectId, final List<FormLayoutRecord> records) {
		final var layoutIds = records.stream()
			.map(FormLayoutRecord::getFormLayoutId)
			.toList();

		final var columnsByLayoutId = loadColumns(projectId, layoutIds);

		final var lineRecords = dslContext
			.selectFrom(FORM_LAYOUT_LINE)
			.where(FORM_LAYOUT_LINE.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT_LINE.FORM_LAYOUT_ID.in(layoutIds))
			.orderBy(FORM_LAYOUT_LINE.LINE_ORDER)
			.fetch();

		final var lineIds = lineRecords.stream()
			.map(FormLayoutLineRecord::getFormLayoutLineId)
			.toList();

		final List<FormLayoutCellRecord> cellRecords;
		if(lineIds.isEmpty()) {
			cellRecords = List.of();
		}
		else {
			cellRecords = dslContext
				.selectFrom(FORM_LAYOUT_CELL)
				.where(FORM_LAYOUT_CELL.PROJECT_ID.eq(projectId))
				.and(FORM_LAYOUT_CELL.FORM_LAYOUT_LINE_ID.in(lineIds))
				.orderBy(FORM_LAYOUT_CELL.LINE_ORDER)
				.fetch();
		}

		final var cellIds = cellRecords.stream()
			.map(FormLayoutCellRecord::getFormLayoutCellId)
			.toList();

		final var criteriaByCell = loadCriteria(projectId, cellIds);

		final Map<UUID, List<CellDTO>> cellsByLineId = new HashMap<>();
		for(final var cellRecord : cellRecords) {
			cellsByLineId
				.computeIfAbsent(cellRecord.getFormLayoutLineId(), _ -> new ArrayList<>())
				.add(mapCellToDTO(cellRecord, criteriaByCell.getOrDefault(cellRecord.getFormLayoutCellId(), List.of())));
		}

		final Map<UUID, List<LayoutLineDTO>> linesByLayoutId = new HashMap<>();
		for(final var lineRecord : lineRecords) {
			final var lineDTO = new LayoutLineDTO();
			lineDTO.setFormLayoutLineId(lineRecord.getFormLayoutLineId());
			lineDTO.setCells(cellsByLineId.getOrDefault(lineRecord.getFormLayoutLineId(), List.of()));
			linesByLayoutId
				.computeIfAbsent(lineRecord.getFormLayoutId(), _ -> new ArrayList<>())
				.add(lineDTO);
		}

		return records.stream()
			.map(record -> mapLayoutToDTO(
				record,
				columnsByLayoutId.getOrDefault(record.getFormLayoutId(), List.of()),
				linesByLayoutId.getOrDefault(record.getFormLayoutId(), List.of())
			))
			.toList();
	}

	private Map<UUID, List<ColumnHeaderDTO>> loadColumns(final UUID projectId, final List<UUID> layoutIds) {
		if(layoutIds.isEmpty()) {
			return Map.of();
		}

		final var rows = dslContext
			.select(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID, FORM_LAYOUT_COLUMN.CSS_CODE)
			.from(FORM_LAYOUT_COLUMN)
			.where(FORM_LAYOUT_COLUMN.PROJECT_ID.eq(projectId))
			.and(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID.in(layoutIds))
			.orderBy(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID, FORM_LAYOUT_COLUMN.COL_ORDER)
			.fetch();

		final Map<UUID, List<ColumnHeaderDTO>> result = new HashMap<>();
		for(final var row : rows) {
			result.computeIfAbsent(row.value1(), _ -> new ArrayList<>())
				.add(new ColumnHeaderDTO(row.value2()));
		}
		return result;
	}

	private Map<UUID, List<VisibilityCriteriaDTO>> loadCriteria(final UUID projectId, final List<UUID> cellIds) {
		if(cellIds.isEmpty()) {
			return Map.of();
		}

		final var criteriaRecords = dslContext
			.selectFrom(FORM_CELL_VISIBILITY_CRITERIA)
			.where(FORM_CELL_VISIBILITY_CRITERIA.PROJECT_ID.eq(projectId))
			.and(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID.in(cellIds))
			.fetch();

		if(criteriaRecords.isEmpty()) {
			return Map.of();
		}

		final var criteriaIds = criteriaRecords.stream()
			.map(FormCellVisibilityCriteriaRecord::getFormCellVisibleCriteriaId)
			.toList();

		final var valueRows = dslContext
			.select(
				FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID,
				FORM_CELL_VISIBILITY_CRITERIA_VALUE.POSSIBLE_VALUE_ID,
				FORM_CELL_VISIBILITY_CRITERIA_VALUE.VALUE
			)
			.from(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
			.where(FORM_CELL_VISIBILITY_CRITERIA_VALUE.PROJECT_ID.eq(projectId))
			.and(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_VALUE.LINE_ORDER)
			.fetch();

		final Map<UUID, List<String>> valuesByCriteriaId = new HashMap<>();
		for(final var row : valueRows) {
			final String value = row.value2() != null ? row.value2().toString() : row.value3();
			valuesByCriteriaId.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(value);
		}

		final var targetLayoutRows = dslContext
			.select(
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID,
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.TARGET_LAYOUT_ID
			)
			.from(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
			.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.PROJECT_ID.eq(projectId))
			.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER)
			.fetch();

		final Map<UUID, List<String>> targetLayoutsByCriteriaId = new HashMap<>();
		for(final var row : targetLayoutRows) {
			targetLayoutsByCriteriaId.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2().toString());
		}

		final var targetCellRows = dslContext
			.select(
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID,
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.TARGET_CELL_ID
			)
			.from(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
			.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.PROJECT_ID.eq(projectId))
			.and(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID.in(criteriaIds))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER)
			.fetch();

		final Map<UUID, List<String>> targetCellsByCriteriaId = new HashMap<>();
		for(final var row : targetCellRows) {
			targetCellsByCriteriaId.computeIfAbsent(row.value1(), _ -> new ArrayList<>()).add(row.value2().toString());
		}

		final Map<UUID, List<VisibilityCriteriaDTO>> result = new HashMap<>();
		for(final var criteriaRecord : criteriaRecords) {
			final var criteriaId = criteriaRecord.getFormCellVisibleCriteriaId();

			final var dto = new VisibilityCriteriaDTO();
			dto.setFormCellVisibilityCriteriaId(criteriaId);
			dto.setOperator(criteriaRecord.getOperator() != null ? Operator.valueOf(criteriaRecord.getOperator().name()) : null);
			dto.setAction(criteriaRecord.getAction() != null ? VisibilityCriterionAction.valueOf(criteriaRecord.getAction().name()) : null);
			dto.setValues(valuesByCriteriaId.getOrDefault(criteriaId, List.of()));
			dto.setTargetLayoutIds(targetLayoutsByCriteriaId.getOrDefault(criteriaId, List.of()));
			dto.setTargetCellIds(targetCellsByCriteriaId.getOrDefault(criteriaId, List.of()));

			result.computeIfAbsent(criteriaRecord.getFormLayoutCellId(), _ -> new ArrayList<>()).add(dto);
		}
		return result;
	}

	private CellDTO mapCellToDTO(final FormLayoutCellRecord record, final List<VisibilityCriteriaDTO> visibilityCriteria) {
		final var dto = new CellDTO();
		dto.setFormLayoutCellId(record.getFormLayoutCellId());
		dto.setId(record.getCode());
		dto.setDatasetModelId(record.getDatasetModelId());
		dto.setFieldModelId(record.getFieldModelId());
		dto.setTextBefore(jsonMapperService.fromJson(record.getTextBefore(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setTextAfter(jsonMapperService.fromJson(record.getTextAfter(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setDisplayLabel(record.getDisplayLabel());
		dto.setDisplayPossibleValueLabels(record.getDisplayPossibleValueLabels());
		dto.setPossibleValuesColumnNumber(record.getPossibleValuesColumnNumber());
		dto.setPossibleValuesColumnWidth(record.getPossibleValuesColumnWidth());
		dto.setColspan(record.getColspan());
		dto.setCssCodeForLabel(record.getCssCodeForLabel());
		dto.setCssCodeForInput(record.getCssCodeForInput());
		dto.setVisibilityCriteria(visibilityCriteria);
		return dto;
	}

	private LayoutDTO mapLayoutToDTO(
		final FormLayoutRecord record,
		final List<ColumnHeaderDTO> columns,
		final List<LayoutLineDTO> lines
	) {
		final var dto = new LayoutDTO();
		dto.setFormLayoutId(record.getFormLayoutId());
		dto.setFormModelId(record.getFormModelId());
		dto.setId(record.getCode());
		dto.setDescription(jsonMapperService.fromJson(record.getDescription(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setType(record.getType() != null ? LayoutType.valueOf(record.getType().name()) : null);
		if(record.getDatasetModelId() != null) {
			final var datasetModel = new DatasetModelDTO();
			datasetModel.setDatasetModelId(record.getDatasetModelId());
			dto.setDatasetModel(datasetModel);
		}
		dto.setDefaultSortFieldModelId(record.getDefaultSortFieldModelId() != null ? record.getDefaultSortFieldModelId().toString() : null);
		dto.setTextBefore(jsonMapperService.fromJson(record.getTextBefore(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setTextAfter(jsonMapperService.fromJson(record.getTextAfter(), new TypeReference<TreeMap<String, String>>() {
		}));
		dto.setCssCode(record.getCssCode());
		dto.setSortOrder(record.getSortOrder());
		dto.setColumns(columns);
		dto.setLines(lines);
		return dto;
	}
}
