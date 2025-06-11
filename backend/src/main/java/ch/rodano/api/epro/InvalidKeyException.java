package ch.rodano.api.epro;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;

public class InvalidKeyException extends Exception implements ManagedException {
	private static final long serialVersionUID = 8628140211418817059L;

	/**
	 * Constructs a new exception with {@code null} as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 */
	public InvalidKeyException() {}

	/**
	 * Constructs a new exception with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for
	 *                later retrieval by the {@link #getMessage()} method.
	 */
	public InvalidKeyException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
