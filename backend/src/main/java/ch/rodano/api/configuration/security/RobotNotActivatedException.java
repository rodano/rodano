package ch.rodano.api.configuration.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import ch.rodano.api.exception.ManagedException;

public class RobotNotActivatedException extends AuthenticationException implements ManagedException {
	private static final long serialVersionUID = 6943086598314464416L;

	public RobotNotActivatedException(final String robotName) {
		super("The robot named " + robotName + " has not been activated");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
