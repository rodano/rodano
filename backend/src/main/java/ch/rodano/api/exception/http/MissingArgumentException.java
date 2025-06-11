package ch.rodano.api.exception.http;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class MissingArgumentException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = -3242583625226819191L;

	public MissingArgumentException(final String parameterName) {
		super("You have to provide " + parameterName + " to perform this action");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}

}
