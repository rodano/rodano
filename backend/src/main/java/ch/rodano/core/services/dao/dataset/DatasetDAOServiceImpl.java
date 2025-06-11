package ch.rodano.core.services.dao.dataset;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.DatasetAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.DatasetAuditRecord;
import ch.rodano.core.model.jooq.tables.records.DatasetRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.DATASET;

@Service
public class DatasetDAOServiceImpl extends AuditableDAOService<Dataset, DatasetAuditTrail, DatasetRecord, DatasetAuditRecord> implements DatasetDAOService {

	public DatasetDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<DatasetRecord> getTable() {
		return Tables.DATASET;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<DatasetAuditRecord> getAuditTable() {
		return Tables.DATASET_AUDIT;
	}

	@Override
	protected Class<Dataset> getDAOClass() {
		return Dataset.class;
	}

	@Override
	protected Class<DatasetAuditTrail> getEntityAuditClass() {
		return DatasetAuditTrail.class;
	}

	@Override
	public Dataset getDatasetByPk(final Long pk) {
		final var query = create.selectFrom(DATASET).where(DATASET.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<Dataset> getDatasetByPks(final List<Long> pks) {
		final var query = create.selectFrom(DATASET).where(DATASET.PK.in(pks));
		return find(query);
	}

	@Override
	public List<Dataset> getDatasetsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(DATASET).where(DATASET.SCOPE_FK.eq(scopePk).and(DATASET.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public List<Dataset> getDatasetsByScopePkAndDatasetModelIds(final Long scopePk, final Collection<String> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(DATASET.SCOPE_FK.eq(scopePk).and(DATASET.DATASET_MODEL_ID.in(datasetModelIds)).and(DATASET.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByDatasetModelIds(final Collection<String> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(DATASET.DATASET_MODEL_ID.in(datasetModelIds));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(DATASET).where(DATASET.SCOPE_FK.eq(scopePk));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByScopePkAndDatasetModelIds(final Long scopePk, final Collection<String> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(DATASET.SCOPE_FK.eq(scopePk)).and(DATASET.DATASET_MODEL_ID.in(datasetModelIds));
		return find(query);
	}

	@Override
	public List<Dataset> getDatasetsByEventPk(final Long eventPk) {
		final var query = create.selectFrom(DATASET).where(DATASET.EVENT_FK.eq(eventPk).and(DATASET.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByEventPk(final Long eventPk) {
		final var query = create.selectFrom(DATASET).where(DATASET.EVENT_FK.eq(eventPk));
		return find(query);
	}

	@Override
	public List<Dataset> getDatasetsByEventPkAndDatasetModelIds(final Long eventPk, final Collection<String> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(DATASET.EVENT_FK.eq(eventPk).and(DATASET.DATASET_MODEL_ID.in(datasetModelIds)).and(DATASET.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByEventPkAndDatasetModelIds(final Long eventPk, final Collection<String> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(DATASET.EVENT_FK.eq(eventPk).and(DATASET.DATASET_MODEL_ID.in(datasetModelIds)));
		return find(query);
	}

	@Override
	public void deleteDataset(final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		delete(dataset, context, rationale);
	}

	@Override
	public void restoreDataset(final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		restore(dataset, context, rationale);
	}

	@Override
	public void saveDataset(final Dataset dataset, final DatabaseActionContext context, final String rationale) {
		if(dataset.getId() == null) {
			dataset.setId(UUID.randomUUID().toString());
		}
		save(dataset, context, rationale);
	}

}
