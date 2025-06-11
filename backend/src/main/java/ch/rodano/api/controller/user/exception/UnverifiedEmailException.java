package ch.rodano.api.controller.user.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class UnverifiedEmailException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = 7476617757206924474L;

	public UnverifiedEmailException() {
		super("The user's e-mail has not been verified");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
