package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.layout.ColumnHeader;
import ch.rodano.core.model.jooq.tables.records.FormLayoutColumnRecord;

import static ch.rodano.core.model.jooq.tables.FormLayoutColumn.FORM_LAYOUT_COLUMN;

@Repository
public class ColumnHeaderDAO {

	private final DSLContext dslContext;

	public ColumnHeaderDAO(final DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<ColumnHeader> findByLayout(final UUID layoutId) {
		return dslContext.selectFrom(FORM_LAYOUT_COLUMN)
			.where(FORM_LAYOUT_COLUMN.FORM_LAYOUT_ID.eq(layoutId))
			.orderBy(FORM_LAYOUT_COLUMN.COL_ORDER)
			.fetch(this::mapToModel);
	}

	private ColumnHeader mapToModel(final FormLayoutColumnRecord record) {
		if(record == null) {
			return null;
		}

		final ColumnHeader model = new ColumnHeader();

		model.setCssCode(record.getCssCode());

		return model;
	}
}
