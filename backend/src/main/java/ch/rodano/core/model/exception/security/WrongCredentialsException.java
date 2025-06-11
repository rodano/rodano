package ch.rodano.core.model.exception.security;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class WrongCredentialsException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = -3484601656930020185L;

	public WrongCredentialsException() {
		super("Wrong e-mail or password");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
