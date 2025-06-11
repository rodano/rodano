package ch.rodano.core.model.rules.formula.exception;

import ch.rodano.core.model.exception.TechnicalException;

public class FormulaBadSyntaxException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = 5046503907867421007L;

	public FormulaBadSyntaxException(final String message) {
		super(message);
	}
}
