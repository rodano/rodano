package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.layout.Line;
import ch.rodano.core.model.jooq.tables.records.FormLayoutLineRecord;

import static ch.rodano.core.model.jooq.tables.FormLayoutLine.FORM_LAYOUT_LINE;

@Repository
public class LineDAO {

	private final DSLContext dslContext;
	private final CellDAO cellDAO;

	public LineDAO(final DSLContext dslContext, final CellDAO cellDAO) {
		this.dslContext = dslContext;
		this.cellDAO = cellDAO;
	}

	public List<Line> findByLayout(final UUID layoutId) {
		return dslContext.selectFrom(FORM_LAYOUT_LINE)
			.where(FORM_LAYOUT_LINE.FORM_LAYOUT_ID.eq(layoutId))
			.orderBy(FORM_LAYOUT_LINE.LINE_ORDER)
			.fetch(this::mapToModel);
	}

	private Line mapToModel(final FormLayoutLineRecord record) {
		final Line model = new Line();

		model.setLayoutLineId(record.getFormLayoutLineId());

		final var cells = cellDAO.findByLine(record.getFormLayoutLineId());

		for(final var cell : cells) {
			cell.setLine(model);
		}

		model.setCells(cells);

		return model;
	}
}
