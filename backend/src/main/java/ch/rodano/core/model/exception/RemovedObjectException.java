package ch.rodano.core.model.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.common.RemovableObject;

public class RemovedObjectException extends RuntimeException implements ManagedException, TechnicalException {

	@Serial
	private static final long serialVersionUID = 1498063173525701017L;

	public RemovedObjectException(final RemovableObject object) {
		super(String.format("%s is removed", object.getClass().getSimpleName()));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
