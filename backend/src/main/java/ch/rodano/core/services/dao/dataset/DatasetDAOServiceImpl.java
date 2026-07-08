package ch.rodano.core.services.dao.dataset;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
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
	public List<Dataset> getDatasetsByPks(final Collection<Long> pks) {
		if(pks.isEmpty()) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(DATASET).where(DATASET.PK.in(pks));
		return find(query);
	}

	@Override
	public List<Dataset> getAllDatasetsByDatasetModelIds(final Collection<String> datasetModelIds) {
		if(datasetModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(DATASET).where(DATASET.DATASET_MODEL_ID.in(datasetModelIds));
		return find(query);
	}

	private List<Dataset> search(final Optional<Long> scopePk, final Optional<Long> eventPk, final boolean includeDeleted, final Optional<Collection<String>> datasetModelIds) {
		final var query = create.selectFrom(DATASET).where(
			scopePk.map(DATASET.SCOPE_FK::eq).orElse(DSL.noCondition())
				.and(eventPk.map(DATASET.EVENT_FK::eq).orElse(DATASET.EVENT_FK.isNull()))
				.and(includeDeleted ? DSL.noCondition() : DATASET.DELETED.isFalse())
				.and(datasetModelIds.map(DATASET.DATASET_MODEL_ID::in).orElse(DSL.noCondition()))
		);
		return find(query);
	}

	@Override
	public List<Dataset> search(final Long scopePk, final Optional<Long> eventPk, final boolean includeDeleted, final Optional<Collection<String>> datasetModelIds) {
		return search(Optional.of(scopePk), eventPk, includeDeleted, datasetModelIds);
	}

	//scope
	@Override
	public List<Dataset> getDatasetsByScopePk(final Long scopePk) {
		return search(Optional.of(scopePk), Optional.empty(), false, Optional.empty());
	}

	@Override
	public List<Dataset> getDatasetsByScopePkAndDatasetModelIds(final Long scopePk, final Collection<String> datasetModelIds) {
		if(datasetModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		return search(Optional.of(scopePk), Optional.empty(), false, Optional.of(datasetModelIds));
	}

	@Override
	public List<Dataset> getAllDatasetsByScopePk(final Long scopePk) {
		return search(Optional.of(scopePk), Optional.empty(), true, Optional.empty());
	}

	@Override
	public List<Dataset> getAllDatasetsByScopePkAndDatasetModelIds(final Long scopePk, final Collection<String> datasetModelIds) {
		if(datasetModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		return search(Optional.of(scopePk), Optional.empty(), true, Optional.of(datasetModelIds));
	}

	//event
	@Override
	public List<Dataset> getDatasetsByEventPk(final Long eventPk) {
		return search(Optional.empty(), Optional.of(eventPk), false, Optional.empty());
	}

	@Override
	public List<Dataset> getDatasetsByEventPkAndDatasetModelIds(final Long eventPk, final Collection<String> datasetModelIds) {
		if(datasetModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		return search(Optional.empty(), Optional.of(eventPk), false, Optional.of(datasetModelIds));
	}

	@Override
	public List<Dataset> getAllDatasetsByEventPk(final Long eventPk) {
		return search(Optional.empty(), Optional.of(eventPk), true, Optional.empty());
	}

	@Override
	public List<Dataset> getAllDatasetsByEventPkAndDatasetModelIds(final Long eventPk, final Collection<String> datasetModelIds) {
		if(datasetModelIds.isEmpty()) {
			return Collections.emptyList();
		}
		return search(Optional.empty(), Optional.of(eventPk), true, Optional.of(datasetModelIds));
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
