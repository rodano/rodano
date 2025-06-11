package ch.rodano.api.exception.http;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class ForbiddenOperationException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = -4756725511335581895L;

	public ForbiddenOperationException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.FORBIDDEN;
	}
}
