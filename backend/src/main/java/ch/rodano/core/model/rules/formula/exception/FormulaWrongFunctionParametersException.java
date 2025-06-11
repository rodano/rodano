package ch.rodano.core.model.rules.formula.exception;

import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.rules.formula.FormulaFunction;

public class FormulaWrongFunctionParametersException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = 5046503907867421007L;

	public FormulaWrongFunctionParametersException(final FormulaFunction function, final int actualParameterNumber, final int expectedParameterNumber) {
		super(String.format("Function %s takes %d parameters, not %d", function.name(), expectedParameterNumber, actualParameterNumber));
	}
}
