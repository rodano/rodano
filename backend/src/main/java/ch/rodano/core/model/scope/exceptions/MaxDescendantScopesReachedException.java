package ch.rodano.core.model.scope.exceptions;

import java.io.Serial;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.model.scope.Scope;

public class MaxDescendantScopesReachedException extends RuntimeException implements TechnicalException, ManagedException {
	@Serial
	private static final long serialVersionUID = -8940370412257766462L;

	public static final Map<String, String> MAX_NUMBER_REACHED = new TreeMap<>();
	public static final String DEFAULT_MAX_NUMBER_REACHED = "%s's number of descendants is limited to %d.";
	public static final Map<String, String> MAX_NUMBER_REACHED_IN_SCOPE = new TreeMap<>();
	public static final String DEFAULT_MAX_NUMBER_REACHED_IN_SCOPE = "%s's number of descendants is limited to %d in %s %s.";

	static {
		MAX_NUMBER_REACHED.put(LanguageStatic.en.getId(), DEFAULT_MAX_NUMBER_REACHED);
		MAX_NUMBER_REACHED.put(LanguageStatic.fr.getId(), "Le nombre de %s est limité à %d.");

		MAX_NUMBER_REACHED_IN_SCOPE.put(LanguageStatic.en.getId(), DEFAULT_MAX_NUMBER_REACHED_IN_SCOPE);
		MAX_NUMBER_REACHED_IN_SCOPE.put(LanguageStatic.fr.getId(), "Le nombre de %s est limité à %d dans le %s %s.");
	}

	public MaxDescendantScopesReachedException(final ScopeModel scopeModel) {
		super(
			String.format(
				DEFAULT_MAX_NUMBER_REACHED,
				scopeModel.getId(),
				scopeModel.getMaxNumber()
			)
		);
	}

	public MaxDescendantScopesReachedException(final ScopeModel scopeModel, final Scope container) {
		super(
			String.format(
				DEFAULT_MAX_NUMBER_REACHED_IN_SCOPE,
				scopeModel.getStudy().getLeafScopeModel().getId(),
				container.getMaxNumber(),
				container.getScopeModel().getId(),
				container.getCode()
			)
		);
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
