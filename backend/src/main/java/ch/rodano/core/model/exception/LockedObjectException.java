package ch.rodano.core.model.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.common.LockableObject;

public class LockedObjectException extends RuntimeException implements ManagedException, TechnicalException {
	@Serial
	private static final long serialVersionUID = -443747186092021364L;

	public LockedObjectException(final LockableObject object) {
		super(String.format("%s is locked", object.getClass().getSimpleName()));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
