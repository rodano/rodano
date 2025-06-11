package ch.rodano.core.model.exception.security;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class WrongTwoStepTokenException extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = -784973102454582905L;

	public WrongTwoStepTokenException() {}

	public WrongTwoStepTokenException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
