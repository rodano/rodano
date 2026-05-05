package ch.rodano.core.model.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class InconsistentStateDetectedException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 368175752234756194L;

	public InconsistentStateDetectedException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

}
