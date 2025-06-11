package ch.rodano.core.model.exception.security;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class MustChangePasswordException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 8765165256070545373L;

	public MustChangePasswordException() {
		super("Password must be changed");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.FORBIDDEN;
	}
}
