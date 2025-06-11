package ch.rodano.core.model.exception.security;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class WeakPasswordException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = 7685976028881528633L;

	public WeakPasswordException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.PRECONDITION_FAILED;
	}
}
