package ch.rodano.core.model.rules.formula.exception;

public class UnableToCalculateFormulaException extends Exception {

	private static final long serialVersionUID = -208364812713950687L;

	public UnableToCalculateFormulaException(final Exception e) {
		super(e);
	}
}
