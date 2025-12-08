package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.core.model.jooq.tables.records.FieldPossibleValueRecord;

import static ch.rodano.core.model.jooq.tables.FieldPossibleValue.FIELD_POSSIBLE_VALUE;

@Repository
public class PossibleValueDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;

	public PossibleValueDAO(final DSLContext dslContext, final MappingHelper mappingHelper) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
	}

	public List<PossibleValue> findByFieldModel(final UUID fieldModelId) {
		return dslContext.selectFrom(FIELD_POSSIBLE_VALUE)
			.where(FIELD_POSSIBLE_VALUE.FIELD_MODEL_ID.eq(fieldModelId))
			.orderBy(FIELD_POSSIBLE_VALUE.SORT_ORDER.asc())
			.fetch()
			.map(this::mapToModel);
	}

	private PossibleValue mapToModel(final FieldPossibleValueRecord record) {
		final PossibleValue model = new PossibleValue();

		model.setPossibleValueId(record.getPossibleValueId());
		model.setId(record.getCode());
		model.setSpecify(record.getSpecify());
		model.setExportLabel(record.getExportLabel());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));

		return model;
	}
}
