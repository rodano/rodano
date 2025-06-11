package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.scope.Scope;

public class ImpossibleVirtualChainException extends RuntimeException implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 7871363641164539782L;

	public ImpossibleVirtualChainException(final ScopeModel model, final Scope parent) {
		super(String.format("Unable to create a non virtual %s in virtual parent %s", model.getId(), parent.getCode()));
	}
}
