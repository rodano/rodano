package ch.rodano.core.plugins.entity;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.AbstractEntityDefiner;
import ch.rodano.core.services.plugin.entity.EntityType;

public abstract class AbstractStaticEntityDefiner extends AbstractEntityDefiner {
	/**
	 * Get the related rulable entity
	 *
	 * @return A rulable entity
	 */
	@Override
	public RulableEntity getRelatedRulableEntity() {
		return null;
	}

	/**
	 * Get the related entity type
	 *
	 * @return An entity type
	 */
	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.STATIC;
	}
}
