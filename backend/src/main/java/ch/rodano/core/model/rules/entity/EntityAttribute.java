package ch.rodano.core.model.rules.entity;

import ch.rodano.configuration.model.rules.OperandType;
import ch.rodano.core.model.rules.Evaluable;

public interface EntityAttribute extends IdentifiableEntity {
	Object getValue(Evaluable evaluable);

	OperandType getType();
}
