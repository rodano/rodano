package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.core.model.jooq.enums.RuleEntityType;
import ch.rodano.core.model.jooq.tables.records.DatasetModelRecord;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;

@Repository
public class DatasetModelDAO implements BaseProjectDAO<DatasetModel> {

	private final DSLContext dslContext;
	private final MappingHelper mappingHelper;
	private final FieldModelDAO fieldModelDAO;
	private final RuleDAO ruleDAO;

	public DatasetModelDAO(final DSLContext dslContext,
						   final MappingHelper mappingHelper,
						   final FieldModelDAO fieldModelDAO,
						   final RuleDAO ruleDAO) {
		this.dslContext = dslContext;
		this.mappingHelper = mappingHelper;
		this.fieldModelDAO = fieldModelDAO;
		this.ruleDAO = ruleDAO;
	}

	@Override
	public List<DatasetModel> findByProject(final UUID projectId) {
		return dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.orderBy(DATASET_MODEL.EXPORT_ORDER)
			.fetch(this::mapToModel);
	}

	@Override
	public DatasetModel findByProjectAndCode(final UUID projectId, final String code) {
		return dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.PROJECT_ID.eq(projectId))
			.and(DATASET_MODEL.CODE.eq(code))
			.fetchOne(this::mapToModel);
	}

	@Override
	public DatasetModel findById(final UUID id) {
		return dslContext.selectFrom(DATASET_MODEL)
			.where(DATASET_MODEL.DATASET_MODEL_ID.eq(id))
			.fetchOne(this::mapToModel);
	}

	@Override
	public DatasetModel save(final DatasetModel entity) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	@Override
	public void delete(final UUID id) {
		throw new UnsupportedOperationException("Not implemented yet - Phase 3");
	}

	private DatasetModel mapToModel(final DatasetModelRecord record) {
		if(record == null) {
			return null;
		}

		final DatasetModel model = new DatasetModel();

		model.setDatasetModelId(record.getDatasetModelId());
		model.setId(record.getCode());

		model.setMultiple(record.getMultiple() != null ? record.getMultiple() : false);
		model.setMaster(record.getMaster() != null ? record.getMaster() : false);
		model.setExportable(record.getExportable() != null ? record.getExportable() : false);
		model.setExportOrder(record.getExportOrder() != null ? record.getExportOrder() : 0);
		model.setFamily(record.getFamily());
		model.setCollapsedLabelPattern(record.getCollapsedLabelPattern());
		model.setExpandedLabelPattern(record.getExpandedLabelPattern());

		model.setShortname(mappingHelper.parseJsonToMap(record.getShortname()));
		model.setLongname(mappingHelper.parseJsonToMap(record.getLongname()));
		model.setDescription(mappingHelper.parseJsonToMap(record.getDescription()));

		model.setFieldModels(fieldModelDAO.findByDatasetModel(record.getDatasetModelId()));

		model.setRestoreRules(ruleDAO.findByEntityAndType(RuleEntityType.DATASET_MODEL, record.getDatasetModelId(), "RESTORE"));
		model.setDeleteRules(ruleDAO.findByEntityAndType(RuleEntityType.DATASET_MODEL, record.getDatasetModelId(), "DELETE"));

		return model;
	}
}
