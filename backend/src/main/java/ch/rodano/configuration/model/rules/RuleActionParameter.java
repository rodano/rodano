package ch.rodano.configuration.model.rules;

import java.util.Collection;
import java.util.Collections;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;

public class RuleActionParameter implements Node {
	private static final long serialVersionUID = -2969109579952293555L;

	private String id;
	private RulableEntity rulableEntity;
	private String conditionId;
	private String value;

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final RulableEntity getRulableEntity() {
		return rulableEntity;
	}

	public final void setRulableEntity(final RulableEntity rulableEntity) {
		this.rulableEntity = rulableEntity;
	}

	public final String getConditionId() {
		return conditionId;
	}

	public final void setConditionId(final String conditionId) {
		this.conditionId = conditionId;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(final String value) {
		this.value = value;
	}

	@Override
	public final Entity getEntity() {
		return Entity.RULE_ACTION_PARAMETER;
	}

	@Override
	public String toString() {
		final var string = new StringBuilder();
		string.append(getId());
		string.append(": ");
		if(rulableEntity != null) {
			string.append(String.format("Entity %s", rulableEntity));
		}
		else if(conditionId != null) {
			string.append(String.format("Condition %s", conditionId));
		}
		else {
			string.append(String.format("%s", value));
		}

		return string.toString();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
