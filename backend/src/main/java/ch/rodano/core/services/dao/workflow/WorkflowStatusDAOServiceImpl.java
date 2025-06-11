package ch.rodano.core.services.dao.workflow;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.workflow.WorkflowStatusSearch;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.WorkflowStatusAuditTrail;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.WorkflowStatusAuditRecord;
import ch.rodano.core.model.jooq.tables.records.WorkflowStatusRecord;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.DATASET;
import static ch.rodano.core.model.jooq.Tables.EVENT;
import static ch.rodano.core.model.jooq.Tables.FIELD;
import static ch.rodano.core.model.jooq.Tables.FORM;
import static ch.rodano.core.model.jooq.Tables.SCOPE;
import static ch.rodano.core.model.jooq.Tables.SCOPE_ANCESTOR;
import static ch.rodano.core.model.jooq.tables.WorkflowStatus.WORKFLOW_STATUS;

@Service
public class WorkflowStatusDAOServiceImpl extends AuditableDAOService<WorkflowStatus, WorkflowStatusAuditTrail, WorkflowStatusRecord, WorkflowStatusAuditRecord> implements WorkflowStatusDAOService {

	public WorkflowStatusDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<WorkflowStatusRecord> getTable() {
		return Tables.WORKFLOW_STATUS;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<WorkflowStatusAuditRecord> getAuditTable() {
		return Tables.WORKFLOW_STATUS_AUDIT;
	}

	@Override
	protected Class<WorkflowStatusAuditTrail> getEntityAuditClass() {
		return WorkflowStatusAuditTrail.class;
	}

	@Override
	protected Class<WorkflowStatus> getDAOClass() {
		return WorkflowStatus.class;
	}

	@Override
	public WorkflowStatus getWorkflowStatusByPk(final Long pk) {
		final var query = create.selectFrom(WORKFLOW_STATUS).where(WORKFLOW_STATUS.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByScopePk(final Long scopePk) {
		return getWorkflowStatusesByScopePks(Collections.singleton(scopePk));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByScopePk(final Long scopePk, final String workflowId) {
		return getWorkflowStatusesByScopePks(Collections.singleton(scopePk), Optional.of(workflowId));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByScopePks(final Collection<Long> scopePks) {
		return getWorkflowStatusesByScopePks(scopePks, Optional.empty());
	}

	private List<WorkflowStatus> getWorkflowStatusesByScopePks(final Collection<Long> scopePks, final Optional<String> workflowId) {
		if(scopePks.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final var query = create.selectFrom(WORKFLOW_STATUS)
			.where(
				WORKFLOW_STATUS.SCOPE_FK.in(scopePks)
					.and(WORKFLOW_STATUS.EVENT_FK.isNull())
					.and(WORKFLOW_STATUS.FORM_FK.isNull())
					.and(WORKFLOW_STATUS.FIELD_FK.isNull())
					.and(WORKFLOW_STATUS.DELETED.isFalse())
					.and(workflowId.map(w -> WORKFLOW_STATUS.WORKFLOW_ID.eq(w)).orElse(DSL.noCondition()))
			);
		return find(query);
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByEventPk(final Long eventPk) {
		return getWorkflowStatusesByEventPks(Collections.singleton(eventPk));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByEventPk(final Long eventPk, final String workflowId) {
		return getWorkflowStatusesByEventPks(Collections.singleton(eventPk), Optional.of(workflowId));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByEventPks(final Collection<Long> eventPks) {
		return getWorkflowStatusesByEventPks(eventPks, Optional.empty());
	}

	private List<WorkflowStatus> getWorkflowStatusesByEventPks(final Collection<Long> eventPks, final Optional<String> workflowId) {
		if(eventPks.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final var query = create.selectFrom(WORKFLOW_STATUS)
			.where(
				WORKFLOW_STATUS.EVENT_FK.in(eventPks)
					.and(WORKFLOW_STATUS.FORM_FK.isNull())
					.and(WORKFLOW_STATUS.FIELD_FK.isNull())
					.and(WORKFLOW_STATUS.DELETED.isFalse())
					.and(workflowId.map(w -> WORKFLOW_STATUS.WORKFLOW_ID.eq(w)).orElse(DSL.noCondition()))
			);
		return find(query);
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFormPk(final Long formPk) {
		return getWorkflowStatusesByFormPks(Collections.singleton(formPk));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFormPk(final Long formPk, final String workflowId) {
		return getWorkflowStatusesByFormPks(Collections.singleton(formPk), Optional.of(workflowId));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFormPks(final Collection<Long> formPks) {
		return getWorkflowStatusesByFormPks(formPks, Optional.empty());
	}

	private List<WorkflowStatus> getWorkflowStatusesByFormPks(final Collection<Long> formPks, final Optional<String> workflowId) {
		if(formPks.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final var query = create.selectFrom(WORKFLOW_STATUS)
			.where(
				WORKFLOW_STATUS.FORM_FK.in(formPks)
					.and(WORKFLOW_STATUS.DELETED.isFalse())
					.and(workflowId.map(w -> WORKFLOW_STATUS.WORKFLOW_ID.eq(w)).orElse(DSL.noCondition()))
			);
		return find(query);
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFieldPk(final Long fieldPk) {
		return getWorkflowStatusesByFieldPks(Collections.singleton(fieldPk));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFieldPk(final Long fieldPk, final String workflowId) {
		return getWorkflowStatusesByFieldPks(Collections.singleton(fieldPk), Optional.of(workflowId));
	}

	@Override
	public List<WorkflowStatus> getWorkflowStatusesByFieldPks(final Collection<Long> fieldPks) {
		return getWorkflowStatusesByFieldPks(fieldPks, Optional.empty());
	}

	private List<WorkflowStatus> getWorkflowStatusesByFieldPks(final Collection<Long> fieldPks, final Optional<String> workflowId) {
		if(fieldPks.isEmpty()) {
			throw new IllegalArgumentException();
		}
		final var query = create.selectFrom(WORKFLOW_STATUS)
			.where(
				WORKFLOW_STATUS.FIELD_FK.in(fieldPks)
					.and(WORKFLOW_STATUS.DELETED.isFalse())
					.and(workflowId.map(w -> WORKFLOW_STATUS.WORKFLOW_ID.eq(w)).orElse(DSL.noCondition()))
			);
		return find(query);
	}

	@Override
	public void saveWorkflowStatus(final WorkflowStatus ws, final DatabaseActionContext context, final String rationale) {
		save(ws, context, rationale);
	}

	@Override
	public void deleteWorkflowStatus(final WorkflowStatus ws, final DatabaseActionContext context, final String rationale) {
		delete(ws, context, rationale);
	}

	@Override
	public PagedResult<WorkflowStatus> search(final WorkflowStatusSearch search) {
		final var now = ZonedDateTime.now();

		final var query = create.selectDistinct(
			WORKFLOW_STATUS.asterisk(),
			DSL.count().over().as("total")
		)
			.from(WORKFLOW_STATUS)
			.leftJoin(SCOPE_ANCESTOR).on(
				WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE_ANCESTOR.SCOPE_FK)
					.and(SCOPE_ANCESTOR.START_DATE.lessThan(now))
					.and(SCOPE_ANCESTOR.END_DATE.isNull().or(SCOPE_ANCESTOR.END_DATE.greaterThan(now)))
			)
			.leftJoin(SCOPE).on(Tables.WORKFLOW_STATUS.SCOPE_FK.eq(SCOPE.PK))
			.leftJoin(EVENT).on(Tables.WORKFLOW_STATUS.EVENT_FK.eq(EVENT.PK))
			.leftJoin(FORM).on(Tables.WORKFLOW_STATUS.FORM_FK.eq(FORM.PK))
			.leftJoin(FIELD).on(Tables.WORKFLOW_STATUS.FIELD_FK.eq(FIELD.PK))
			.leftJoin(DATASET).on(FIELD.DATASET_FK.eq(DATASET.PK));

		final List<Condition> conditions = new ArrayList<>();

		//do not consider workflow statuses that are deleted or attached to deleted entities
		conditions.add(WORKFLOW_STATUS.DELETED.isFalse());
		conditions.add(SCOPE.DELETED.isNull().or(SCOPE.DELETED.isFalse()));
		conditions.add(EVENT.DELETED.isNull().or(EVENT.DELETED.isFalse()));
		conditions.add(FORM.DELETED.isNull().or(FORM.DELETED.isFalse()));
		conditions.add(DATASET.DELETED.isNull().or(DATASET.DELETED.isFalse()));

		//mandatory conditions
		conditions.add(SCOPE_ANCESTOR.ANCESTOR_FK.in(search.getAncestorScopePks()).or(WORKFLOW_STATUS.SCOPE_FK.in(search.getAncestorScopePks())));
		conditions.add(WORKFLOW_STATUS.WORKFLOW_ID.in(search.getWorkflowIds()));

		search.getStateIds().ifPresent(stateIds -> {
			conditions.add(WORKFLOW_STATUS.STATE_ID.in(stateIds));
		});

		search.getScopePks().ifPresent(scopePks -> {
			conditions.add(WORKFLOW_STATUS.SCOPE_FK.in(scopePks));
		});

		search.getEventPks().ifPresent(eventPks -> {
			conditions.add(EVENT.PK.in(eventPks));
		});

		search.getFilterExpectedEvents().ifPresent(_ -> {
			conditions.add(EVENT.PK.isNull().or(EVENT.DATE.isNotNull()));
		});

		search.getFullText().ifPresent(_ -> {
			conditions.add(WORKFLOW_STATUS.TRIGGER_MESSAGE.containsIgnoreCase(search.getFullText().get()));
		});

		query
			.where(conditions)
			.groupBy(WORKFLOW_STATUS.PK)
			.orderBy(search.getSortBy().getField().sort(search.getOrder()))
			.limit(search.getLimitField())
			.offset(search.getOffsetField());

		final var result = query.fetch();
		var total = 0;
		if(result.size() > 0) {
			total = result.get(0).getValue("total", Integer.class);
		}

		final var workflowStatuses = result.into(WorkflowStatus.class);
		workflowStatuses.forEach(s -> s.onPostLoad(studyService.getStudy()));
		return new PagedResult<>(workflowStatuses, search.getPageSize(), search.getPageIndex(), total);
	}
}
