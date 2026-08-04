package ch.rodano.core.utils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.common.LockableObject;
import ch.rodano.core.model.common.RemovableObject;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.RemovedObjectException;
import ch.rodano.core.model.scope.Scope;

public interface UtilsService {
	DateTimeFormatter HUMAN_READABLE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	DateTimeFormatter HUMAN_READABLE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	DateTimeFormatter HUMAN_READABLE_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	/**
	 * Checks if the object is present, throws an exception otherwise.
	 * @param clazz     Class of the object
	 * @param o         The object itself
	 * @param pk        The PK of the object
	 * @throws NotFoundException    Thrown if the object is not present
	 */
	void checkNotNull(Class<?> clazz, Optional<?> o, final Optional<Long> pk);

	/**
	 * Checks if the object is present, throws an exception otherwise.
	 * @param clazz     Class of the object
	 * @param o         The object itself
	 * @param pk        The PK of the object
	 * @throws NotFoundException    Thrown if the object is not present
	 */
	void checkNotNull(Class<?> clazz, Object o, Long pk);

	/**
	 * Checks if the object is present, throws an exception otherwise.
	 * @param clazz     Class of the object
	 * @param o         The object itself
	 * @param id        The id of the object
	 * @throws NotFoundException    Thrown if the object is not present
	 */
	void checkNotNull(Class<?> clazz, Object o, String id);

	/**
	 * Checks if the object is not removed, throws an exception otherwise.
	 * @param o         The object itself
	 * @throws RemovedObjectException    Thrown if the object is removed
	 */
	void checkNotRemoved(RemovableObject o);

	/**
	 * Checks if the scope is not removed and, if the event is present, that it is not removed either, throws an exception otherwise.
	 * @param scope     The scope
	 * @param event     The optional event belonging to the scope
	 * @throws RemovedObjectException    Thrown if the scope or the event is removed
	 */
	void checkNotRemoved(Scope scope, Optional<Event> event);

	/**
	 * Checks if the object is not locked, throws an exception otherwise.
	 * @param o         The object itself
	 */
	void checkNotLocked(LockableObject o);

	/**
	 * Checks if the scope is not locked and, if the event is present, that it is not locked either, throws an exception otherwise.
	 * @param scope     The scope
	 * @param event     The optional event belonging to the scope
	 */
	void checkNotLocked(Scope scope, Optional<Event> event);
}
