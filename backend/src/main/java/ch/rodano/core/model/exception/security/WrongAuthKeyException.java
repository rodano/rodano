package ch.rodano.core.model.exception.security;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class WrongAuthKeyException extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = 963328271260677559L;

	public WrongAuthKeyException() {}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
