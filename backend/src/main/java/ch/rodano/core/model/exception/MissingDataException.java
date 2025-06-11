package ch.rodano.core.model.exception;

public final class MissingDataException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = 7818321812399281061L;

	public MissingDataException(final String message) {
		super(message);
	}
}
