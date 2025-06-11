package ch.rodano.core.model.rules.formula.exception;

import ch.rodano.core.model.exception.TechnicalException;

public class FormulaNullParameterException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = 204523419813053082L;

	public FormulaNullParameterException() {
		super("One of parameter for the function is null");
	}

}
