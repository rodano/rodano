package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.layout.Layout;
import ch.rodano.configuration.model.layout.LayoutType;
import ch.rodano.core.model.jooq.enums.RuleConstraintOwnerType;
import ch.rodano.core.model.jooq.tables.records.FormLayoutRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FormLayout.FORM_LAYOUT;

@Repository
public class LayoutDAO {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final LineDAO lineDAO;
	private final ColumnHeaderDAO columnHeaderDAO;
	private final RuleDAO ruleDAO;

	public LayoutDAO(final DSLContext dslContext,
					 final MappingHelper mappingHelper,
					 final LineDAO lineDAO,
					 final ColumnHeaderDAO columnHeaderDAO,
					 final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.lineDAO = lineDAO;
		this.columnHeaderDAO = columnHeaderDAO;
		this.ruleDAO = ruleDAO;
	}

	public List<Layout> findByFormModel(final UUID formModelId) {
		return dslContext.selectFrom(FORM_LAYOUT)
			.where(FORM_LAYOUT.FORM_MODEL_ID.eq(formModelId))
			.fetch(this::mapToModel);
	}

	private Layout mapToModel(final FormLayoutRecord record) {
		if(record == null) {
			return null;
		}

		final Layout model = new Layout();

		model.setId(record.getCode());
		model.setLayoutId(record.getFormLayoutId());
		model.setCssCode(record.getCssCode());

		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));
		model.setTextBefore(mappingHelper.parseJsonToMap(record.getTextBefore()));
		model.setTextAfter(mappingHelper.parseJsonToMap(record.getTextAfter()));

		model.setType(mappingHelper.parseEnum(LayoutType.class, record.getType(), "type"));

		if(record.getDatasetModelId() != null) {
			model.setDatasetModelId(getDatasetModelCode(record.getDatasetModelId()));
		}

		if(record.getDefaultSortFieldModelId() != null) {
			model.setDefaultSortFieldModelId(getFieldModelCode(record.getDefaultSortFieldModelId()));
		}

		model.setColumns(columnHeaderDAO.findByLayout(record.getFormLayoutId()));

		final var lines = lineDAO.findByLayout(record.getFormLayoutId());

		for(final var line : lines) {
			line.setLayout(model);
		}

		model.setLines(lines);

		model.setConstraint(ruleDAO.loadConstraintForOwner(RuleConstraintOwnerType.FORM_LAYOUT, record.getFormLayoutId()));

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
