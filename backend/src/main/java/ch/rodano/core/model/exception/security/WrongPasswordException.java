package ch.rodano.core.model.exception.security;

import java.io.Serial;

//this exception must never be sent to the user for not disclosing information about existing users
public final class WrongPasswordException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -7627973212995854926L;

	public WrongPasswordException() {
		super("Wrong password");
	}
}
