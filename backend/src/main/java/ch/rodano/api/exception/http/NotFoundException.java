package ch.rodano.api.exception.http;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class NotFoundException extends RuntimeException implements ManagedException {

	@Serial
	private static final long serialVersionUID = 2633899282052460313L;

	public NotFoundException(final Class<?> clazz, final Long pk) {
		super("Unable to find " + clazz.getSimpleName().toLowerCase() + " with pk=" + pk);
	}

	public NotFoundException(final Class<?> clazz, final String id) {
		super("Unable to find " + clazz.getSimpleName().toLowerCase() + " with id=" + id);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.NOT_FOUND;
	}
}
