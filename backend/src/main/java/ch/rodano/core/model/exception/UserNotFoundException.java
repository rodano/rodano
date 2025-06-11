package ch.rodano.core.model.exception;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public final class UserNotFoundException extends RuntimeException implements ManagedException {
	private static final long serialVersionUID = 6276416946762454757L;

	public UserNotFoundException(final String email) {
		super(String.format("No user with e-mail %s in this study", email));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.NOT_FOUND;
	}
}
