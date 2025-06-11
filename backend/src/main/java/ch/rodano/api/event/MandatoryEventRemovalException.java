package ch.rodano.api.event;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class MandatoryEventRemovalException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = 6873162138022468014L;

	public MandatoryEventRemovalException() {
		super("You can not remove an event that is mandatory");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
