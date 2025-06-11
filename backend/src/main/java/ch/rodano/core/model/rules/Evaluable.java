package ch.rodano.core.model.rules;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.common.TimestampableObject;

public interface Evaluable extends TimestampableObject {

	Long getPk();

	String getId();

	RulableEntity getRulableEntity();
}
