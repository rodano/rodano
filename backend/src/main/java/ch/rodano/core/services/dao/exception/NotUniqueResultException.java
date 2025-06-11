package ch.rodano.core.services.dao.exception;

import ch.rodano.core.model.exception.TechnicalException;

public class NotUniqueResultException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = -5028537241469207060L;

	public NotUniqueResultException(final String message) {
		super(message);
	}
}
