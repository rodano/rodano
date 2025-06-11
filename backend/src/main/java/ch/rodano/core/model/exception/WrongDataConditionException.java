package ch.rodano.core.model.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class WrongDataConditionException extends RuntimeException implements ManagedException {

	private static final long serialVersionUID = 368175752234756194L;

	public WrongDataConditionException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}

}
