package ch.rodano.core.model.rules.entity;

import java.util.Set;

import ch.rodano.configuration.model.rules.RulableEntity;
import ch.rodano.core.model.rules.Evaluable;

public interface EntityRelation extends IdentifiableEntity {
	RulableEntity getTargetEntity();

	Set<Evaluable> getTargetEvaluables(Evaluable evaluable);
}
