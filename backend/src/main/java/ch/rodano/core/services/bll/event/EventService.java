package ch.rodano.core.services.bll.event;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Progression;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.utils.ACL;

public interface EventService {

	Event create(
		Scope scope,
		EventModel eventModel,
		DatabaseActionContext context,
		String rationale
	);

	List<Event> createAll(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	void save(
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	void delete(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	void restore(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	void lock(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	void unlock(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Validate all datasets attached to the event.
	 * @param scope Event's scope
	 * @param event The event
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 */
	void validate(
		Scope scope,
		Event event,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Update the event date. Note that this action can modify dates of the events that are dependent on the provided event.
	 * @param scope The event's scope
	 * @param event The event
	 * @param date  The new event date
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 * @return  Updated event
	 */
	Event updateDate(
		Scope scope,
		Event event,
		ZonedDateTime date,
		DatabaseActionContext context,
		String rationale
	);

	/**
	 * Reset dates of all events on scope.
	 * @param scope The scope
	 * @param context   Action context
	 * @param rationale The rationale for the operation
	 */
	void resetDates(
		Scope scope,
		DatabaseActionContext context,
		String rationale
	);

	List<Event> getAll(Scope scope);

	Optional<Event> get(Dataset dataset);

	Optional<Event> get(Form form);

	Optional<Event> get(WorkflowStatus workflowStatus);

	Event get(
		Scope scope,
		EventModel eventModel,
		int eventGroupNumber
	);

	/**
	 * Search for events for the given scope and the given ACL.
	 *
	 * @param scope The scope
	 * @param acl The ACL
	 * @return Events associated with the scope
	 */
	List<Event> search(Scope scope, ACL acl);

	/**
	 * Get all the events (including removed ones) matching the provided event model.
	 *
	 * @param eventModel The event model
	 * @return The events matching the event model
	 */
	List<Event> getAllIncludingRemoved(EventModel eventModel);

	/**
	 * Get all the events (including removed ones) for the given scope.
	 *
	 * @param scope The scope
	 * @return Events associated with the scope
	 */
	List<Event> getAllIncludingRemoved(Scope scope);

	/**
	 * Get all the events (including removed ones) for the given scope and event model.
	 *
	 * @param scope The scope
	 * @param eventModel The event model
	 * @return Events associated with the scope and the event model
	 */
	List<Event> getAllIncludingRemoved(Scope scope, EventModel eventModel);

	/**
	 * Get the events for the given scope and event model.
	 *
	 * @param scope The scope
	 * @param eventModel The event mode
	 * @return Events associated with the scope and the event model
	 */
	List<Event> getAll(Scope scope, EventModel eventModel);

	/**
	 * Get the inceptive event, as defined in the configuration.
	 * @param scope The scope
	 * @return The inceptive event
	 */
	Event getInceptive(Scope scope);

	ZonedDateTime getTheoreticalDate(Scope scope, Event event);

	/**
	 * Get event's progression.
	 * @param event The event
	 * @return  The event's progression
	 */
	Progression getProgression(Event event);

	/**
	 * Get the previous event in the chronological order, if any.
	 * @param event The event
	 * @return  Event preceding the given event
	 */
	Optional<Event> getPrevious(Event event);

	/**
	 * Get the next event in the chronological order, if any.
	 * @param event The event
	 * @return  Event following the given event
	 */
	Optional<Event> getNext(Event event);

	/**
	 * Get the available event models on scope.
	 * @param scope The scope
	 * @return  A list of available event models
	 */
	List<EventModel> getEventModels(Scope scope);

	String getLabel(
		Scope scope,
		Event event,
		String... languages
	);

	String getLabel(Scope scope, Event event);
}
