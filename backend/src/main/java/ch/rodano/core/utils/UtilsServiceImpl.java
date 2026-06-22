package ch.rodano.core.utils;

import java.util.Optional;

import org.springframework.stereotype.Service;

import ch.rodano.api.exception.http.NotFoundException;
import ch.rodano.core.model.common.DeletableObject;
import ch.rodano.core.model.common.LockableObject;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.DeletedObjectException;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.scope.Scope;

@Service
public class UtilsServiceImpl implements UtilsService {

	@Override
	public void checkNotNull(final Class<?> clazz, final Optional<?> o, final Optional<Long> pk) {
		if(o.isEmpty() && pk.isPresent()) {
			throw new NotFoundException(clazz, pk.get());
		}
	}

	@Override
	public void checkNotNull(final Class<?> clazz, final Object o, final Long pk) {
		if(o == null) {
			throw new NotFoundException(clazz, pk);
		}
	}

	@Override
	public void checkNotNull(final Class<?> clazz, final Object o, final String id) {
		if(o == null) {
			throw new NotFoundException(clazz, id);
		}
	}

	@Override
	public void checkNotDeleted(final DeletableObject o) {
		if(o.getDeleted()) {
			throw new DeletedObjectException(o);
		}
	}

	@Override
	public void checkNotDeleted(final Scope scope, final Optional<Event> event) {
		if(scope.getDeleted()) {
			throw new DeletedObjectException(scope);
		}
		if(event.isPresent() && event.get().getDeleted()) {
			throw new DeletedObjectException(event.get());
		}
	}

	@Override
	public void checkNotLocked(final LockableObject o) {
		if(o.getLocked()) {
			throw new LockedObjectException(o);
		}
	}

	@Override
	public void checkNotLocked(final Scope scope, final Optional<Event> event) {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}
		if(event.isPresent() && event.get().getLocked()) {
			throw new LockedObjectException(event.get());
		}
	}
}
