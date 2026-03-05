package ch.rodano.core.services.dao.field;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.FieldAuditRecord;
import ch.rodano.core.model.jooq.tables.records.FieldRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.FIELD;

@Service
public class FieldDAOServiceImpl extends AuditableDAOService<Field, FieldAuditTrail, FieldRecord, FieldAuditRecord> implements FieldDAOService {

	public FieldDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
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
		save(field, context, rationale);
	}

	@Override
	public List<Field> getFieldsByDatasetPks(final Collection<Long> datasetPks) {
		if(datasetPks.isEmpty()) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(FIELD).where(FIELD.DATASET_FK.in(datasetPks));
		return find(query);
	}

	@Override
	public List<Field> getFieldsByDatasetPkHavingFieldModelIds(final Long datasetPk, final Collection<String> fieldModelIds) {
		if(fieldModelIds.isEmpty()) {
			return Collections.emptyList();
		}
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
			.where(FIELD.dataset().SCOPE_FK.eq(scopePk).and(FIELD.dataset().EVENT_FK.isNull()));
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromScopeWithAValue(final Long scopePk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.dataset().SCOPE_FK.eq(scopePk).and(FIELD.dataset().EVENT_FK.isNull()).and(FIELD.VALUE.isNotNull()));
		return find(query);
	}

	@Override
	public boolean doesScopeHaveFieldsWithAValue(final Long scopePk) {
		return create.select(DSL.countDistinct(FIELD.PK))
			.from(FIELD)
			.where(FIELD.dataset().SCOPE_FK.eq(scopePk).and(FIELD.dataset().EVENT_FK.isNull()).and(FIELD.VALUE.isNotNull()))
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public List<Field> getFieldsByEventPk(final Long eventPk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.dataset().EVENT_FK.eq(eventPk));
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromEventWithAValue(final Long eventPk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.dataset().EVENT_FK.eq(eventPk).and(FIELD.VALUE.isNotNull()));
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

	@Override
	public Map<Long, List<Field>> getSearchableFieldsOnScope(final ScopeModel scopeModel) {
		final var dsOnScope = studyService.getStudy().getScopeModel(scopeModel.getId()).getDatasetModels().stream().toList();

		// Keep all searchable field IDs for each dataset model ID.
		final Map<String, List<String>> searchableFieldsByDataset = dsOnScope.stream()
			.flatMap(dm -> dm.getFieldModels().stream()
				.filter(FieldModel::isSearchable)
				.map(fm -> Map.entry(dm.getId(), fm.getId())))
			.collect(Collectors.groupingBy(
				Map.Entry::getKey,
				Collectors.mapping(Map.Entry::getValue, Collectors.toList())
			));

		if(searchableFieldsByDataset.isEmpty()) {
			return Map.of();
		}

		final var pairedConditions = searchableFieldsByDataset.entrySet().stream()
			.map(entry -> DATASET.DATASET_MODEL_ID.eq(entry.getKey()).and(FIELD.FIELD_MODEL_ID.in(entry.getValue())))
			.toList();

		final SelectConditionStep<Record> query = create.select().from(FIELD)
			.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.where(DSL.or(pairedConditions));

		return create.fetch(query).stream()
			.collect(Collectors.groupingBy(
				r -> r.get(DATASET.SCOPE_FK),
				Collectors.mapping(r -> r.into(FIELD).into(Field.class), Collectors.toList())
			));
	}


}
