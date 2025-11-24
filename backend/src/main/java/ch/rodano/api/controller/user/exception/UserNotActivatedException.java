package ch.rodano.api.controller.user.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class UserNotActivatedException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = -7662550671758037572L;

	public UserNotActivatedException() {
		super("The user has not been activated");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
