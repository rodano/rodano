package ch.rodano.core.plugins.rules.visit;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.entity.AbstractEntityDefiner;

public abstract class AbstractEventEntityDefiner extends AbstractEntityDefiner {
	/**
	 * Get the related rulable entity
	 *
	 * @return A rulable entity
	 */
	@Override
	public RulableEntity getRelatedRulableEntity() {
		return RulableEntity.EVENT;
	}
}
