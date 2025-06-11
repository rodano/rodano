package ch.rodano.api.exception.http;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class ForbiddenArgumentException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = -5659999712025034404L;

	public ForbiddenArgumentException() {
		super("One of the parameter is either forbidden or cannot be used in this context");
	}

	public ForbiddenArgumentException(final String parameterValue) {
		super("The parameter's value: " + parameterValue + " is forbidden in this context");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.FORBIDDEN;
	}
}
