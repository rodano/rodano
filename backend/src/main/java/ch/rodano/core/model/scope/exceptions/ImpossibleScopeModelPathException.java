package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;

import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.exception.TechnicalException;

public class ImpossibleScopeModelPathException extends RuntimeException implements TechnicalException {
	@Serial
	private static final long serialVersionUID = 8403254047223509403L;

	public ImpossibleScopeModelPathException(final ScopeModel model, final ScopeModel parent) {
		super(parent == null
			? String.format("Scope model [%s] cannot be created without a parent (only the root model can).", safeId(model))
			: String.format("Scope model [%s] cannot be a child of [%s].", safeId(model), safeId(parent)));
	}

	private static String safeId(final ScopeModel m) {
		return m == null ? "null" : m.getId();
	}
}
