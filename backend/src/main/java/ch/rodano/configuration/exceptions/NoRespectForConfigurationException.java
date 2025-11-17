package ch.rodano.configuration.exceptions;

import java.io.Serial;

public final class NoRespectForConfigurationException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -5787488342725660255L;

	/**
	 * Constructor
	 *
	 * @param message The message
	 */
	public NoRespectForConfigurationException(final String message) {
		super(message);
	}
}
