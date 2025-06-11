package ch.rodano.core.model.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class InvalidOneUseCodeException extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = 6276416946762454757L;

	public InvalidOneUseCodeException() {
		super("Code is invalid");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.NOT_FOUND;
	}
}
