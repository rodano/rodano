package ch.rodano.api.controller.user.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class InvalidEmailException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = -5371079453802727633L;

	public InvalidEmailException(final String message) {
		super(String.format("The e-mail provided is not valid: %s", message));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
