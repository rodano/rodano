package ch.rodano.core.services.dao.event;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.EventAuditTrail;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.jooq.Tables;
import ch.rodano.core.model.jooq.tables.records.EventAuditRecord;
import ch.rodano.core.model.jooq.tables.records.EventRecord;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.commons.AuditableDAOService;
import ch.rodano.core.services.dao.strategy.DAOStrategy;

import static ch.rodano.core.model.jooq.Tables.EVENT;

@Service
public class EventDAOServiceImpl extends AuditableDAOService<Event, EventAuditTrail, EventRecord, EventAuditRecord> implements EventDAOService {

	public EventDAOServiceImpl(
		final DSLContext create,
		final DAOStrategy strategy,
		final StudyService studyService
	) {
		super(create, strategy, studyService);
	}

	@Override
	protected Table<EventRecord> getTable() {
		return Tables.EVENT;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Table<EventAuditRecord> getAuditTable() {
		return Tables.EVENT_AUDIT;
	}

	@Override
	protected Class<EventAuditTrail> getEntityAuditClass() {
		return EventAuditTrail.class;
	}

	@Override
	protected Class<Event> getDAOClass() {
		return Event.class;
	}

	@Override
	public Event getEventByPk(final Long pk) {
		final var query = create.selectFrom(EVENT).where(EVENT.PK.eq(pk));
		return findUnique(query);
	}

	@Override
	public Event getEventById(final String id) {
		final var query = create.selectFrom(EVENT).where(EVENT.ID.eq(id));
		return findUnique(query);
	}

	@Override
	public void saveEvent(final Event event, final DatabaseActionContext context, final String rationale) {
		if(event.getId() == null) {
			event.setId(UUID.randomUUID().toString());
		}
		save(event, context, rationale);
	}

	@Override
	public void deleteEvent(final Event event, final DatabaseActionContext context, final String rationale) {
		delete(event, context, rationale);
	}

	@Override
	public void restoreEvent(final Event event, final DatabaseActionContext context, final String rationale) {
		restore(event, context, rationale);
	}

	@Override
	public List<Event> getEventsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(EVENT).where(EVENT.SCOPE_FK.eq(scopePk).and(EVENT.DELETED.isFalse()));
		return find(query);
	}

	@Override
	public List<Event> getAllEventsByEventModelId(final String eventModelId) {
		final var query = create.selectFrom(EVENT).where(EVENT.EVENT_MODEL_ID.eq(eventModelId));
		return find(query);
	}

	@Override
	public List<Event> getEventsByScopePkAndEventModelId(final Long scopePk, final String eventModelId) {
		final var query = create.selectFrom(EVENT).where(EVENT.SCOPE_FK.eq(scopePk).and(EVENT.EVENT_MODEL_ID.eq(eventModelId))).and(EVENT.DELETED.isFalse());
		return find(query);
	}

	@Override
	public List<Event> getAllEventsByScopePk(final Long scopePk) {
		final var query = create.selectFrom(EVENT).where(EVENT.SCOPE_FK.eq(scopePk));
		return find(query);
	}

	@Override
	public List<Event> getAllEventsByScopePkAndEventModelId(final Long scopePk, final String eventModelId) {
		final var query = create.selectFrom(EVENT).where(EVENT.SCOPE_FK.eq(scopePk).and(EVENT.EVENT_MODEL_ID.eq(eventModelId)));
		return find(query);
	}

	@Override
	public Event getEventByScopePkAndEventModelIdAndEventNumber(final Long scopePk, final String eventModelId, final int eventNumber) {
		final var query = create.selectFrom(EVENT)
			.where(
				EVENT.SCOPE_FK.eq(scopePk)
					.and(EVENT.EVENT_MODEL_ID.eq(eventModelId))
					.and(EVENT.EVENT_GROUP_NUMBER.eq(eventNumber))
					.and(EVENT.DELETED.isFalse())
			);
		return findUnique(query);
	}

}
