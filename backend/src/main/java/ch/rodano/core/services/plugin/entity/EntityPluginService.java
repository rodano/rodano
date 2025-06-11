package ch.rodano.core.services.plugin.entity;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.StaticEntity;
import ch.rodano.core.services.plugin.PluginService;

public interface EntityPluginService extends PluginService {
	/**
	 * Check if a plugin attribute exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return True if it exists, false otherwise
	 */
	Boolean checkPluginAttributeExists(final RulableEntity rulableEntity, final String attributeId);

	/**
	 * Get the plugin attribute
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return A plugin attribute
	 */
	EntityAttribute getPluginAttribute(final RulableEntity rulableEntity, final String attributeId);

	/**
	 * Check if a plugin relation exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return True if it exists, false otherwise
	 */
	Boolean checkPluginRelationExists(final RulableEntity rulableEntity, final String relationId);

	/**
	 * Get the plugin relation
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return A plugin relation
	 */
	EntityRelation getPluginRelation(final RulableEntity rulableEntity, final String relationId);

	/**
	 * Check if a plugin action exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return True if it exists, false otherwise
	 */
	Boolean checkPluginActionExists(final RulableEntity rulableEntity, final String actionId);

	/**
	 * Get the plugin action
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return A plugin action
	 */
	EntityAction getPluginAction(final RulableEntity rulableEntity, final String actionId);

	/**
	 * Check if a static plugin exists
	 *
	 * @param pluginId The plugin id
	 * @return True if it exists, false otherwise
	 */
	Boolean checkStaticPluginExists(final String pluginId);

	/**
	 * Get the static plugin
	 *
	 * @param pluginId The plugin id
	 * @return A static plugin
	 */
	StaticEntity getStaticPlugin(final String pluginId);
}
