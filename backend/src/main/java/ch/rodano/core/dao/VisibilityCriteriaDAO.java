package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.layout.VisibilityCriteria;
import ch.rodano.configuration.model.layout.VisibilityCriterionAction;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.core.model.jooq.tables.records.FormCellVisibilityCriteriaRecord;

import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteria.FORM_CELL_VISIBILITY_CRITERIA;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetCell.FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaTargetLayout.FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormCellVisibilityCriteriaValue.FORM_CELL_VISIBILITY_CRITERIA_VALUE;
import static ch.rodano.core.model.jooq.tables.FormLayout.FORM_LAYOUT;
import static ch.rodano.core.model.jooq.tables.FormLayoutCell.FORM_LAYOUT_CELL;

@Repository
public class VisibilityCriteriaDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public VisibilityCriteriaDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<VisibilityCriteria> findByCell(final UUID cellId) {
		return dslContext.selectFrom(FORM_CELL_VISIBILITY_CRITERIA)
			.where(FORM_CELL_VISIBILITY_CRITERIA.FORM_LAYOUT_CELL_ID.eq(cellId))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA.LINE_ORDER)
			.fetch(this::mapToModel);
	}

	private VisibilityCriteria mapToModel(final FormCellVisibilityCriteriaRecord record) {
		if(record == null) {
			return null;
		}

		final VisibilityCriteria model = new VisibilityCriteria();

		model.setVisibilityCriteriaId(record.getFormCellVisibleCriteriaId());

		model.setOperator(record.getOperator() != null
			? mappingHelper.parseEnum(Operator.class, record.getOperator().name(), "operator")
			: null);
		model.setAction(mappingHelper.parseEnum(VisibilityCriterionAction.class, record.getAction().name(), "action"));

		model.setValues(loadValues(record.getFormCellVisibleCriteriaId()));
		model.setTargetLayoutIds(loadTargetLayoutIds(record.getFormCellVisibleCriteriaId()));
		model.setTargetCellIds(loadTargetCellIds(record.getFormCellVisibleCriteriaId()));

		return model;
	}

	private List<String> loadValues(final UUID criteriaId) {
		return dslContext
			.select(FIELD_POSSIBLE_VALUE.CODE, FORM_CELL_VISIBILITY_CRITERIA_VALUE.VALUE)
			.from(FORM_CELL_VISIBILITY_CRITERIA_VALUE)
			.leftJoin(FIELD_POSSIBLE_VALUE)
			.on(FIELD_POSSIBLE_VALUE.POSSIBLE_VALUE_ID.eq(
				FORM_CELL_VISIBILITY_CRITERIA_VALUE.POSSIBLE_VALUE_ID))
			.where(FORM_CELL_VISIBILITY_CRITERIA_VALUE.FORM_CELL_VISIBLE_CRITERIA_ID.eq(criteriaId))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_VALUE.LINE_ORDER)
			.fetch(row -> row.value1() != null ? row.value1() : row.value2());
	}

	private List<String> loadTargetLayoutIds(final UUID criteriaId) {
		return dslContext.select(FORM_LAYOUT.CODE)
			.from(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT)
			.join(FORM_LAYOUT)
			.on(FORM_LAYOUT.FORM_LAYOUT_ID.eq(
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.TARGET_LAYOUT_ID))
			.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.FORM_CELL_VISIBLE_CRITERIA_ID.eq(criteriaId))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_TARGET_LAYOUT.LINE_ORDER)
			.fetch(FORM_LAYOUT.CODE);
	}

	private List<String> loadTargetCellIds(final UUID criteriaId) {
		return dslContext.select(FORM_LAYOUT_CELL.CODE)
			.from(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL)
			.join(FORM_LAYOUT_CELL)
			.on(FORM_LAYOUT_CELL.FORM_LAYOUT_CELL_ID.eq(
				FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.TARGET_CELL_ID))
			.where(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.FORM_CELL_VISIBLE_CRITERIA_ID.eq(criteriaId))
			.orderBy(FORM_CELL_VISIBILITY_CRITERIA_TARGET_CELL.LINE_ORDER)
			.fetch(FORM_LAYOUT_CELL.CODE);
	}
}
