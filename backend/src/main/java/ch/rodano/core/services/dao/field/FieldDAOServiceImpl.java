package ch.rodano.core.services.dao.field;

import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.scope.ScopeModel;

import ch.rodano.core.model.dataset.Dataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
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
import java.util.stream.Collectors;

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
	public List<Field> getFieldsByDatasetPkHavingFieldModelIds(final Long datasetPk, final Collection<String> fieldModelIds) {
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
			.where(FIELD.dataset().SCOPE_FK.eq(scopePk));
		return find(query);
	}

	@Override
	public List<Field> getFieldsFromScopeWithAValue(final Long scopePk) {
		final var query = create.selectFrom(FIELD)
			.where(FIELD.dataset().SCOPE_FK.eq(scopePk).and(FIELD.VALUE.isNotNull()));
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
	public List<Field> getFieldsRelatedToEvent(final Long scopePk, final Optional<Long> eventPk) {
		final var conditions = new ArrayList<Condition>();
		conditions.add(FIELD.dataset().SCOPE_FK.eq(scopePk));
		eventPk.ifPresent(p -> conditions.add(FIELD.dataset().EVENT_FK.eq(p)));
		final var query = create.selectFrom(FIELD)
			.where(DSL.condition(Operator.OR, conditions));
		return find(query);
	}

	@Override
	public List<Field> getSearchableFields(final Collection<Long> scopePk, final Collection<String> fieldModelIds) {
		final var query = create.selectFrom(FIELD).where(FIELD.FIELD_MODEL_ID.in(fieldModelIds).and(FIELD.dataset().SCOPE_FK.in(scopePk)));
		return find(query);
	}

	@Override
	public Map<Long, List<Dataset>> getSearchableFieldsOnScope(ScopeModel scopeModel) {
		final var dsOnScope = studyService.getStudy().getScopeModel(scopeModel.getId()).getDatasetModels().stream().toList();

		// Map of dataset model IDs and searchable field model IDs
		final Map<String, String> searchableFields = dsOnScope.stream()
			.flatMap(dm -> dm.getFieldModels().stream()
				.filter(FieldModel::isSearchable)
				.map(fm -> Map.entry(dm.getId(), fm.getId())))
			.collect(java.util.stream.Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(a, b) -> a
			));

		final SelectConditionStep<Record> query = create.select().from(FIELD)
			.join(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK))
			.where(DATASET.DATASET_MODEL_ID.in(searchableFields.keySet()))
			.and(FIELD.FIELD_MODEL_ID.in(searchableFields.values()));

		// Fetch the fields and map to Field entities
		final var result = create.fetch(query);

		return result.stream()
			.collect(Collectors.groupingBy(
				r -> r.get(DATASET.SCOPE_FK),
				Collectors.mapping(r -> r.into(Dataset.class), Collectors.toList())
			));
	}



}
