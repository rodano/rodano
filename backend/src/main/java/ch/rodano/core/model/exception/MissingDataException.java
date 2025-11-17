package ch.rodano.core.model.exception;

import java.io.Serial;

public final class MissingDataException extends RuntimeException implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 7818321812399281061L;

	public MissingDataException(final String message) {
		super(message);
	}
}
