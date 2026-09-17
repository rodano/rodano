package ch.rodano.core.model.exception.security;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class MaintenanceModeException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 1L;

	public MaintenanceModeException() {
		super("The application is currently under maintenance");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.SERVICE_UNAVAILABLE;
	}
}
