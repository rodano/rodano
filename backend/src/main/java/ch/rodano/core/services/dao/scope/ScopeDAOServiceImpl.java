package ch.rodano.core.services.dao.scope;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.ScopeAuditTrail;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.ScopeAuditRecord;
import ch.rodano.core.model.jooq.tables.records.ScopeRecord;
import ch.rodano.core.model.jooqutils.JOOQTranslator;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;
import static ch.rodano.core.model.jooq.Tables.WORKFLOW_STATUS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Service
public class ScopeDAOServiceImpl extends AuditableDAOService<Scope, ScopeAuditTrail, ScopeRecord, ScopeAuditRecord> implements ScopeDAOService {

	public ScopeDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<ScopeRecord> getTable() {
		return Tables.SCOPE;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<ScopeAuditRecord> getAuditTable() {
		return Tables.SCOPE_AUDIT;
	}

	@Override
	protected Class<ScopeAuditTrail> getEntityAuditClass() {
		return ScopeAuditTrail.class;
	}

	@Override
	protected Class<Scope> getDAOClass() {
		return Scope.class;
	}

	@Override
	public List<Scope> getAllScopes() {
		final var query = create.selectFrom(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getAllScopesByScopeModelId(final String scopeModelId) {
		final var query = create.selectFrom(SCOPE).where(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId));
		return find(query);
	}

	@Override
	public List<Scope> getVirtualScopes() {
		final var query = create.selectFrom(SCOPE).where(SCOPE.VIRTUAL.isTrue().and(SCOPE.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public Scope getRootScope() {
		final var query = create.selectFrom(SCOPE)
			.where(DSL.notExists(DSL.selectFrom(SCOPE_RELATION).where(SCOPE_RELATION.SCOPE_FK.eq(SCOPE.PK))));
		return findUnique(query);
	}

	@Override
	public List<Scope> getScopesByScopeModelId(final String scopeModelId) {
		final var query = create.selectFrom(SCOPE).where(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId).and(SCOPE.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public Integer getScopesByScopeModelIdCount(final String scopeModelId) {
		return create.select(DSL.count(SCOPE.PK))
			.from(SCOPE)
			.where(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId).and(SCOPE.DELETED.isFalse()))
			.fetchSingle()
			.value1();
	}

	@Override
	public List<Scope> getScopesByScopeModelIdHavingAncestor(final Collection<String> scopeModelIds, final Collection<Long> ancestorPks) {
		if(scopeModelIds.isEmpty() || ancestorPks.isEmpty()) {
			return Collections.emptyList();
		}
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.leftJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE.PK.in(ancestorPks).or(
					SCOPE_ANCESTOR.ANCESTOR_FK.in(ancestorPks)
						.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
						.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
				)
					.and(SCOPE.SCOPE_MODEL_ID.in(scopeModelIds))
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getDescendants(final Long scopePk) {
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getDescendants(final Long scopePk, final String scopeModelId) {
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId))
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getAllEnabledDescendants(final Long scopePk) {
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
					.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))
					//TODO the prefix "All" in the method name is usually used to say that we consider all objects, even the deleted ones
					//this condition should be removed
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getAllEnabledDescendants(final Long scopePk, final String scopeModelId) {
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
					.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))
					.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId))
					//TODO the prefix "All" in the method name is usually used to say that we consider all objects, even the deleted ones
					//this condition should be removed
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getEnabledDescendants(final Long scopePk) {
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getEnabledDescendants(final Long scopePk, final String scopeModelId) {
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
					.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId))
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public Integer getEnabledDescendantsByScopeModelIdCount(final String scopeModelId, final Long scopePk) {
		return getEnabledDescendantsByScopeModelIdCount(scopeModelId, Collections.singleton(scopePk)).get(scopePk);
	}

	@Override
	public Map<Long, Integer> getEnabledDescendantsByScopeModelIdCount(final String scopeModelId, final Collection<Long> scopePks) {
		final var now = ZonedDateTime.now();
		final var count = DSL.countDistinct(SCOPE.PK).as("count");
		final var values = create.select(SCOPE_ANCESTOR.ANCESTOR_FK, count)
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.SCOPE_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.in(scopePks)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
					.and(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId))
					.and(SCOPE.DELETED.isFalse())
			)
			.groupBy(SCOPE_ANCESTOR.ANCESTOR_FK)
			.fetchMap(SCOPE_ANCESTOR.ANCESTOR_FK, count);
		//the query above won't return any value for deleted scopes or scopes without descendants
		final var defaultValues = scopePks.stream().collect(Collectors.toMap(Function.identity(), _ -> 0));
		defaultValues.putAll(values);
		return defaultValues;
	}

	@Override
	public List<Scope> getAncestors(final Long scopePk) {
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.ANCESTOR_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getEnabledAncestors(final Long scopePk) {
		final var now = ZonedDateTime.now();
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.ANCESTOR_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public List<Scope> getDefaultAncestors(final Long scopePk) {
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.ANCESTOR_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.DEFAULT.isTrue())
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public boolean hasDeletedDefaultAncestor(final Long scopePk) {
		return create.select(DSL.count(SCOPE.PK))
			.from(SCOPE_ANCESTOR)
			.where(
				SCOPE_ANCESTOR.ANCESTOR_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isTrue())
					.and(SCOPE_ANCESTOR.DEFAULT.isTrue())
			)
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public List<Scope> getVirtualAncestors(final Long scopePk) {
		final var query = create.selectDistinct(SCOPE.asterisk())
			.from(SCOPE)
			.innerJoin(SCOPE_ANCESTOR).on(SCOPE_ANCESTOR.ANCESTOR_FK.eq(SCOPE.PK))
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue())
					.and(SCOPE.DELETED.isFalse())
			)
			.coerce(SCOPE);
		return find(query);
	}

	@Override
	public boolean isDescendantOfEnabled(final Long scopePk, final Long potentialAncestorPk) {
		final var now = ZonedDateTime.now();
		return create.select(DSL.count(SCOPE_ANCESTOR.SCOPE_FK))
			.from(SCOPE_ANCESTOR)
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.eq(potentialAncestorPk))
					.and(SCOPE_ANCESTOR.VIRTUAL.isTrue().or(SCOPE_ANCESTOR.START_DATE.lessThan(now).and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))))
					.and(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse())
			)
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public boolean isDescendantOf(final Long scopePk, final Long potentialAncestorPk) {
		return create.select(DSL.count(SCOPE_ANCESTOR.SCOPE_FK))
			.from(SCOPE_ANCESTOR)
			.where(
				SCOPE_ANCESTOR.SCOPE_FK.eq(scopePk)
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.eq(potentialAncestorPk))
			)
			.fetchSingle()
			.value1() > 0;
	}

	@Override
	public Scope getScopeByPk(final Long pk) {
		final var query = create.selectFrom(SCOPE).where(SCOPE.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<Scope> getScopesByPks(final Collection<Long> pks) {
		if(CollectionUtils.isEmpty(pks)) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(SCOPE).where(SCOPE.PK.in(pks));
		return find(query);
	}

	@Override
	public Scope getScopeByCode(final String code) {
		final var query = create.selectFrom(SCOPE).where(SCOPE.CODE.eq(code));
		return findUnique(query);
	}

	@Override
	public List<Scope> getScopesByCodes(final Collection<String> codes) {
		if(CollectionUtils.isEmpty(codes)) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(SCOPE).where(SCOPE.CODE.in(codes));
		return find(query);
	}

	@Override
	public Scope getScopeById(final String id) {
		final var query = create.selectFrom(SCOPE).where(SCOPE.ID.eq(id));
		return findUnique(query);
	}

	@Override
	public List<Scope> getScopesByIds(final Collection<String> ids) {
		if(CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		final var query = create.selectFrom(SCOPE).where(SCOPE.ID.in(ids));
		return find(query);
	}

	@Override
	public void saveScope(final Scope scope, final DatabaseActionContext context, final String rationale) {
		if(scope.getId() == null) {
			scope.setId(UUID.randomUUID().toString());
		}
		save(scope, context, rationale);
	}

	@Override
	public void deleteScope(final Scope scope, final DatabaseActionContext context, final String rationale) {
		delete(scope, context, rationale);
	}

	@Override
	public void restoreScope(final Scope scope, final DatabaseActionContext context, final String rationale) {
		restore(scope, context, rationale);
	}

	@Override
	public PagedResult<Scope> search(final ScopeSearch search) {
		final var now = ZonedDateTime.now();

		final List<Condition> conditions = new ArrayList<>();

		final var query = create.select(SCOPE.asterisk(), DSL.count().over().as("total"))
			.from(SCOPE)
			//do no inner join to be able to search for the root scope
			.leftJoin(SCOPE_ANCESTOR).on(SCOPE.PK.eq(SCOPE_ANCESTOR.SCOPE_FK));

		// join tables if necessary
		search.getParentPks().ifPresent(parentPks -> {
			// perform an inner join because the goal is to filter scopes
			query.innerJoin(SCOPE_RELATION)
				.on(
					SCOPE.PK.eq(SCOPE_RELATION.SCOPE_FK)
						.and(SCOPE_RELATION.PARENT_FK.in(parentPks))
						.and(SCOPE_RELATION.END_DATE.isNull().or(SCOPE_RELATION.END_DATE.ge(now)))
				);
		});

		search.getAncestorPks().ifPresent(ancestorPks -> {
			final var aliasedTable = SCOPE_ANCESTOR.as("SA");
			query.innerJoin(aliasedTable).on(SCOPE.PK.eq(aliasedTable.SCOPE_FK).and(aliasedTable.ANCESTOR_FK.in(ancestorPks)));
			if(!search.getIncludeDeleted()) {
				conditions.add(aliasedTable.ANCESTOR_DELETED.isFalse());
			}
		});

		if(search.getWorkflowStates().isPresent()) {
			//one join per workflow filter is required
			for(final var entry : search.getWorkflowStates().get().entrySet()) {
				final var workflowJoin = WORKFLOW_STATUS.as(entry.getKey());
				//perform an inner join to only include scopes that have matching workflow status rows
				query.innerJoin(workflowJoin).on(
					SCOPE.PK.eq(workflowJoin.SCOPE_FK)
						.and(workflowJoin.WORKFLOW_ID.eq(entry.getKey()))
						.and(workflowJoin.DELETED.isFalse())
						.and(workflowJoin.STATE_ID.in(entry.getValue()))
				);
			}
		}

		search.getFieldModelCriteria().ifPresent(criteria -> {
			final var criteriaByDocument = criteria.stream()
				.collect(Collectors.groupingBy(FieldModelCriterion::datasetModelId));

			for(final var documentCriteria : criteriaByDocument.entrySet()) {
				final var datasetModelId = documentCriteria.getKey();
				final var datasetModel = studyService.getStudy().getDatasetModel(datasetModelId);

				final var exportScopeFkColumnName = String.format("%s.scope_fk", datasetModel.getExportTableName());
				var criteriaCondition = DSL.noCondition();

				for(final var criterion : documentCriteria.getValue()) {
					final var fieldModel = datasetModel.getFieldModel(criterion.fieldModelId());
					final var operator = criterion.operator();
					final var criterionValue = criterion.value();

					final var operatedFieldName = String.format("%s.%s", datasetModel.getExportTableName(), fieldModel.getId());
					final var operatedField = field(operatedFieldName);
					final var fieldCondition = JOOQTranslator.translate(operator, fieldModel, operatedField, criterionValue);

					criteriaCondition = criteriaCondition.and(fieldCondition);
				}

				query.innerJoin(table(datasetModel.getExportTableName())).on(
					SCOPE.PK
						.eq(field(exportScopeFkColumnName).cast(Long.class))
						.and(criteriaCondition)
				);
			}
		});

		//restrict search according to rights and manage the scope models
		search.getScopeModelId().ifPresent(scopeModelId -> {
			conditions.add(SCOPE.SCOPE_MODEL_ID.eq(scopeModelId));
		});

		search.getScopeModelAncestorPks().ifPresent(ancestorPks -> {
			if(ancestorPks.size() > 0) {
				final List<Condition> scopeConditions = new ArrayList<>();
				for(final var entry : ancestorPks.entrySet()) {
					scopeConditions.add(
						SCOPE.PK.in(entry.getValue())
							.or(SCOPE_ANCESTOR.ANCESTOR_FK.in(entry.getValue()))
							.and(SCOPE.SCOPE_MODEL_ID.eq(entry.getKey()))
					);
				}
				conditions.add(DSL.or(scopeConditions));
			}
		});

		if(!search.getIncludeDeleted()) {
			// if we exclude the deleted scopes, then we also exclude the scopes that descend from a deleted scope
			conditions.add(
				SCOPE.DELETED.isFalse()
					//scope ancestor table is null for the root scope
					.and(SCOPE_ANCESTOR.ANCESTOR_FK.isNull().or(SCOPE_ANCESTOR.ANCESTOR_DELETED.isFalse()))
			);
		}

		search.getPks().ifPresent(pks -> {
			conditions.add(SCOPE.PK.in(pks));
		});

		search.getIds().ifPresent(ids -> {
			conditions.add(SCOPE.ID.in(ids));
		});

		search.getCode().ifPresent(code -> {
			conditions.add(SCOPE.CODE.containsIgnoreCase(code));
		});

		search.getLeaf().ifPresent(leaf -> {
			final var leafId = studyService.getStudy().getLeafScopeModel().getId();
			if(leaf) {
				conditions.add(SCOPE.SCOPE_MODEL_ID.eq(leafId));
			}
			else {
				conditions.add(SCOPE.SCOPE_MODEL_ID.notEqual(leafId));
			}
		});

		search.getFullText().ifPresent(fullText -> {
			conditions.add(
				SCOPE.CODE.containsIgnoreCase(fullText)
					.or(SCOPE.SHORTNAME.containsIgnoreCase(fullText))
					.or(SCOPE.LONGNAME.containsIgnoreCase(fullText))
			);
		});

		// group results by unique scope pk to avoid duplicates
		query
			.where(conditions)
			.groupBy(SCOPE.PK)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var scopes = result.into(Scope.class);
		scopes.forEach(s -> s.onPostLoad(studyService.getStudy()));
		return new PagedResult<>(scopes, search.getPageSize(), search.getPageIndex(), total);
	}
}
