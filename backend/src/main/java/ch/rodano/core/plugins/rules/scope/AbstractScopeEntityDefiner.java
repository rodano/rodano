package ch.rodano.core.plugins.rules.scope;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.AbstractEntityDefiner;

public abstract class AbstractScopeEntityDefiner extends AbstractEntityDefiner {
	/**
	 * Get the related rulable entity
	 *
	 * @return A rulable entity
	 */
	@Override
	public RulableEntity getRelatedRulableEntity() {
		return RulableEntity.SCOPE;
	}
}
