package ch.rodano.core.model.rules.formula.exception;

import ch.rodano.core.model.exception.TechnicalException;

public class FormulaWrongParameterTypeException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = 204523419813053082L;

	public FormulaWrongParameterTypeException(final Object o, final Exception cause) {
		super(String.format("The parameter %s for the function has an invalid type", o), cause);
	}

}
