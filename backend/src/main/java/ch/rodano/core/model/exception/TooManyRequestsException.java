package ch.rodano.core.model.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class TooManyRequestsException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 5207348912675913302L;

	public TooManyRequestsException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.TOO_MANY_REQUESTS;
	}

}
