package ch.rodano.api.configuration.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import ch.rodano.api.exception.ManagedException;

public class RobotDoesNotExistException extends AuthenticationException implements ManagedException {
	private static final long serialVersionUID = 3089763968832398606L;

	public RobotDoesNotExistException(final String robotName) {
		super("The robot named " + robotName + " does not exist");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
