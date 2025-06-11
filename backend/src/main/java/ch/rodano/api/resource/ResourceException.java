package ch.rodano.api.resource;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class ResourceException extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = 1515352132495239409L;

	public ResourceException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
