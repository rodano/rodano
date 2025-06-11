package ch.rodano.core.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import ch.rodano.api.exception.ManagedException;

public final class NoEnabledRoleException extends AuthenticationException implements ManagedException {

	private static final long serialVersionUID = 147954062461199597L;

	public NoEnabledRoleException() {
		super("The user has no active role");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.FORBIDDEN;
	}
}
