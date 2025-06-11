package ch.rodano.core.services.rule;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.EntityAttribute;
import ch.rodano.core.model.rules.entity.EntityRelation;
import ch.rodano.core.services.plugin.entity.EntityPluginService;
import ch.rodano.core.services.plugin.entity.EntityType;
import ch.rodano.core.services.rule.exception.UnknownPluginException;

/**
 * This service is a wrapper of the entity plugin service
 */
@Service
public class RulableEntityBinderServiceImpl implements RulableEntityBinderService {
	private final EntityPluginService entityPluginService;

	public RulableEntityBinderServiceImpl(final EntityPluginService entityPluginService) {
		this.entityPluginService = entityPluginService;
	}

	/**
	 * Get an attribute entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return An attribute entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	@Override
	public EntityAttribute getAttribute(final RulableEntity rulableEntity, final String attributeId) {
		if(entityPluginService.checkPluginAttributeExists(rulableEntity, attributeId)) {
			return entityPluginService.getPluginAttribute(rulableEntity, attributeId);
		}

		throw new UnknownPluginException(rulableEntity, EntityType.ATTRIBUTE, attributeId);
	}

	/**
	 * Get a relation entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return A relation entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	@Override
	public EntityRelation getRelation(final RulableEntity rulableEntity, final String relationId) {
		if(entityPluginService.checkPluginRelationExists(rulableEntity, relationId)) {
			return entityPluginService.getPluginRelation(rulableEntity, relationId);
		}

		throw new UnknownPluginException(rulableEntity, EntityType.RELATION, relationId);
	}

	/**
	 * Get an action entity
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return An action entity
	 * @throws UnknownPluginException Thrown if no plugin is found with the given parameters
	 */
	@Override
	public EntityAction getAction(final RulableEntity rulableEntity, final String actionId) {
		if(entityPluginService.checkPluginActionExists(rulableEntity, actionId)) {
			return entityPluginService.getPluginAction(rulableEntity, actionId);
		}

		throw new UnknownPluginException(rulableEntity, EntityType.ACTION, actionId);
	}

	/**
	 * Check if an attribute entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param attributeId   The attribute id
	 * @return True if the attribute entity exists and false otherwise
	 */
	@Override
	public Boolean attributeExists(final RulableEntity rulableEntity, final String attributeId) {
		return entityPluginService.checkPluginAttributeExists(rulableEntity, attributeId);
	}

	/**
	 * Check if a relation entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param relationId    The relation id
	 * @return True if the relation entity exists and false otherwise
	 */
	@Override
	public Boolean relationExists(final RulableEntity rulableEntity, final String relationId) {
		return entityPluginService.checkPluginRelationExists(rulableEntity, relationId);
	}

	/**
	 * Check if an action entity exists
	 *
	 * @param rulableEntity The rulable entity
	 * @param actionId      The action id
	 * @return True if the action entity exists and false otherwise
	 */
	@Override
	public Boolean actionExists(final RulableEntity rulableEntity, final String actionId) {
		return entityPluginService.checkPluginActionExists(rulableEntity, actionId);
	}
}
