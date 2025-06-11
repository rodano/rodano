package ch.rodano.core.services.bll.scope;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.core.model.exception.TechnicalException;

public class ScopeRelationException extends RuntimeException implements ManagedException, TechnicalException {

	@Serial
	private static final long serialVersionUID = 6342236255220190346L;

	public ScopeRelationException(final String message) {
		super(message);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
