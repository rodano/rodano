package ch.rodano.api.controller.user.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class EmailAlreadyUsedException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = -5371079453802727633L;

	public EmailAlreadyUsedException() {
		super("The given e-mail is already in use");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
