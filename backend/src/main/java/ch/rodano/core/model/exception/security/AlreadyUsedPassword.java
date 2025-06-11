package ch.rodano.core.model.exception.security;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class AlreadyUsedPassword extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = 6904976712601969446L;

	public AlreadyUsedPassword() {
		super("The password has already been used");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
