package ch.rodano.core.services.rule;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.services.rule.exception.UnknownPluginException;

public interface RulableEntityBinderService {
	/**
	 * Get an attribute entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return An attribute entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	EntityAttribute getAttribute(RulableEntity rulableEntity, String attributeId);

	/**
	 * Get a relation entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return A relation entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	EntityRelation getRelation(RulableEntity rulableEntity, String relationId);

	/**
	 * Get an action entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return An action entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	EntityAction getAction(RulableEntity rulableEntity, String actionId);

	/**
	 * Check if an attribute entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return True if the attribute entity exists and false otherwise
	 */
	Boolean attributeExists(RulableEntity rulableEntity, String attributeId);

	/**
	 * Check if a relation entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return True if the relation entity exists and false otherwise
	 */
	Boolean relationExists(RulableEntity rulableEntity, String relationId);

	/**
	 * Check if an action entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return True if the action entity exists and false otherwise
	 */
	Boolean actionExists(RulableEntity rulableEntity, String actionId);
}
