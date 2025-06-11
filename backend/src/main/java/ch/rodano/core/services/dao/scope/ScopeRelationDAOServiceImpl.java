package ch.rodano.core.services.dao.scope;

import java.time.ZonedDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.ScopeRelationRecord;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AbstractDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.SCOPE_RELATION;

@Service
public class ScopeRelationDAOServiceImpl extends AbstractDAOService<ScopeRelation, ScopeRelationRecord> implements ScopeRelationDAOService {

	public ScopeRelationDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<ScopeRelationRecord> getTable() {
		return Tables.SCOPE_RELATION;
	}

	@Override
	protected Class<ScopeRelation> getDAOClass() {
		return ScopeRelation.class;
	}

	@Override
	public ScopeRelation getScopeRelationByPk(final Long pk) {
		final var query = create.selectFrom(SCOPE_RELATION).where(SCOPE_RELATION.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public ScopeRelation getActiveScopeRelation(final Long scopePk, final Long parentPk) {
		final var now = ZonedDateTime.now();
		final var query = create.selectFrom(SCOPE_RELATION)
			.where(
				SCOPE_RELATION.SCOPE_FK.eq(scopePk)
					.and(SCOPE_RELATION.PARENT_FK.eq(parentPk))
					.and(SCOPE_RELATION.START_DATE.lessThan(now))
					.and(SCOPE_RELATION.END_DATE.isNull().or(SCOPE_RELATION.END_DATE.greaterThan(now)))
			);
		return findUnique(query);
	}

	@Override
	public List<ScopeRelation> getParentRelations(final Long scopePk) {
		final var query = create.selectFrom(SCOPE_RELATION).where(SCOPE_RELATION.SCOPE_FK.eq(scopePk));
		return find(query);
	}

	@Override
	public List<ScopeRelation> getChildrenRelations(final Long parentPk) {
		final var query = create.selectFrom(SCOPE_RELATION).where(SCOPE_RELATION.PARENT_FK.eq(parentPk));
		return find(query);
	}

	@Override
	public void saveScopeRelation(final ScopeRelation relation, final DatabaseActionContext context, final String rationale) {
		save(relation, context, rationale);
	}

	@Override
	public void deleteScopeRelation(final ScopeRelation relation) {
		delete(relation);
	}

	@Override
	public List<ScopeRelation> getScopeRelations(final Long scopePk) {
		final var query = create.selectFrom(SCOPE_RELATION).where(SCOPE_RELATION.SCOPE_FK.eq(scopePk).or(SCOPE_RELATION.PARENT_FK.eq(scopePk)));
		return find(query);
	}
}
