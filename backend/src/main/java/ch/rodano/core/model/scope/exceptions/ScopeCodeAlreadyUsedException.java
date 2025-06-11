package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.exception.TechnicalException;

public class ScopeCodeAlreadyUsedException extends RuntimeException implements TechnicalException, ManagedException {
	@Serial
	private static final long serialVersionUID = -7939657985768017820L;

	public ScopeCodeAlreadyUsedException(final String code) {
		super(String.format("The scope code %s is already used", code));
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
