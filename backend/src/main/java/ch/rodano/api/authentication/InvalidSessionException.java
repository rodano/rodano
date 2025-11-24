package ch.rodano.api.authentication;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import ch.rodano.api.exception.ManagedException;

public class InvalidSessionException extends AuthenticationException implements ManagedException {
	@Serial
	private static final long serialVersionUID = -4478919899121156539L;

	public InvalidSessionException() {
		super("Session has expired");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
