package ch.rodano.core.model.rules.entity;

import java.util.List;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.services.plugin.entity.EntityType;

public interface EntityDefiner {
	/**
	 * Get the related entity type
	 *
	 * @return An entity type
	 */
	EntityType getRelatedEntityType();

	/**
	 * Get the related rulable entity
	 *
	 * @return A rulable entity
	 */
	RulableEntity getRelatedRulableEntity();

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	List<IdentifiableEntity> getRegisteredBeans();
}
