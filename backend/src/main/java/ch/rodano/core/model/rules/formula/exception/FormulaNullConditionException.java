package ch.rodano.core.model.rules.formula.exception;

import java.io.Serial;

import ch.rodano.core.model.exception.TechnicalException;

public class FormulaNullConditionException extends RuntimeException implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 204523419813053082L;

	public FormulaNullConditionException(final String message) {
		super(message);
	}

	public FormulaNullConditionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
