package ch.rodano.api.exception;

import org.springframework.http.HttpStatus;

public class ConfigurationConstraintException extends RuntimeException implements ManagedException {

	public ConfigurationConstraintException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.CONFLICT;
	}
}
