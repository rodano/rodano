package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.exception.TechnicalException;

public class ImpossibleScopeModelPathException extends RuntimeException implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 8403254047223509403L;

	public ImpossibleScopeModelPathException(final ScopeModel model, final ScopeModel parent) {
		super(String.format("Unable to create a %s in a %s", model.getId(), parent.getId()));
	}
}
