package ch.rodano.core.utils;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.DeletedObjectException;
import ch.rodano.core.model.exception.LockedObjectException;
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
	 * Checks if the object is not deleted, throws an exception otherwise.
	 * @param o         The object itself
	 * @throws DeletedObjectException    Thrown if the object is deleted
	 */
	void checkNotDeleted(DeletableObject o);

	/**
	 * Checks if the scope is not locked, throws an exception otherwise.
	 * @param scope         The scope
	 * @throws LockedObjectException    Thrown if the scope is locked
	 */
	void checkNotLocked(Scope scope);

	/**
	 * Checks if the event is not locked, throws an exception otherwise.
	 * @param event         The event
	 * @throws LockedObjectException    Thrown if the event is locked
	 */
	void checkNotLocked(Event event);
}
