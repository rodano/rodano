package ch.rodano.core.services.plugin.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityDefiner;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.rules.entity.StaticEntity;

@Service
public class EntityPluginServiceImpl implements EntityPluginService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// Stores all the entity rulableBeans in the map using their id as identifier
	private final Map<Pair<EntityType, RulableEntity>, Map<String, IdentifiableEntity>> rulableBeans;
	private final Map<String, StaticEntity> staticBeans;

	public EntityPluginServiceImpl(final List<EntityDefiner> entities) {
		rulableBeans = new HashMap<>();
		staticBeans = new HashMap<>();

		Arrays.stream(EntityType.values()).filter(entityType -> entityType != EntityType.STATIC).forEach(
			entityType -> Arrays.stream(RulableEntity.values()).forEach(
				rulableEntity -> rulableBeans.put(
					Pair.of(entityType, rulableEntity),
					new HashMap<>()
				)
			)
		);

		entities.stream().filter(entity -> entity.getRelatedEntityType() != EntityType.STATIC).forEach(entity -> {
			final var plugins = rulableBeans.get(Pair.of(entity.getRelatedEntityType(), entity.getRelatedRulableEntity()));
			entity.getRegisteredBeans().forEach(plugin -> plugins.put(plugin.getId(), plugin));
		});

		entities.stream().filter(entity -> entity.getRelatedEntityType() == EntityType.STATIC).forEach(
			entity -> entity.getRegisteredBeans().forEach(bean -> staticBeans.put(bean.getId(), (StaticEntity) bean))
		);

		rulableBeans.forEach(
			(key, plugins) -> plugins.forEach((pluginId, _) -> logger.debug("Plugin for entity " + key.getLeft() + " for rulable " + key.getRight() + " with id " + pluginId + " loaded"))
		);
		staticBeans.keySet().forEach(pluginId -> logger.debug("Static plugin " + pluginId + " loaded"));
	}

	/**
	 * Check if a plugin attribute exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return True if it exists, false otherwise
	 */
	@Override
	public Boolean checkPluginAttributeExists(final RulableEntity rulableEntity, final String attributeId) {
		return rulableBeans.get(Pair.of(EntityType.ATTRIBUTE, rulableEntity)).containsKey(attributeId);
	}

	/**
	 * Get the plugin attribute
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return A plugin attribute
	 */
	@Override
	public EntityAttribute getPluginAttribute(final RulableEntity rulableEntity, final String attributeId) {
		return (EntityAttribute) rulableBeans.get(Pair.of(EntityType.ATTRIBUTE, rulableEntity)).get(attributeId);
	}

	/**
	 * Check if a plugin relation exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return True if it exists, false otherwise
	 */
	@Override
	public Boolean checkPluginRelationExists(final RulableEntity rulableEntity, final String relationId) {
		return rulableBeans.get(Pair.of(EntityType.RELATION, rulableEntity)).containsKey(relationId);
	}

	/**
	 * Get the plugin relation
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return A plugin relation
	 */
	@Override
	public EntityRelation getPluginRelation(final RulableEntity rulableEntity, final String relationId) {
		return (EntityRelation) rulableBeans.get(Pair.of(EntityType.RELATION, rulableEntity)).get(relationId);
	}

	/**
	 * Check if a plugin action exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return True if it exists, false otherwise
	 */
	@Override
	public Boolean checkPluginActionExists(final RulableEntity rulableEntity, final String actionId) {
		return rulableBeans.get(Pair.of(EntityType.ACTION, rulableEntity)).containsKey(actionId);
	}

	/**
	 * Get the plugin action
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return A plugin action
	 */
	@Override
	public EntityAction getPluginAction(final RulableEntity rulableEntity, final String actionId) {
		return (EntityAction) rulableBeans.get(Pair.of(EntityType.ACTION, rulableEntity)).get(actionId);
	}

	/**
	 * Check if a static plugin exists
	 *
	 * @param pluginId The plugin id
	 * @return True if it exists, false otherwise
	 */
	@Override
	public Boolean checkStaticPluginExists(final String pluginId) {
		return staticBeans.containsKey(pluginId);
	}

	/**
	 * Get the static plugin
	 *
	 * @param pluginId The plugin id
	 * @return A static plugin
	 */
	@Override
	public StaticEntity getStaticPlugin(final String pluginId) {
		return staticBeans.get(pluginId);
	}
}
