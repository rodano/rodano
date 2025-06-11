package ch.rodano.core.services.dao.event;

import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.Function;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.audit.models.EventAuditTrail;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;

public interface EventDAOService {
	Event getEventByPk(Long pk);

	Event getEventById(String id);

	void saveEvent(Event event, DatabaseActionContext context, String rationale);

	void deleteEvent(Event event, DatabaseActionContext context, String rationale);

	void restoreEvent(Event event, DatabaseActionContext context, String rationale);

	List<Event> getEventsByScopePk(Long scopePk);

	/**
	 * Get the events matching the provided scope pk and event model id
	 * @param scopePk
	 * @param eventModelId
	 * @return The events associated with the scope and matching the event model id
	 */
	List<Event> getEventsByScopePkAndEventModelId(Long scopePk, String eventModelId);

	/**
	 * Get all the events (including deleted) matching the provided event model id
	 *
	 * @param eventModelId The event model id
	 * @return The events matching the event model id
	 */
	List<Event> getAllEventsByEventModelId(String eventModelId);

	/**
	 * Get all the events (including deleted) associated with the given scope pk
	 *
	 * @param scopePk The scope pk
	 * @return The events associated with the scope
	 */
	List<Event> getAllEventsByScopePk(Long scopePk);

	List<Event> getAllEventsByScopePkAndEventModelId(Long scopePk, String eventModelId);

	Event getEventByScopePkAndEventModelIdAndEventNumber(Long scopePk, String eventModelId, int eventNumber);

	NavigableSet<EventAuditTrail> getAuditTrails(Event event, Optional<Timeframe> timeframe, Optional<Long> actorPk);

	NavigableSet<EventAuditTrail> getAuditTrailsForProperty(Event event, Optional<Timeframe> timeframe, Function<EventAuditTrail, Object> property);

	NavigableSet<EventAuditTrail> getAuditTrailsForProperties(Event event, Optional<Timeframe> timeframe, List<Function<EventAuditTrail, Object>> properties);

}
