package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.scope.Scope;

public class OutOfEnrollmentWindowException extends RuntimeException implements ManagedException {
	@Serial
	private static final long serialVersionUID = -994174716076526158L;

	public OutOfEnrollmentWindowException(final Scope scope) {
		super("The scope " + scope.getCode() + " is out of enrollment time window, child cannot be added");
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
