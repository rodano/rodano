package ch.rodano.core.model.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;

public class LockedObjectException extends RuntimeException implements ManagedException, TechnicalException {
	@Serial
	private static final long serialVersionUID = -443747186092021364L;

	public LockedObjectException(final Scope scope) {
		super("Scope" + scope.getCode() + " is locked");
	}

	public LockedObjectException(final Event event) {
		super("Event " + event.getEventModelId() + " is locked");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
