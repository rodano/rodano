package ch.rodano.core.helpers;

import org.springframework.stereotype.Service;

import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;

@Service
public class ScopeCreatorService {

	private final ScopeService scopeService;

	public ScopeCreatorService(
		final ScopeService scopeService
	) {
		this.scopeService = scopeService;
	}

	public Scope createScope(final ScopeBuilder scopeBuilder) {
		return scopeService.createFromCandidate(
			scopeBuilder.get(),
			scopeBuilder.get().getScopeModel(),
			scopeBuilder.getParent(),
			scopeBuilder.getContext(),
			null
		);
	}
}
