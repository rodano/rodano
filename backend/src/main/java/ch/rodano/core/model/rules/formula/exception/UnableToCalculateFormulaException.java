package ch.rodano.core.model.rules.formula.exception;

import java.io.Serial;

public class UnableToCalculateFormulaException extends Exception {

	@Serial
	private static final long serialVersionUID = -208364812713950687L;

	public UnableToCalculateFormulaException(final String formula, final Exception e) {
		super(String.format("Unable to calculate formula %s: %s", formula, e.getLocalizedMessage()));
	}
}
