package ch.rodano.core.services.rule.exception;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.exception.TechnicalException;
import ch.rodano.core.services.plugin.entity.EntityType;

public class UnknownPluginException extends RuntimeException implements TechnicalException {
	private static final long serialVersionUID = -179147243078889827L;

	public UnknownPluginException(final RulableEntity rulableEntity, final EntityType entityType, final String pluginName) {
		super("The " + entityType.name().toLowerCase() + " plugin '" + pluginName + "' has not been found for the rulable entity: " + rulableEntity);
	}
}
