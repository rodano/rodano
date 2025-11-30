package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.layout.Cell;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.tables.records.FormLayoutCellRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;

@Repository
public class CellDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final VisibilityCriteriaDAO visibilityCriteriaDAO;
	private final RuleDAO ruleDAO;

	public CellDAO(final DSLContext dslContext,
				   final MappingHelper mappingHelper,
				   final VisibilityCriteriaDAO visibilityCriteriaDAO,
				   final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.visibilityCriteriaDAO = visibilityCriteriaDAO;
		this.ruleDAO = ruleDAO;
	}

	public List<Cell> findByLine(final UUID lineId) {
		return dslContext.selectFrom(FORM_LAYOUT_CELL)
			.where(FORM_LAYOUT_CELL.FORM_LAYOUT_LINE_ID.eq(lineId))
			.orderBy(FORM_LAYOUT_CELL.LINE_ORDER)
			.fetch(this::mapToModel);
	}

	private Cell mapToModel(final FormLayoutCellRecord record) {
		if(record == null) {
			return null;
		}

		final Cell model = new Cell();

		model.setId(record.getCode());
		model.setLayoutCellId(record.getFormLayoutCellId());

		model.setDisplayLabel(record.getDisplayLabel() != null ? record.getDisplayLabel() : false);
		model.setDisplayPossibleValueLabels(record.getDisplayPossibleValueLabels() != null ? record.getDisplayPossibleValueLabels() : false);
		model.setPossibleValuesColumnNumber(record.getPossibleValuesColumnNumber());
		model.setPossibleValuesColumnWidth(record.getPossibleValuesColumnWidth());
		model.setColspan(record.getColspan() != null ? record.getColspan() : 1);
		model.setCssCodeForLabel(record.getCssCodeForLabel());
		model.setCssCodeForInput(record.getCssCodeForInput());

		model.setTextBefore(mappingHelper.parseJsonToMap(record.getTextBefore()));
		model.setTextAfter(mappingHelper.parseJsonToMap(record.getTextAfter()));

		if(record.getDatasetModelId() != null) {
			model.setDatasetModelId(getDatasetModelCode(record.getDatasetModelId()));
		}

		if(record.getFieldModelId() != null) {
			model.setFieldModelId(getFieldModelCode(record.getFieldModelId()));
		}

		model.setVisibilityCriteria(visibilityCriteriaDAO.findByCell(record.getFormLayoutCellId()));

		model.setConstraint(ruleDAO.loadConstraintForOwner(RuleConstraintOwnerType.FORM_LAYOUT_CELL, record.getFormLayoutCellId()));

		return model;
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
