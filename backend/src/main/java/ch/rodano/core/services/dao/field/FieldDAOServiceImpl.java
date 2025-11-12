package ch.rodano.core.services.dao.field;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.FieldAuditRecord;
import ch.rodano.core.model.jooq.tables.records.FieldRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.tables.Dataset.DATASET;
import static ch.rodano.core.model.jooq.tables.Field.FIELD;

@Service
public class FieldDAOServiceImpl extends AuditableDAOService<Field, FieldAuditTrail, FieldRecord, FieldAuditRecord> implements FieldDAOService {

	public FieldDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	private UUID tenant() {
		return studyService.getStudy().getProjectId();
	}

	@Override
	protected Table<FieldRecord> getTable() {
		return Tables.FIELD;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<FieldAuditRecord> getAuditTable() {
		return Tables.FIELD_AUDIT;
	}

	@Override
	protected Class<FieldAuditTrail> getEntityAuditClass() {
		return FieldAuditTrail.class;
	}

	@Override
	protected Class<Field> getDAOClass() {
		return Field.class;
	}

	@Override
	public Field getFieldByPk(final Long pk) {
		final var query = create.selectFrom(FIELD).where(FIELD.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public void saveField(final Field field, final DatabaseActionContext context, final String rationale) {
		if(field.getProjectId() == null) {
			field.setProjectId(studyService.getStudy().getProjectId());
		}
		save(field, context, rationale);
	}

	@Override
	public List<Field> getFieldsByDatasetPkHavingFieldModelIds(final Long datasetPk, final Collection<UUID> fieldModelIds) {
		final var query = create.selectFrom(FIELD).where(FIELD.DATASET_FK.eq(datasetPk).and(FIELD.FIELD_MODEL_ID.in(fieldModelIds)));
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromDatasetWithAValue(final Long datasetPk) {
		final var query = create.selectFrom(FIELD).where(FIELD.DATASET_FK.eq(datasetPk).and(FIELD.VALUE.isNotNull()));
		return find(query);
	}

	@Override
	public List<Field> getFieldsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant()))
			.andExists(
				create.selectOne()
					.from(DATASET)
					.where(DATASET.PK.eq(FIELD.DATASET_FK)
						.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
						.and(DATASET.SCOPE_FK.eq(scopePk)))
			);
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromScopeWithAValue(final Long scopePk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant()))
			.and(FIELD.VALUE.isNotNull())
			.andExists(
				create.selectOne()
					.from(DATASET)
					.where(DATASET.PK.eq(FIELD.DATASET_FK)
						.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
						.and(DATASET.SCOPE_FK.eq(scopePk)))
			);
		return find(query);
	}

	@Override
	public boolean doesScopeHaveFieldsWithAValue(final Long scopePk) {
		return create.select(DSL.countDistinct(FIELD.PK))
			.from(FIELD)
			.innerJoin(DATASET).on(DATASET.PK.eq(FIELD.DATASET_FK))
			.where(DATASET.SCOPE_FK.eq(scopePk).and(FIELD.VALUE.isNotNull()))
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public List<Field> getFieldsByEventPk(final Long eventPk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant()))
			.andExists(
				create.selectOne()
					.from(DATASET)
					.where(DATASET.PK.eq(FIELD.DATASET_FK)
						.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
						.and(DATASET.EVENT_FK.eq(eventPk)))
			);
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromEventWithAValue(final Long eventPk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant()))
			.and(FIELD.VALUE.isNotNull())
			.andExists(
				create.selectOne()
					.from(DATASET)
					.where(DATASET.PK.eq(FIELD.DATASET_FK)
						.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
						.and(DATASET.EVENT_FK.eq(eventPk)))
			);
		return find(query);
	}

	@Override
	public boolean doesEventHaveFieldsWithAValue(final Long eventPk) {
		return create.select(DSL.countDistinct(FIELD.PK))
			.from(FIELD)
			.innerJoin(DATASET).on(DATASET.PK.eq(FIELD.DATASET_FK))
			.where(DATASET.EVENT_FK.eq(eventPk).and(FIELD.VALUE.isNotNull()))
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public List<Field> getFieldsRelatedToEvent(final Long scopePk, final Optional<Long> eventPk) {
		var condition = DATASET.PK.eq(FIELD.DATASET_FK)
			.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
			.and(DATASET.SCOPE_FK.eq(scopePk));

		if(eventPk.isPresent()) {
			condition = condition.and(DATASET.EVENT_FK.eq(eventPk.get()));
		}

		final var exists = create.selectOne()
			.from(DATASET)
			.where(condition);

		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant())).andExists(exists);
		return find(query);
	}

	public List<Field> getSearchableFields(final Collection<Long> scopePk, final Collection<String> fieldModelIds) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.PROJECT_ID.eq(tenant()))
			.and(FIELD.FIELD_MODEL_ID.in(fieldModelIds))
			.andExists(
				create.selectOne()
					.from(DATASET)
					.where(DATASET.PK.eq(FIELD.DATASET_FK)
						.and(DATASET.PROJECT_ID.eq(FIELD.PROJECT_ID))
						.and(DATASET.SCOPE_FK.in(scopePk)))
			);
		return find(query);
	}

}
