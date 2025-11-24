package ch.rodano.configuration.exceptions;

import java.io.Serial;

public class RuleBreakException extends Exception {
	@Serial
	private static final long serialVersionUID = 179170340556907064L;

	private final boolean valid;

	/**
	 * Constructor
	 *
	 * @param valid
	 */
	public RuleBreakException(final boolean valid) {
		this.valid = valid;
	}

	public final boolean isValid() {
		return valid;
	}
}
