package ch.rodano.core.model.exception.security;

//this exception must never be sent to the user for not disclosing information about existing users
public class TooManyAttemptsException extends RuntimeException {

	private static final long serialVersionUID = 8182108171171763136L;

	public TooManyAttemptsException(final int maxAttempts) {
		super(String.format("Account locked due to %s failed attempts", maxAttempts));
	}
}
